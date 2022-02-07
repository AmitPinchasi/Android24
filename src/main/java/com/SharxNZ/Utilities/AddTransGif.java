package com.SharxNZ.Utilities;

import com.SharxNZ.Android24;
import com.SharxNZ.Game.Race;
import com.SharxNZ.Game.Transformation;
import com.SharxNZ.Gifs.TransGif;
import com.drew.imaging.ImageProcessingException;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import javax.naming.NameNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class AddTransGif extends Command {

    public AddTransGif() {
        super.name = "addTransGif";
        super.aliases = new String[]{"atg", "addTG"};
        super.arguments = "[race] [from] [to] (you need to add the gif as a file)";
        super.help = "Add the gif as a file and then put all the arguments";
        super.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String[] args = commandEvent.getArgs().split("\\s+");
        if (args.length != 3 || commandEvent.getMessage().getAttachments().isEmpty())
            commandEvent.reply("You haven't put the arguments correctly. The arguments are:\n`" + arguments + "`");
        else {
            try {
                new TransGif(
                        new Race(args[0]),
                        new Transformation(args[1]),
                        new Transformation(args[2]),
                        commandEvent.getMessage().getAttachments().get(0).getUrl())
                        .checkGif(commandEvent.getAuthor(), commandEvent.getMessage().getId());
                commandEvent.reply(Embeds.successEmbed("The gif has sent do test and will wait for approval!"));

            } catch (NameNotFoundException e) {
                commandEvent.reply(Embeds.errorEmbed("The race or transformation you have choose is not valid!"));
            } catch (ImageProcessingException | IOException e) {
                commandEvent.reply(Embeds.errorEmbed("The gif you have choose is not supported 😮. Please choose another one or download and upload the gif and use the command: "));
            } catch (SQLException throwables) {
                Android24.logError(throwables);

                commandEvent.reply(Embeds.errorEmbed());
            }
        }

    }
}
