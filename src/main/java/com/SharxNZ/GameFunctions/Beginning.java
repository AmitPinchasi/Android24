package com.SharxNZ.GameFunctions;

import com.SharxNZ.Android24;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Beginning extends ListenerAdapter {

    private static final String joiningMessage = """
            Thanks for inviting my to your server!
            There are some stuff that you should take care of before we start.
            1) You or one of your staff should use the command `/server_setup` to define the server settings.
            2) When the bot joined he created a category called `Bot Battles`. This is the default category for the battles on this server.
            3) When the bot joined he created a role called `---transformations---`. This role will define where all the transformations roles will be positioned.
               Therefore, it is highly recommended to move this role's position up as much as you can so when someone is transformed, his color will change accordingly.
               BTW, you can choose other role to be set the transformations role positions, just use the `/server_setup` 😁
            """;

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent guildJoinEvent) {
        guildJoinEvent.getGuild().getRolesByName("---transformations---", true).forEach(role -> role.delete().queue());
        guildJoinEvent.getGuild().getCategoriesByName("Bot Battles", true).forEach(category -> category.delete().queue());
        guildJoinEvent.getGuild().createCategory("Bot Battles").queue(category ->
                guildJoinEvent.getGuild().createRole().setName("---transformations---")
                        .queue(role -> {
                            try (
                                    Connection con = Android24.getConnection();
                                    PreparedStatement join = con.prepareStatement(
                                            "INSERT INTO `guilds`.`guilds_data` (`GuildID`, `GuildName`, `BattlesCt`, `TransRole`) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `GuildName` = ?, `BattlesCt` = ?, `TransRole` = ?;")
                            ) {

                                join.setLong(1, guildJoinEvent.getGuild().getIdLong());
                                join.setString(2, guildJoinEvent.getGuild().getName());
                                join.setLong(3, category.getIdLong());
                                join.setLong(4, role.getIdLong());
                                join.setString(5, guildJoinEvent.getGuild().getName());
                                join.setLong(6, category.getIdLong());
                                join.setLong(7, role.getIdLong());
                                join.executeUpdate();

                                guildJoinEvent.getGuild().retrieveOwner().queue(owner -> {
                                    owner.getUser().openPrivateChannel().queue(privateChannel -> {
                                        privateChannel.sendMessage(joiningMessage).queue(null, throwable -> {
                                            Android24.nirin.openPrivateChannel().queue(nirinChannel -> {
                                                nirinChannel.sendMessage("The owner of " + guildJoinEvent.getGuild().getName() + "have no private channel!" +
                                                        "\nYou need to do things manually...").queue();
                                            });
                                        });
                                    });
                                });

                            } catch (SQLException throwables) {
                                Android24.logError(throwables);
                            }

                        })
        );
        System.out.println("Joined to " + guildJoinEvent.getGuild().getId());
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent guildLeaveEvent) {
    }
}
