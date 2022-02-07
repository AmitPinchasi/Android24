package com.SharxNZ.Battle;

import com.SharxNZ.Android24;
import com.SharxNZ.Game.Attack;
import com.SharxNZ.Gifs.ExpGif;
import com.SharxNZ.Gifs.Gif;
import com.SharxNZ.Gifs.ResultGif;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.NameNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

enum TurnType {
    Attack, Defence
}

public class Battle {

    private static final Random rand = new Random();

    private static final HashMap<Long, Battle> battles = new HashMap<>();
    private final HashMap<Long, Fighter> fightersMap = new HashMap<>();
    private long channelId;
    private long resultEmbedId;
    private Queue<Fighter> attackOrder = new LinkedList<>();
    private TurnType turnType = TurnType.Attack;
    private byte passes;
    private Fighter attacker;
    private Fighter defender;

    public Battle(@NotNull Category category, User @NotNull ... users) {
        ChannelAction<TextChannel> channel = category.createTextChannel(users[0].getName() + " vs " + users[1].getName());
        String mention = "";
        Fighter tempFighter;
        for (User user : users) {
            mention = mention.concat(user.getAsMention());
            tempFighter = new Fighter(user.getIdLong());
            fightersMap.put(user.getIdLong(), tempFighter);
            attackOrder.add(tempFighter);
            channel.addMemberPermissionOverride(user.getIdLong(), EnumSet.of(Permission.MESSAGE_WRITE), null);
        }
        tempFighter = attackOrder.poll();
        long firstFighter = tempFighter.getUserID();
        while (attackOrder.peek().getUserID() != firstFighter) {
            tempFighter.setTarget(attackOrder.peek());
            attackOrder.add(tempFighter);
            tempFighter = attackOrder.poll();
        }
        tempFighter.setTarget(attackOrder.peek());
        attackOrder.add(tempFighter);
        String finalMention = mention;
        channel.queue(textChannel -> {
            channelId = textChannel.getIdLong();
            battles.put(channelId, this);
            textChannel.sendMessage("The battle started!\n" + finalMention).queue();
            textChannel.sendMessageEmbeds(new EmbedBuilder().setImage("https://freepngimg.com/thumb/fight/28608-6-fight.png").build()).queue(
                    message -> resultEmbedId = message.getIdLong());
        });

        attacker = attackOrder.poll();
        defender = attacker.getTarget();
        waiter();
    }

