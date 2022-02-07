package com.SharxNZ.Commands.ModeretionCommands;

import com.SharxNZ.Android24;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

public class LeaveServer extends Command {

    public LeaveServer() {
        super.name = "leaveServer";
        super.aliases = new String[]{"ls"};
        super.arguments = "[server id]";
        super.help = "Make the bot leave the server you entered";
        super.userPermissions = new Permission[]{Permission.KICK_MEMBERS};
        super.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        try {
            switch (commandEvent.getJDA().getGuildById(commandEvent.getArgs())) {
                case null -> commandEvent.reply("This guild doesn't exist or that the bot isn't in this guild");
                case Guild guild -> {
                    guild.leave().queue();
                    commandEvent.reply("The bot left " + guild.getName() + " Successfully!");
                }
            }
        } catch (Exception e) {
            commandEvent.reply(e.getMessage());
        }
    }
}
