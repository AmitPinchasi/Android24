package com.SharxNZ.Commands.GameCommands;

import com.SharxNZ.Android24;
import com.SharxNZ.Game.Race;
import com.SharxNZ.GameFunctions.StartGame;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class StartGameCommand extends Command {

    public StartGameCommand() {
        super.name = "startGame";
        super.aliases = new String[]{"sg", "s"};
        super.help = "Start the game";
        super.arguments = "[Your type]";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply("use the `/start_game`");
    }
}
