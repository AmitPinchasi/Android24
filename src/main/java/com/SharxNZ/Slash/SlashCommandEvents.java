package com.SharxNZ.Slash;

import com.SharxNZ.Android24;
import com.SharxNZ.Commands.GameCommands.GetStats;
import com.SharxNZ.Commands.GameCommands.Inventory;
import com.SharxNZ.Commands.GameCommands.PowerPoints;
import com.SharxNZ.Commands.Level;
import com.SharxNZ.Game.Attack;
import com.SharxNZ.Game.Being;
import com.SharxNZ.Game.Race;
import com.SharxNZ.Game.Transformation;
import com.SharxNZ.GameFunctions.StartGame;
import com.SharxNZ.Gifs.ActionGif;
import com.SharxNZ.Gifs.Gif;
import com.SharxNZ.Gifs.ResultGif;
import com.SharxNZ.Gifs.TransGif;
import com.SharxNZ.Shop.Shop;
import com.SharxNZ.Utilities.Embeds;
import com.SharxNZ.Utilities.Server;
import com.SharxNZ.Utilities.Utils;
import com.drew.imaging.ImageProcessingException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import javax.naming.NameNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SlashCommandEvents extends ListenerAdapter {

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent slashCommandEvent) {
        // Only accept commands from guilds
        if (slashCommandEvent.getGuild() == null)
            return;

        User user = slashCommandEvent.getUser();
        Guild guild = slashCommandEvent.getGuild();
        long guildID = guild.getIdLong();
        long userID = user.getIdLong();

        //Make sure you are in the game
        if (!slashCommandEvent.getName().equals("start_game") && !Utils.checkInGame(userID)) {
            slashCommandEvent.reply("You are not in the game! Please use the selection menu" +
                            " or use the command `/start_game` to join the game 😁").addActionRow(StartGame.getSelectionMenu())
                    .setEphemeral(true).queue();
            return;
        }

        switch (slashCommandEvent.getName()) {
            case "start_game" -> {
                try {
                    slashCommandEvent.reply(StartGame.startGame(userID,
                                    new Race(slashCommandEvent.getOption("race").getAsString())))
                            .setEphemeral(true).queue();
                } catch (SQLException | NameNotFoundException e) {
                    Android24.logError(e);
                    slashCommandEvent.replyEmbeds(Embeds.errorEmbed()).setEphemeral(true).queue();
                }
            }
            case "echo" -> slashCommandEvent.reply(slashCommandEvent.getOption("content").getAsString()).queue();

            case "ping" -> slashCommandEvent.reply("Pong!").queue();
            //slashCommandEvent.getHook().sendFile()

            case "stats" -> {
                if (!slashCommandEvent.getOptions().isEmpty() && slashCommandEvent.getOptions().get(0).getAsBoolean())
                    slashCommandEvent.replyEmbeds(GetStats.statsEmbed(user)).queue();
                else
                    slashCommandEvent.replyEmbeds(GetStats.statsEmbed(user)).setEphemeral(true).queue();
            }

            case "level" -> slashCommandEvent.deferReply(slashCommandEvent.getOptions().isEmpty() || !slashCommandEvent.getOptions().get(0).getAsBoolean()).queue(
                    interactionHook -> interactionHook.sendFile(Level.returnLevel(guildID, userID, user.getAvatarUrl()), "Level.jpg").queue());

            case "get_power_points" -> {
                PowerPoints.getPPoints().remove(userID);
                boolean ephemeral = slashCommandEvent.getOptionsByName("display").isEmpty() || !slashCommandEvent.getOptionsByName("display").get(0).getAsBoolean();
                boolean refresh = !(slashCommandEvent.getOptionsByName("refresh").isEmpty() || !slashCommandEvent.getOptionsByName("refresh").get(0).getAsBoolean());
                String args = ephemeral + ":" + refresh;
                String buttonID = userID + "#$#" + args;
                // userID#command#ephemeral:refresh
                ArrayList<ActionRow> ppButtons = new ArrayList<>(Arrays.asList(
                        //Raw 1
                        ActionRow.of(Button.secondary(buttonID.replace("$", "Left"), "⬅"),
                                Button.secondary(buttonID.replace("$", "Right"), "➡"),
                                Button.secondary(buttonID.replace("$", "Up"), "⬆"),
                                Button.secondary(buttonID.replace("$", "Down"), "⬇")),
                        //Raw 2
                        ActionRow.of(Button.success(buttonID.replace("$", "Save"), "Save ✅")/*.withEmoji(Emoji.fromEmote())*/,
                                Button.danger(buttonID.replace("$", "Discard"), "Discard ❌"),
                                Button.primary(buttonID.replace("$", "Refresh"), "Refresh 🔄"))
                ));

                PowerPoints powerPoints = PowerPoints.getPowerPoints(userID);
                slashCommandEvent.deferReply().setEphemeral(ephemeral).queue();
                slashCommandEvent.getHook().sendFile(powerPoints.statsImage(), "png.png")
                        .addEmbeds(powerPoints.getPowerPointsEmbed())
                        .addActionRows(ppButtons).queue();

//                if (ephemeral) {
//                    slashCommandEvent.deferReply(true).queue();
//                    slashCommandEvent.getHook()
//                            .sendMessageEmbeds(PowerPoints.getPowerPointsEmbed(
//                                    PowerPoints.getPowerPoints(userID), true, true))
//                            .setEphemeral(true).addActionRows(ppButtons).queue();
//                } else {
//                    PowerPoints powerPoints = PowerPoints.getPowerPoints(userID);
//                    slashCommandEvent.deferReply().queue();
//                    slashCommandEvent.getHook().sendFile(Graphics.statsImage(powerPoints), "png.png")
//                            .addEmbeds(PowerPoints.getPowerPointsEmbed(
//                                    PowerPoints.getPowerPoints(userID), false, true))
//                            .addActionRows(ppButtons).queue();
//                }

            }

            case "shop" -> {
                ArrayList<ActionRow> shpButtons = new ArrayList<>(Arrays.asList(
                        //Raw 1
                        ActionRow.of(Button.primary("sce#left#" + slashCommandEvent.getId(), "⬅"),
                                Button.primary("sce#right#" + slashCommandEvent.getId(), "➡")),
                        //Raw 2
                        ActionRow.of(Button.success("sce#buy#" + slashCommandEvent.getId(), "Buy 💵"))
                ));
                if (slashCommandEvent.getSubcommandName().equals("view")) {
                    if (slashCommandEvent.getOptionsByName("type").isEmpty() || slashCommandEvent.getOption("type").getAsString().equals("List"))
                        switch (slashCommandEvent.getOption("of").getAsString()) {
                            case "Special Attacks" -> slashCommandEvent.replyEmbeds(Shop.attacksShop(userID)).setEphemeral(true).queue();
                            case "Transformations" -> slashCommandEvent.replyEmbeds(Shop.transformationsShop(userID)).setEphemeral(true).queue();
                        }
                    else
                        switch (slashCommandEvent.getOption("of").getAsString()) {
                            case "Special Attacks" -> slashCommandEvent.replyEmbeds(Shop.getAttacksShop(userID).startScrollingEvent(user, slashCommandEvent.getId()))
                                    .addActionRows(shpButtons).setEphemeral(true).queue();
                            case "Transformations" -> slashCommandEvent.replyEmbeds(Shop.getTransformationsShop(userID).startScrollingEvent(user, slashCommandEvent.getId()))
                                    .addActionRows(shpButtons).setEphemeral(true).queue();
                        }
                } else if (slashCommandEvent.getSubcommandName().equals("buy"))
                    slashCommandEvent.replyEmbeds(Shop.shopView(slashCommandEvent.getOption("item").getAsString()))
                            .addActionRow(Button.success("shop#" + slashCommandEvent.getOption("item").getAsString(), "Buy 💵")).setEphemeral(true).queue();
            }

            case "inventory" -> {
                if (slashCommandEvent.getOptionsByName("type").isEmpty() || slashCommandEvent.getOption("type").getAsString().equals("List"))
                    switch (slashCommandEvent.getOption("of").getAsString()) {
                        case "Special Attacks" -> slashCommandEvent.replyEmbeds(Inventory.attacks(user)).setEphemeral(true).queue();
                        case "Transformations" -> slashCommandEvent.replyEmbeds(Inventory.transformations(user)).setEphemeral(true).queue();
                    }
                else
                    try {
                        switch (slashCommandEvent.getOption("of").getAsString()) {
                            case "Special Attacks" -> slashCommandEvent.replyEmbeds(Being.getDisplayAttacks(user.getIdLong()).startScrollingEvent(user, slashCommandEvent.getId())).setEphemeral(true)
                                    .addActionRow(Button.primary("sce#left#" + slashCommandEvent.getId(), "⬅"), Button.primary("sce#right#" + slashCommandEvent.getId(), "➡")).queue();
                            case "Transformations" -> slashCommandEvent.replyEmbeds(Being.getDisplayTransformations(user.getIdLong()).startScrollingEvent(user, slashCommandEvent.getId())).setEphemeral(true)
                                    .addActionRow(Button.primary("sce#left#" + slashCommandEvent.getId(), "⬅"), Button.primary("sce#right#" + slashCommandEvent.getId(), "➡")).queue();
                        }
                    } catch (SQLException throwables) {
                        Android24.logError(throwables);

                    } catch (NameNotFoundException exception) {
                        slashCommandEvent.replyEmbeds(Embeds.errorEmbed("You have no items to display...")).queue();
                    }
            }

            case "transform" -> {
                // If the user owns the transformation
                Being being = new Being(userID);

                if (Transformation.checkTransformation(userID, slashCommandEvent.getOption("name").getAsString())) {
                    guild.retrieveMemberById(userID).queue(member -> { //הצעה לשיפור - ראה Android24
                        try {
                            Transformation newTransformation = new Transformation(slashCommandEvent.getOption("name").getAsString());

                            if (newTransformation.equals(being.getTransformation())) {
                                Gif gif = TransGif.getTransGif(being.getTransformation().getAbbreviated(), newTransformation.getAbbreviated());
                                if (gif == null)
                                    slashCommandEvent.reply("not gifs available for power up...").setEphemeral(true).queue();
                                else
                                    slashCommandEvent.reply(gif.getLink())
                                            .queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(gif.getLength() * 3, TimeUnit.SECONDS));
                                return;
                            }

                            if (being.getTransformation().getName() != null && member.getRoles().stream().anyMatch(role -> role.getName().equals(being.getTransformation().getName())))
                                guild.removeRoleFromMember(userID,
                                        guild.getRolesByName(being.getTransformation().getName(), true).get(0)).queue();

                            if (guild.getRoles().stream().anyMatch(role -> role.getName().equals(newTransformation.getName()))) {
                                guild.addRoleToMember(userID,
                                        guild.getRolesByName(newTransformation.getName(), true).get(0)).queue();
                                Gif gif = TransGif.getTransGif(being.getTransformation().getAbbreviated(), newTransformation.getAbbreviated());
                                being.setTransformation(newTransformation);
                                if (gif == null)
                                    slashCommandEvent.reply("role added").setEphemeral(true).queue();
                                else
                                    slashCommandEvent.reply(gif.getLink())
                                            .queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(gif.getLength() * 3, TimeUnit.SECONDS));

                            } else {
                                guild.createRole().setName(newTransformation.getName())
                                        .setColor(newTransformation.getColor()).queue(role -> {
                                            guild.addRoleToMember(userID,
                                                    guild.getRolesByName(newTransformation.getName(), true).get(0)).queue();
                                            Gif gif = TransGif.getTransGif(being.getTransformation().getAbbreviated(), newTransformation.getAbbreviated());
                                            being.setTransformation(newTransformation);
                                            if (gif == null)
                                                slashCommandEvent.reply("role added").setEphemeral(true).queue();
                                            else
                                                slashCommandEvent.reply(gif.getLink())
                                                        .queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(gif.getLength() * 3, TimeUnit.SECONDS));
                                            Server server = new Server(guildID);
                                            try {
                                                guild.modifyRolePositions().selectPosition(role).moveTo(
                                                        guild.getRoleById(server.getTransRole()).getPosition() - 1).queue();
                                            } catch (NullPointerException nullPointerException) {
                                                Utils.notifyProblem(guild, server, "You didn't have a role for the transformations 😱.\n" +
                                                        "Plz set one to prevent error and for best experience :-)");
                                            }
                                        });
                            }
                        } catch (NameNotFoundException exception) {
                            slashCommandEvent.reply("This transformations doesn't exists.").setEphemeral(true).queue();
                        } catch (SQLException throwables) {
                            Android24.logError(throwables);
                            slashCommandEvent.replyEmbeds(Embeds.errorEmbed()).setEphemeral(true).queue();
                        }
                    });

                } else if (slashCommandEvent.getOption("name").getAsString().equalsIgnoreCase("base")) {
                    if (being.getTransformation().getName() != null) {
                        guild.retrieveMemberById(userID).queue(member -> {
                            if (member.getRoles().stream().anyMatch(role -> role.getName().equals(being.getTransformation().getName())))
                                guild.removeRoleFromMember(userID,
                                        guild.getRolesByName(being.getTransformation().getName(), true).get(0)).queue();
                            Gif gif = TransGif.getTransGif(being.getTransformation().getAbbreviated(), null);
                            try {
                                being.setTransformation(new Transformation("base"));
                            } catch (NameNotFoundException | SQLException e) {
                                Android24.logError(e);
                            }
                            if (gif == null)
                                slashCommandEvent.reply("You have reverted back").setEphemeral(true).queue();
                            else
                                slashCommandEvent.reply(gif.getLink())
                                        .queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(gif.getLength() * 3, TimeUnit.SECONDS));
                        });
                    } else
                        slashCommandEvent.reply("you're already in base").setEphemeral(true).queue();
                } else
                    slashCommandEvent.reply("You don't have this transformation...").setEphemeral(true).queue();
            }

            case "server_setup" -> guild.retrieveMemberById(userID).queue(member -> {
                if (!member.hasPermission(Permission.MANAGE_PERMISSIONS)) {
                    slashCommandEvent.reply("You have to permission to use this command...").setEphemeral(true).queue();
                    return;
                }
                Server server = new Server(slashCommandEvent.getGuild().getIdLong());
                long comCh = !slashCommandEvent.getOptionsByName("cmd_channel").isEmpty() ? slashCommandEvent.getOption("cmd_channel").getAsLong() : server.getCommandsCh();
                long wlcCh = !slashCommandEvent.getOptionsByName("wlc_channel").isEmpty() ? slashCommandEvent.getOption("wlc_channel").getAsLong() : server.getWelcomeCh();
                long battleCt = !slashCommandEvent.getOptionsByName("battles_category").isEmpty() ? slashCommandEvent.getOption("battles_category").getAsLong() : server.getBattlesCt();
                long logCh = !slashCommandEvent.getOptionsByName("logg_channel").isEmpty() ? slashCommandEvent.getOption("logg_channel").getAsLong() : server.getLoggingCh();
                long transRl = !slashCommandEvent.getOptionsByName("trans_role").isEmpty() ? slashCommandEvent.getOption("trans_role").getAsLong() : server.getTransRole();
                boolean allowTrsGif = !slashCommandEvent.getOptionsByName("allow_trans_gif").isEmpty() ? slashCommandEvent.getOption("allow_trans_gif").getAsBoolean() : server.isAllowTransGif();
                server.setCommandsCh(comCh);
                server.setWelcomeCh(wlcCh);
                server.setBattlesCt(battleCt);
                server.setLoggingCh(logCh);
                server.setTransRole(transRl);
                server.setAllowTransGif(allowTrsGif);

                server.setServer();
                slashCommandEvent.reply(
                        "Command channel: " + (guild.getTextChannelById(server.getCommandsCh()) != null ? guild.getTextChannelById(server.getCommandsCh()).getAsMention() : "`null`") +
                                "\nWelcome channel: " + (guild.getTextChannelById(server.getWelcomeCh()) != null ? guild.getTextChannelById(server.getWelcomeCh()).getAsMention() : "`null`") +
                                "\nBattle category: " + (guild.getCategoryById(server.getBattlesCt()) != null ? guild.getCategoryById(server.getBattlesCt()).getAsMention() : "`null`") +
                                "\nLogging channel: " + (guild.getTextChannelById(server.getLoggingCh()) != null ? guild.getTextChannelById(server.getLoggingCh()).getAsMention() : "`null`") +
                                "\nTransformations role: " + (guild.getRoleById(server.getTransRole()) != null ? guild.getRoleById(server.getTransRole()).getAsMention() : "`null`") +
                                "\nAllow transformations gif globally: `" + server.isAllowTransGif() + "`"
                ).setEphemeral(true).queue();
            });

            case "add_gif" -> {
                slashCommandEvent.deferReply().setEphemeral(true).queue();
                switch (slashCommandEvent.getSubcommandName()) {
                    case "transformation" -> {
                        try {
                            new TransGif( // שיפורים לכל אלו: לעשות שזה יחזיר רק את מה שצריך. לא צריך כל פעם ליצור אובייקט חדש, מספיק לקבל את מה שצריך ולעשות את זה יותר יעיל
                                    new Race(slashCommandEvent.getOption("race").getAsString()),
                                    new Transformation(slashCommandEvent.getOption("from").getAsString()),
                                    new Transformation(slashCommandEvent.getOption("to").getAsString()),
                                    slashCommandEvent.getOption("link").getAsString())
                                    .checkGif(user, slashCommandEvent.getId());
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.successEmbed("The gif has sent do test and will wait for approval!")).setEphemeral(true).queue();

                        } catch (NameNotFoundException e) {
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed("The race or transformation you have choose is not valid!")).setEphemeral(true).queue();
                        } catch (ImageProcessingException | IOException e) {
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed("The gif you have choose is not supported 😮. Please choose another one or download and upload the gif and use the command: `!addTransGif`")).setEphemeral(true).queue();
                        } catch (SQLException throwables) {
                            Android24.logError(throwables);

                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed()).setEphemeral(true).queue();
                        }
                    }
                    case "action" -> {
                        try {
                            new ActionGif( // שיפורים לכל אלו: לעשות שזה יחזיר רק את מה שצריך. לא צריך כל פעם ליצור אובייקט חדש, מספיק לקבל את מה שצריך ולעשות את זה יותר יעיל
                                    slashCommandEvent.getOption("race").getAsString().equals("null") ? null :
                                            new Race(slashCommandEvent.getOption("race").getAsString()),
                                    new Transformation(slashCommandEvent.getOption("transformation").getAsString()),
                                    new Attack(slashCommandEvent.getOption("attack").getAsString()),
                                    slashCommandEvent.getOption("link").getAsString())
                                    .checkGif(user, slashCommandEvent.getId());
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.successEmbed("The gif has sent do test and will wait for approval!")).setEphemeral(true).queue();

                        } catch (NameNotFoundException e) {
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed("The race, transformation or attack you have choose is not valid!")).setEphemeral(true).queue();
                        } catch (ImageProcessingException | IOException e) {
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed("The gif you have choose is not supported 😮. Please choose another one or download and upload the gif and use the command: ")).setEphemeral(true).queue();
                        } catch (SQLException throwables) {
                            Android24.logError(throwables);

                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed()).setEphemeral(true).queue();
                        }
                    }
                    case "result" -> {
                        try {
                            new ResultGif( // שיפורים לכל אלו: לעשות שזה יחזיר רק את מה שצריך. לא צריך כל פעם ליצור אובייקט חדש, מספיק לקבל את מה שצריך ולעשות את זה יותר יעיל
                                    slashCommandEvent.getOption("a_race").getAsString().equals("null") ? null :
                                            new Race(slashCommandEvent.getOption("a_race").getAsString()),
                                    new Transformation(slashCommandEvent.getOption("a_transformation").getAsString()),
                                    new Attack(slashCommandEvent.getOption("a_attack").getAsString()),
                                    slashCommandEvent.getOption("d_race").getAsString().equals("null") ? null :
                                            new Race(slashCommandEvent.getOption("d_race").getAsString()),
                                    new Transformation(slashCommandEvent.getOption("d_transformation").getAsString()),
                                    slashCommandEvent.getOption("d_attack").getAsString().equals("null") ? null :
                                            new Attack(slashCommandEvent.getOption("d_attack").getAsString()),
                                    slashCommandEvent.getOption("power").getAsLong(),
                                    slashCommandEvent.getOption("link").getAsString())
                                    .checkGif(user, slashCommandEvent.getId());
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.successEmbed("The gif has sent do test and will wait for approval!")).setEphemeral(true).queue();

                        } catch (NameNotFoundException e) {
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed("The race, transformation or attack you have choose is not valid!")).setEphemeral(true).queue();
                        } catch (ImageProcessingException | IOException e) {
                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed("The gif you have choose is not supported 😮. Please choose another one or download and upload the gif and use the command: ")).setEphemeral(true).queue();
                        } catch (SQLException throwables) {
                            Android24.logError(throwables);

                            slashCommandEvent.getHook().sendMessageEmbeds(Embeds.errorEmbed()).setEphemeral(true).queue();
                        }
                    }
                }
            }

            case "battle" -> {
                String args = userID + ":";
                String buttonID = "battle#" + slashCommandEvent.getSubcommandName() + "#$#" + args;
                // battle#command#button#userID1:userID2(optional)
                switch (slashCommandEvent.getSubcommandName()) {
                    case "pvp" -> {
                        if (slashCommandEvent.getOption("user") == null) {
                            slashCommandEvent.reply(user.getAsMention() + " is looking for battle!")
                                    .addActionRow(Button.success(buttonID.replace("$", "fight"), "fight").withEmoji(Emoji.fromEmote("battle", 890903425678704640L, true))).queue();
                        } else {
                            User targetUser = slashCommandEvent.getOption("user").getAsUser();
                            buttonID += targetUser.getId();
                            slashCommandEvent.reply(targetUser.getAsMention() + ", \n" +
                                            user.getAsMention() + " is challenging you to a fight!")
                                    .addActionRow(Button.success(buttonID.replace("$", "ufight"), "accept the challenge"), Button.danger(buttonID.replace("$", "udecline"), "decline")).queue();
                        }
                    }
                }
            }

            case "minecraft" -> slashCommandEvent.reply("The IP is: `nahidk.apexmc.co`").queue();

            case "nuke" -> {
                slashCommandEvent.reply("""
                        **You got busted and reported to the admin!**
                        https://tenor.com/view/f-bi-raid-swat-gif-11500735
                        https://tenor.com/view/busted-police-unmarked-undercove-gif-20202846""").setEphemeral(true).queue();
                slashCommandEvent.getJDA().retrieveUserById(303807596555534337L).queue(bob -> {
                    slashCommandEvent.getJDA().getTextChannelById(790508049222729739L).sendMessage(
                            slashCommandEvent.getUser().getAsMention() + " tried to nuke the server! 😱\n"
                                    + bob.getAsMention()).queue();
                });
            }

            case "test" -> {
                slashCommandEvent.reply(Android24.getCommitName()).queue();
            }
            //slashCommandEvent.getHook().sendFile().addEmbeds().queue();
            default -> slashCommandEvent.reply("Unregistered command :" + slashCommandEvent.getName() + " | " + slashCommandEvent.getSubcommandName()
                    + " ~ " + slashCommandEvent.getSubcommandGroup()).queue();
        }
    }
}
