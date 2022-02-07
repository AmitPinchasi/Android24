package com.SharxNZ.Commands;

import com.SharxNZ.Android24;
import com.SharxNZ.Utilities.Graphics;
import com.SharxNZ.Utilities.Server;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.IOException;

public class Marco extends ListenerAdapter {
    String message;

    //@Override //s
    public void onMessageReceived(@Nonnull MessageReceivedEvent m) {
        if (m.getMessage().getContentRaw().startsWith(Android24.prefix)) {
            message = m.getMessage().getContentRaw().substring(1).toLowerCase();
            if (message.equals("marco")) {

                m.getChannel().sendTyping().queue();
                m.getJDA().getRestPing().queue((ping) -> {
                    m.getChannel().sendMessage("polo " + ping + "ms / " + m.getJDA().getGatewayPing() + "ms").queue();
                });

                System.out.println();

//                try {
//                    TimeUnit.SECONDS.sleep(30);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            } else if (message.equals("-i ")) {
                EmbedBuilder info = new EmbedBuilder();
                info.setTitle("Your Info 🐱‍💻 <:24:729000135362609204>");
                info.setDescription("Your power level is over 9,000!");
                info.addField("Fild 0", "Fild 1", true);
                info.setColor(0xE1F1FA);
                info.setFooter("Your image", m.getAuthor().getAvatarUrl());
                info.setImage(m.getAuthor().getAvatarUrl());

                m.getChannel().sendTyping().queue();
                m.getChannel().sendMessage(info.build()).queue();
                info.clear();


            }

        }
    }

    public void onGuildMemberJoin(GuildMemberJoinEvent guildMemberJoinEvent) {
        System.out.println("Joined");
        Server server = new Server(guildMemberJoinEvent.getGuild().getIdLong());
        if (server.getWelcomeCh() != 0) {
            try {
                guildMemberJoinEvent.getGuild().getTextChannelById(server.getWelcomeCh()).sendMessage(guildMemberJoinEvent.getMember().getAsMention() + " Joined he is cool!").queue();
                guildMemberJoinEvent.getGuild().getTextChannelById(server.getWelcomeCh()).sendFile(Graphics.welcomeImage(guildMemberJoinEvent), "welcome.jpg").queue();
            } catch (IOException e) {
                Android24.logError(e);
            } catch (NullPointerException ignored){

            }
        }
        guildMemberJoinEvent.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("Welcome to my amazing server!").queue(null, throwable -> {});
        });
    }


}
