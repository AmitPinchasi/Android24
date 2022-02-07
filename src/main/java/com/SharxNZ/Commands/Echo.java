package com.SharxNZ.Commands;

import com.SharxNZ.Android24;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Echo extends Command {
    
    public Echo(){
        super.name = "echo";
        super.aliases = new String[]{"ec"};
        super.help = "Echo back your world";
        Android24.addCommands(new CommandData("echo", "Echo what you say")
                .addOptions(new OptionData(OptionType.STRING, "content", "the content to echo").setRequired(true)));
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        if(commandEvent.getArgs() != ""){
            commandEvent.getChannel().sendMessage(commandEvent.getArgs()).queue();
            try {
                commandEvent.reply("Go to sleep"/* + m.getGuild().getDefaultChannel()*/);
                Thread.sleep(20000l);
                commandEvent.reply("Awoken"/* + m.getGuild().getDefaultChannel()*/);
            } catch (InterruptedException e) {
                Android24.logError(e);
            }
        }
        else {
            commandEvent.getChannel().sendMessage("The syntax is: `!echo <args>`").queue();
        }
    }

}
//public class PingCommand extends Marco {
//
//    public PingCommand(){
//        this.name = "ping";
//        this.help = "reports latency to the API";
//        this.guildOnly = false;
//    }
//
//    @Override
//    protected void execute(CommandEvent event) {

//JDA jda = event.getJDA();

/*
public class temp extends ListenerAdapter{
    public void onMessageReceived(MessageReceivedEvent messageReceivedEvent){

        EmbedBuilder eb = new EmbedBuilder(); eb.setColor(new Color(255, 0, 54));
        eb.setDescription("bruh");
        messageReceivedEvent.getTextChannel().sendMessage(eb.build()).queue(m -> {

                    JDA jda = messageReceivedEvent.getJDA();

                    jda.getRestPing().queue(
                            (ping) -> {
                                //long ping = event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS);
                                EmbedBuilder embedBuilder = new EmbedBuilder();
                                embedBuilder.setColor(new Color(255,0,54));
                                embedBuilder.setDescription("Gateaway Ping: "+jda.getGatewayPing()+"ms\nRest Ping : "+ping+"ms");
                                m.editMessage(embedBuilder.build()).queue();

                            });
                }
        );
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

public void startAPIAbuse() {
  scheduler.scheduleAtFixedRate(() -> {
    //Abuse here
  }, 0, 10, TimeUnit.SECONDS);
}
}*/
