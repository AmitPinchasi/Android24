package com.SharxNZ.Commands.ModeretionCommands;

import com.SharxNZ.Android24;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CleanDatabase extends Command {

    public CleanDatabase() {
        super.name = "cleanDatabase";
        super.aliases = new String[]{"cld"};
        super.help = "Removing all the users that not in any of the servers that the bot is in";
        super.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
        super.ownerCommand = true;
        super.hidden = true;
    }

    @Override
    public void execute(@NotNull CommandEvent commandEvent) {

        HashSet<Member> allMembers = new HashSet<>();
        HashSet<Long> allMembersIds = new HashSet<>();
        List<Guild> guilds = commandEvent.getJDA().getGuilds();
        for (int i = 0; i < guilds.size(); i++) {
            if ((i + 1) < guilds.size())
                guilds.get(i).loadMembers().onSuccess(allMembers::addAll); // collecting all the users
            else { // on the last guild
                guilds.get(i).loadMembers().onSuccess(members -> {
                    allMembers.addAll(members);
                    allMembers.forEach(member -> allMembersIds.add(member.getIdLong()));
                    try (
                            Connection con = Android24.getConnection();
                            PreparedStatement getNames = con.prepareStatement("SELECT UserID FROM `android24`.users_data limit ?, 10;");
                            PreparedStatement setNames = con.prepareStatement("DELETE FROM `android24`.`users_data` WHERE `UserID` = ?;")

                    ) {
                        final short pace = 10;
                        int limit = 0;
                        int check;
                        do {
                            check = pace;
                            getNames.setInt(1, limit);
                            ResultSet result = getNames.executeQuery();
                            for (int j = 0; j < pace; j++) {
                                if (result.next()) {
                                    long id = result.getLong(1);
                                    if (!allMembersIds.contains(id)) {
                                        setNames.setLong(1, id);
                                        setNames.executeUpdate();
                                        Android24.jda.retrieveUserById(id).queue(user -> Android24.log("Deleting {" + user.getAsMention() + ", name: " + user.getName() + "}"));
                                    }
                                    check--;
                                } else {
                                    break;
                                }
                            }
                            limit += pace;
                            result.close();
                        } while (check == 0);
                        commandEvent.reply("Finished cleaning the database");
                    } catch (SQLException throwables) {
                        Android24.logError(throwables);
                    }
                });
            }
        }
    }
}
