package com.SharxNZ.Commands;

import com.SharxNZ.Android24;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public class IP extends Command {

    public IP() {
        super.name = "IP";
        super.aliases = new String[]{"IP", "minecraft"};
        super.help = "The Minecraft server's IP";
        Android24.addCommands(new CommandData("minecraft", "The Minecraft server's IP"));
    }

    @Override
    protected void execute(@NotNull CommandEvent commandEvent) {
        commandEvent.reply("The IP is: `nahidk.apexmc.co`");
    }

}