    public static void battlesCleanup() throws SQLException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT BattlesCt FROM guilds.guilds_data;")
        ) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Category category = Android24.jda.getCategoryById(resultSet.getLong(1));
                if (category != null)
                    category.getTextChannels().forEach(textChannel -> textChannel.delete().queue());
            }
        }
    }

    public static Battle getBattle(long channelId) {
        return battles.get(channelId);
    }

    private void waiter() {
        Android24.eventWaiter.waitForEvent(SlashCommandEvent.class, sce -> sce.getName().equals("battle")
                && !sce.getSubcommandName().equals("pvp") && channelId == sce.getChannel().getIdLong(), sce -> {
            switch (sce.getSubcommandName()) {
                case "action", "special_attack" -> {
                    long userID = sce.getUser().getIdLong();
                    if (getFightersMap().containsKey(userID)) {
                        sce.reply(turn(userID, sce.getOptionsByName("target").isEmpty() ? 0 : sce.getOption("target").getAsUser().getIdLong()
                                , sce.getOptions().get(0).getAsString())).queue();
                    } else {
                        sce.reply("You are not fighting in this battle").setEphemeral(true).queue();
                    }
                }
            }
            passes = 0;
            waiter();
        }, 1, TimeUnit.MINUTES, () -> { // should be 1 minute
            switch (turnType) {
                case Attack -> {
                    if (passes > battles.size() * 2) {
                        sendMessage("The battle is over due to inactivity");
                        end();
                        return;
                    }
                    sendMessage("You took to much time to respond and the turn have pass!");
                    nextTurn();
                    passes++;
                }
                case Defence -> {
                    defender.setAttack(new Attack(Attack.ATTACK_TYPE.Charge));
                    calculateTurn();
                }
            }
            waiter();
        });
    }

    public String turn(long fighterId, long targetId, String action) {
        Fighter fighter = turnType == TurnType.Attack ? attacker : defender;

        if (fighter.getUserID() != fighterId)
            return "It's not your turn!";

        if (turnType == TurnType.Attack && action.equals("Defence")) {
            return "You can't defence in the attack phase";
        }
        if (turnType == TurnType.Attack && action.equals("Charge")) {
            updateResult(attacker.setAttack(new Attack(Attack.ATTACK_TYPE.Charge)));
            nextTurn();
            return "You charged your energy!";
        }
        if (targetId != 0 && turnType == TurnType.Defence) {
            return "You can't set target on the defence phase";
        }
        if (targetId != 0 && !fightersMap.containsKey(targetId)) {
            return "this target is not in this battle!";
        } else if (targetId != 0)
            fighter.setTarget(fightersMap.get(targetId));

        try {
            Attack attack = new Attack(action);
            if (!fighter.getSpecialAttacks().contains(attack.getAbbreviated()))
                return "You don't have this attack!";
            if (attack.getKiConsumption() > fighter.getKi())
                return "You don't have enough ki to use this attack!";
            Gif gif = fighter.setAttack(attack);
            if (gif != null)
                sendGif(gif.getLink());
            if (turnType == TurnType.Attack) {
                turnType = TurnType.Defence;
                return "You've been attacked! <@" + fighter.getTarget().getUserID() + ">\nWhat will you do?";
            }

            return calculateTurn();

        } catch (NameNotFoundException e) {
            return "This attack doesn't exists";
        } catch (SQLException e) {
            Android24.logError(e);
            return "Some error has accrue";
        }
    }

    private void nextTurn() {
        attackOrder.add(attacker);
        attacker = attackOrder.poll();
        defender = attacker.getTarget();
        sendMessage("This is your turn " + attacker.getName());
    }

    @NotNull
    private String calculateTurn() {

        Gif gif = null;
        switch (attacker.getAttack().getAttackType().toString() + defender.getAttack().getAttackType().toString()) {
            case "StrikeStrike" -> gif = StrikeStrike(attacker, defender);
            case "StrikeKi" -> gif = StrikeKi(attacker, defender);
            case "KiStrike" -> gif = StrikeKi(defender, attacker);
            case "StrikeDefence" -> gif = StrikeDefence(attacker, defender);
            case "KiKi" -> gif = KiKi(attacker, defender);
            case "KiDefence" -> gif = KiDefence(attacker, defender);
            case "StrikeCharge" -> gif = StrikeCharge(attacker, defender);
            case "KiCharge" -> gif = KiCharge(attacker, defender);
        }
        sendMessage(attacker.currentStats());
        sendMessage(defender.currentStats());
        if (attacker.getHealth() <= 0 && defender.getHealth() <= 0)
            sendMessage("Tai!");
        else if (attacker.getHealth() <= 0)
            sendMessage(defender.getName() + " Wins!");
        else if (defender.getHealth() <= 0)
            sendMessage(attacker.getName() + " Wins!");
        if (attacker.getHealth() <= 0 || defender.getHealth() <= 0) {
            end();
            return "Battle over";
        }
        turnType = TurnType.Attack;
        nextTurn();
        updateResult(gif);
        return "Turn ended";
    }

    private Gif StrikeStrike(@NotNull Fighter attacker, @NotNull Fighter defender) {
        attacker.randomizeStats();
        defender.randomizeStats();
        double speedDiff = attacker.getSpeed() / (defender.getSpeed() + 0.0);
        long damage;
        log(attacker, defender, "Speed diff", speedDiff);
        if (speedDiff > 2) {
            damage = (int) (attacker.getStrikeAttack() - defender.getDefence() * 0.2);
            log("to the face");
        } else if (speedDiff > 0.6) {
            damage = defender.defende(attacker.getStrikeAttack());
            switch (rand.nextInt(2)) {
                case 0 -> log("defence");
                case 1 -> {
                    damage = attacker.getStrikeAttack() - defender.getStrikeAttack();
                    if (damage >= 0)
                        defender.takeDamage(damage);
                    else
                        attacker.takeDamage(-1 * damage);
                    log("cross punch");
                    return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                            defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(), 50);
                }
            }
        } else if (speedDiff > 0.3) {
            log("dodged");
            return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                    defender.getRace(), defender.getTransformation().getAbbreviated(), "Defence", -1);
        } else {
            log("counter dodged");
            return StrikeStrike(defender, attacker);
        }
        if (damage < attacker.getStrikeAttack() / -4) {
            log("counter defence");
            return StrikeStrike(defender, attacker);
        }
        log("DMG : " + damage);
        short power = defender.takeDamage(damage); // Remove the health
        log("power : " + power);
        if (damage >= defender.getHealth())
            log("you are ded!");
        else log("not dead");

        return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(), power);
    }

    public Gif StrikeKi(@NotNull Fighter attacker, @NotNull Fighter defender) {
        attacker.randomizeStats();
        defender.randomizeStats();
        double speedDiff = attacker.getSpeed() / (defender.getSpeed() + 0.0);
        long damage;
        log(attacker, defender, "Speed diff", speedDiff);

        if (speedDiff > 2) {
            damage = (int) (attacker.getStrikeAttack() - defender.getDefence() * 0.2);
            log("attack punched defender");
            log(damage);
            if (damage < attacker.getStrikeAttack() / -4) {
                log("counter defence for the blaster");
                return StrikeStrike(defender, attacker);
            }
            if (damage >= defender.getHealth()) {
                log("you are ded!");
            } else log("not dead");
            return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                    defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(), defender.takeDamage(damage));
        } else if (speedDiff > 0.6) {
            log("Defender blast attacker but there was defence");
            damage = defender.defende(attacker.getKiAttack());
        } else {
            log("Defender blast attacker in the face");
            damage = (int) (defender.getKiAttack() - attacker.getDefence() * 0.2);
        }
        if (damage < attacker.getStrikeAttack() / -6) {
            log("counter defence for the strike");
            return StrikeStrike(attacker, defender);
        }
        log(damage);
        if (damage >= attacker.getHealth()) {
            log("you are ded!");
        } else log("not dead");
        return ResultGif.getResultGif(defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(),
                attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(), attacker.takeDamage(damage));
    }

    public Gif StrikeDefence(@NotNull Fighter attacker, @NotNull Fighter defender) {
        log("SD");
        attacker.randomizeStats();
        defender.randomizeStats();
        double speedDiff = attacker.getSpeed() / (defender.getSpeed() + 0.0);
        long damage;
        log(attacker, defender, "Speed diff", speedDiff);

        if (speedDiff > 3) {
            log("to the face");
            damage = (int) (attacker.getStrikeAttack() - defender.getDefence() * 0.4);
        } else if (speedDiff > 0.7) {
            damage = defender.defende(attacker.getStrikeAttack());
        } else if (speedDiff > 0.5) {
            log("dodge");
            return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                    defender.getRace(), defender.getTransformation().getAbbreviated(), "Defence", -1);
        } else {
            log("counter dodged");
            return StrikeStrike(defender, attacker);
        }
        if (damage < attacker.getStrikeAttack() / -4) {
            log("counter defence");
            return StrikeStrike(defender, attacker);
        }
        log("DMG : " + damage);
        Gif gif = ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(), defender.takeDamage(damage));
        if (gif == null)
            gif = ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                    defender.getRace(), defender.getTransformation().getAbbreviated(), "Strike", defender.takeDamage(damage));
        if (damage >= defender.getHealth())
            log("you are ded!");
        else log("not dead");
        return gif;
    }

    public Gif KiKi(@NotNull Fighter attacker, @NotNull Fighter defender) {
        attacker.randomizeStats();
        defender.randomizeStats();
        double powerDiff = attacker.getKiAttack() / (defender.getKiAttack() + 0.0);
        long damage;
        log(attacker, defender, "Power diff", powerDiff);

        damage = (int) (attacker.getKiAttack() - defender.getDefence() * 0.2);
        log(damage);
        if (powerDiff > 1.5) {
            log("blow in the defender face");
            if (damage >= defender.getHealth())
                log("you are ded!");
            else log("not dead");
            return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                    defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(), defender.takeDamage(damage));
        } else if (powerDiff < 0.5) {
            log("blow in the attacker face");
            if (damage >= attacker.getHealth())
                log("you are ded!");
            else log("not dead");
            return ResultGif.getResultGif(defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(),
                    attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(), attacker.takeDamage(damage));
        } else
            return ExpGif.getGif();
    }

    public Gif KiDefence(@NotNull Fighter attacker, @NotNull Fighter defender) {
        attacker.randomizeStats();
        defender.randomizeStats();
        double speedDiff = attacker.getSpeed() / (defender.getSpeed() + 0.0);
        long damage;
        log(attacker, defender, "Speed diff", speedDiff);
        if (speedDiff > 4) {
            log("to the face");
            damage = (int) (attacker.getKiAttack() - defender.getDefence() * 0.4);
        } else if (speedDiff > 0.5) {
            log("defence");
            damage = defender.defende(attacker.getKiAttack());
        } else {
            log("dodge");
            return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                    defender.getRace(), defender.getTransformation().getAbbreviated(), "Defence", -1);
        }
        log(damage);
        if (damage >= defender.getHealth())
            log("you are ded!");
        else log("not dead");
        return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(), defender.takeDamage(damage));
    }

    public Gif StrikeCharge(@NotNull Fighter attacker, @NotNull Fighter defender) {
        attacker.randomizeStats();
        defender.randomizeStats();
        int damage = (int) (attacker.getStrikeAttack() - defender.getDefence() * 0.2);
        log(attacker, defender, "damage", damage);
        if (damage >= defender.getHealth())
            log("you are ded!");
        else log("not dead");
        return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(), defender.takeDamage(damage));

    }

    public Gif KiCharge(@NotNull Fighter attacker, @NotNull Fighter defender) {
        attacker.randomizeStats();
        defender.randomizeStats();
        int damage = (int) (attacker.getKiAttack() - defender.getDefence() * 0.2);
        log(attacker, defender, "damage", damage);
        if (damage >= defender.getHealth())
            log("you are ded!");
        else log("not dead");

        return ResultGif.getResultGif(attacker.getRace(), attacker.getTransformation().getAbbreviated(), attacker.getAttack().getAbbreviated(),
                defender.getRace(), defender.getTransformation().getAbbreviated(), defender.getAttack().getAbbreviated(), defender.takeDamage(damage));
    }

    private void end() {
        battles.remove(channelId);
        Fighter.removeFighters(fightersMap.keySet());
        Android24.jda.getTextChannelById(channelId).delete().queueAfter(60, TimeUnit.SECONDS); // should be 60 seconds
    }

    private void sendMessage(String message) {
        Android24.jda.getTextChannelById(channelId).sendMessage(message).queue();
    }

    private void sendMessage(MessageEmbed embed) {
        Android24.jda.getTextChannelById(channelId).sendMessageEmbeds(embed).queue();
    }

    private void sendGif(@NotNull String url) {
        Android24.jda.getTextChannelById(channelId).sendMessage(url).queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
    }

    private void sendGif(@Nullable Gif gif) {
        if (gif != null)
            Android24.jda.getTextChannelById(channelId).sendMessage(gif.getLink()).queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
    }

    private void updateResult(@Nullable Gif gif) {
        if (gif != null)
            Android24.jda.getTextChannelById(channelId).retrieveMessageById(resultEmbedId).queue(message -> {
                if (message != null)
                    message.editMessageEmbeds(new EmbedBuilder().setImage(gif.getLink()).build()).queue();
                else
                    Android24.jda.getTextChannelById(channelId).sendMessageEmbeds(new EmbedBuilder().setImage(gif.getLink()).build()).queue(
                            msg -> resultEmbedId = msg.getIdLong());

            });
    }

    private void log(Object data) {
        log("Log", data);
    }

    private void log(String explanation, Object data) {
        System.out.println(explanation + " : " + data);
//        sendMessage(explanation + " : " + data);
    }

    private void log(Fighter attacker, Fighter defender, String explanation, Object data) {
        System.out.println(attacker);
        System.out.println(defender);
        System.out.println(data);
//        sendMessage("Attacker:\n" + attacker.toString());
//        sendMessage("Defender:\n" + defender.toString());
//        sendMessage(explanation + "\n" + data.toString());
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public HashMap<Long, Fighter> getFightersMap() {
        return fightersMap;
    }

    public Queue<Fighter> getAttackOrder() {
        return attackOrder;
    }

    public void setAttackOrder(Queue<Fighter> attackOrder) {
        this.attackOrder = attackOrder;
    }

    public TurnType getTurnType() {
        return turnType;
    }

    public void setTurnType(TurnType turnType) {
        this.turnType = turnType;
    }

}
