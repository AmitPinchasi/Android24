package com.SharxNZ.Commands.ModeretionCommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.io.*;

public class SysCommand extends Command {

    public SysCommand(){
        super.name = "system";
        super.aliases = new String[]{"sys"};
        super.arguments = "[command]";
        super.help = "Run system commands on the server";
        super.userPermissions = new Permission[]{Permission.BAN_MEMBERS, Permission.ADMINISTRATOR};
        super.requiredRole = "Can do all";
        super.ownerCommand = false;
        super.hidden = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        final Runtime run = Runtime.getRuntime();

        String cmd = commandEvent.getArgs();

        if(cmd.equals(""))
            return;

        Process process;
        StringBuilder output = new StringBuilder();

        try {
            process = run.exec(cmd);


            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.waitFor();

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0)
                output.insert(0, "Success" + "\n------------\n");
            else
                output.insert(0, "Error" + "\n------------\n");


        } catch (IOException | InterruptedException e) {
            output = new StringBuilder("Exception").append("\n------------\n").append(e.getMessage());
        }

        if (output.length() < 2000)
            commandEvent.getChannel().sendMessage("```\n" + output + "```")
                    .reference(commandEvent.getMessage()).mentionRepliedUser(false).queue();
        else {
            try (
                    FileWriter fw = new FileWriter("log.log")
            ) {

                for (int i = 0; i < output.length(); i++)
                    fw.write(output.charAt(i));

                commandEvent.getChannel().sendFile(new File("log.log"))
                        .reference(commandEvent.getMessage()).mentionRepliedUser(false).queue();

            } catch (IOException e) {
                output = new StringBuilder("Exception").append("\n------------\n").append(e.getMessage());
                commandEvent.getChannel().sendMessage("```\n" + output + "```")
                        .reference(commandEvent.getMessage()).mentionRepliedUser(false).queue();
            }
        }
    }
}
