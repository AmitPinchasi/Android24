package com.SharxNZ.Commands.ModeretionCommands;

import com.SharxNZ.Android24;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RefreshNames extends Command {


    public RefreshNames() {
        super.name = "Refresh name";
        super.aliases = new String[]{"rn", "RN"};
        super.arguments = "[n\\s]";
        super.help = "Refresh all the names in the DataBase. (n - users names, s - servers names)";
        super.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement getNames = con.prepareStatement("SELECT UserID FROM `android24`.users_data limit ?, 10;");
                PreparedStatement getSNames = con.prepareStatement("SELECT GuildID FROM guilds.guilds_data limit ?, 10;");
        ) {
            final short pace = 10;
            if (commandEvent.getArgs().isEmpty() || commandEvent.getArgs().equalsIgnoreCase("n")) {
                int limit = 0;
                int check;
                do {
                    check = pace;
                    getNames.setInt(1, limit);
                    ResultSet result = getNames.executeQuery();
                    for (int i = 0; i < pace; i++) {
                        if (result.next()) {
                            long id = result.getLong(1);
                            Android24.jda.retrieveUserById(id).queue(user -> {
                                try (
                                        Connection con2 = Android24.getConnection();
                                        PreparedStatement setNames = con2.prepareStatement("UPDATE `android24`.`users_data` SET `UserName` = ? WHERE `UserID` = ?;")
                                ) {
                                    setNames.setString(1, user.getAsTag());
                                    setNames.setLong(2, id);
                                    setNames.executeUpdate();
                                } catch (SQLException throwables) {
                                    System.out.println("Inner try");
                                    Android24.logError(throwables);
                                }
                                Android24.log("Changing name to -> {" + user.getAsMention() + ", name: " + user.getName() + "}");
                            });
                            check--;
                        } else {
                            break;
                        }
                    }
                    limit += pace;
                    result.close();
                } while (check == 0);
                commandEvent.reply("Name refreshing finished");
                getNames.close();
            } else if (commandEvent.getArgs().equalsIgnoreCase("s")) {
                int limit = 0;
                int check;
                do {
                    check = pace;
                    getSNames.setInt(1, limit);
                    ResultSet result = getSNames.executeQuery();
                    for (int i = 0; i < pace; i++) {
                        if (result.next()) {
                            long id = result.getLong(1);
                            String name = Android24.jda.getGuildById(id).getName();
                            try (
                                    Connection con2 = Android24.getConnection();
                                    PreparedStatement setSNames = con2.prepareStatement("UPDATE guilds.`guilds_data` SET `GuildName` = ? WHERE `GuildID` = ?;")
                            ) {
                                setSNames.setString(1, name);
                                setSNames.setLong(2, id);
                                setSNames.executeUpdate();
                                check--;
                            } catch (SQLException throwables) {
                                System.out.println("Inner try " + name);
                                Android24.logError(throwables);
                            }
                            Android24.log("Changing name to -> { id: " + id + ", name: " + name + "}");
                        } else {
                            break;
                        }
                    }
                    limit += pace;
                    result.close();
                } while (check == 0);
                commandEvent.reply("Name refreshing finished");
                getSNames.close();
            }
        } catch (SQLException | NullPointerException throwables) {
            System.out.println("Outer try");
            Android24.logError(throwables);
        }

    }
}
