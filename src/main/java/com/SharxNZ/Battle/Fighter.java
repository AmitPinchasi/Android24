package com.SharxNZ.Battle;

import com.SharxNZ.Android24;
import com.SharxNZ.Commands.GameCommands.Stats;
import com.SharxNZ.Game.Attack;
import com.SharxNZ.Gifs.ActionGif;
import com.SharxNZ.Gifs.Gif;
import com.SharxNZ.Gifs.TransGif;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class Fighter extends Stats {

    protected static final Random rand = new Random();
    protected static final HashMap<Long, Fighter> fighters = new HashMap<>();

    protected final ArrayList<String> specialAttacks = new ArrayList<>();
    protected long health = super.health;
    protected long ki = super.ki;
    protected long strikeAttack = super.strikeAttack;
    protected long kiAttack = super.kiAttack;
    protected long defence = super.defence;
    protected long speed = super.speed;
    protected Fighter target;
    protected Attack attack;

    public Fighter(long userID) {
        super(userID, true);
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT AttackAbbreviated FROM android24.users_attacks WHERE UserID = ?;")
        ) {
            statement.setLong(1, userID);
            ResultSet resultSet = statement.executeQuery();
            specialAttacks.add("Strike");
            specialAttacks.add("Ki");
            specialAttacks.add("Defence");
            specialAttacks.add("Charge");
            while (resultSet.next())
                specialAttacks.add(resultSet.getString(1));
            fighters.put(userID, this);
        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }

    @NotNull
    protected static String getStatBar(long part, long full, String chr) {
        part = 10 * (Math.max(part, 0)) / full;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < part; i++) {
            bar.append(chr);
        }
        for (int i = 0; i < 10 - part; i++) {
            bar.append("⬛");
        }
        return bar.toString();
    }

    @Nullable
    public static Fighter getFighter(long id) {
        return fighters.get(id);
    }

    public static void removeFighters(long @NotNull ... ids) {
        for (long id : ids)
            fighters.remove(id);
    }

    public static void removeFighters(@NotNull Set<Long> ids) {
        for (Long id : ids)
            fighters.remove(id);
    }

    protected void resetStats() {
        strikeAttack = super.strikeAttack * transformation.getAttackPowerUp();
        kiAttack = super.kiAttack * transformation.getAttackPowerUp();
        defence = super.defence * transformation.getDefencePowerUp();
        speed = super.speed * transformation.getSpeedPowerUp();
    }

    /**
     * @return power
     */
    public short takeDamage(long damage) {
        if (damage >= 0) {
            health -= damage;
            return (short) Math.max(0, Math.min(100, 100 * damage / (super.health / 4)));
        } else return 0;
    }

    //אולי אפשר לעשות שנשבר המגן אם נגמר הקי
    /**
     * f(x)=b*a^(x)-c <-- **This is working on 0.0 - 1.0 and not on 0% - 100%**
     * g(x)=q*x+ℯ^(w x)-1
     * @return demage
     */
    public long defende(long attack){
        final double a = 1.8;
        final double b = 0.5;
        final double c = 0.5;
        final double x = attack/defence; // The precent of the attack from the defence
        final double multiplier = b * Math.pow(a, x) - c;
        changeKi((int) (-1*attack*multiplier));
        return attack - defence;
    }

    void changeKi(int change){
        ki = Math.max(0, Math.min(super.ki, ki + change));
    }

    public Fighter getTarget() {
        return target;
    }

    public void setTarget(Fighter target) {
        this.target = target;
    }

    public Attack getAttack() {
        return attack;
    }

    @Nullable
    public Gif setAttack(@NotNull Attack attack) {
        resetStats();
        if (attack.getAttackType() != Attack.ATTACK_TYPE.Charge)
            ki -= (long) attack.getKiConsumption() * transformation.getKiConsumption();
        switch (attack.getAttackType()) {
            case Strike -> strikeAttack *= attack.getAttackPowerUp();
            case Ki -> kiAttack *= attack.getAttackPowerUp();
            case Charge -> changeKi((int) (super.ki / 15));

        }
        defence *= attack.getDefencePowerUp();
        speed *= attack.getSpeedPowerUp();
        this.attack = attack;
        if (attack.getAttackType() == Attack.ATTACK_TYPE.Charge)
            return TransGif.getTransGif(transformation.getAbbreviated(), transformation.getAbbreviated());
        else
            return ActionGif.getActionGif(race, transformation.getAbbreviated(), attack.getAbbreviated());
    }

    public void randomizeStats() {
        strikeAttack *= rand.nextDouble() + 1;
        kiAttack *= rand.nextDouble() + 1;
        defence *= rand.nextDouble() + 1;
        speed *= rand.nextDouble() + 1;
    }

    public MessageEmbed currentStats() {
        EmbedBuilder builder = new EmbedBuilder();
        if (transformation.getName() != null) {
            builder.setDescription(transformation.getName());
            builder.setColor(transformation.getColor());
        }
        builder.setTitle(name);
        builder.addField("Health", health + "/" + super.health + "\n" + getStatBar(health, super.health, "🟩"), false);
        builder.addField("Ki", ki + "/" + super.ki + "\n" + getStatBar(ki, super.ki, "🟦"), false);
        return builder.build();
    }

    @Override
    public String toString() {
        return "Fighter{" +
                "name=" + name +
                ", userID=" + userID +
                ", attack=" + attack.getName() +
                ", target=" + target.name +
                ", health=" + health +
                ", ki=" + ki +
                ", strikeAttack=" + strikeAttack +
                ", kiAttack=" + kiAttack +
                ", defence=" + defence +
                ", speed=" + speed +
                ", transformation='" + transformation + '\'' +
                '}';
    }

    public ArrayList<String> getSpecialAttacks() {
        return specialAttacks;
    }

    @Override
    public long getHealth() {
        return health;
    }

    @Override
    public long getKi() {
        return ki;
    }

    @Override
    public long getStrikeAttack() {
        return strikeAttack;
    }

    @Override
    public long getKiAttack() {
        return kiAttack;
    }

    @Override
    public long getDefence() {
        return defence;
    }

    @Override
    public long getSpeed() {
        return speed;
    }

}
