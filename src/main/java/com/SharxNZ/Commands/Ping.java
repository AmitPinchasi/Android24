package com.SharxNZ.Commands;

import com.SharxNZ.Android24;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public class Ping extends ListenerAdapter {
    public Ping(){
        Android24.addCommands(new CommandData("ping", "pong you"));
    }
    String message;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent m) {
        if (m.getMessage().getContentRaw().startsWith(Android24.prefix)) {
            message = m.getMessage().getContentRaw().substring(1).toLowerCase();

            if (message.equals("ping")) {
                m.getChannel().sendTyping().queue();
                m.getChannel().sendMessage("pong"/* + m.getGuild().getDefaultChannel()*/).queue();
            }
        }
    }
}

