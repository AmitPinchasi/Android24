package com.SharxNZ.Slash;

//compileJava.options.encoding = 'UTF-8';
import com.SharxNZ.Android24;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;

import static com.SharxNZ.Android24.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;



public class SlashTest extends ListenerAdapter {
    public SlashTest(){
        // Moderation commands with required options
        Android24.addCommands(
                new CommandData("ban", "Ban a user from this server. Requires permission to Ban users.")
                        .addOptions(new OptionData(USER, "user", "The user to ban") // USER type allows to include members of the server or other users by id
                                .setRequired(true)) // This command requires a parameter
                        .addOptions(new OptionData(INTEGER, "del_days", "Delete messages from the past days.")) // This is optional
        );

        // Simple reply commands
        Android24.addCommands(
                new CommandData("say", "Makes the bot say what you tell it to")
                        .addOptions(new OptionData(STRING, "content", "What the bot should say")
                                .setRequired(true))
        );

        // Commands without any inputs
        Android24.addCommands(
                new CommandData("leave", "Make the bot leave the server")
        );

        Android24.addCommands(
                new CommandData("prune", "Prune messages from this channel")
                        .addOptions(new OptionData(INTEGER, "amount", "How many messages to prune (Default 100)"))
        );

        // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
        // commandListDebug.queue();
        //commandListUpdateAction.queue(list -> list.forEach(command -> System.out.println(command.getId())));

    }

    @Override
    public void onSlashCommand(SlashCommandEvent event)
    {
        // Only accept commands from guilds
        if (event.getGuild() == null)
            return;
        switch (event.getName()) {
            case "ban" -> {
                Member member = event.getOption("user").getAsMember(); // the "user" option is required so it doesn't need a null-check here
                User user = event.getOption("user").getAsUser();
                ban(event, user, member);
            }
            case "say" -> say(event, event.getOption("content").getAsString()); // content is required so no null-check here
            case "leave" -> jda.getTextChannelById(debugChannelID).sendMessage("?????? ????????").queue();

            //leave(event);
            case "prune" -> // 2 stage command with a button prompt
                    prune(event);
            default -> event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    public void say(SlashCommandEvent event, String content)
    {
        event.reply(content).queue(); // This requires no permissions!
        event.getChannel().sendMessage(content).queue(); // This requires no permissions!
        //event.reply(content).setEphemeral(true).queue(); // This requires no permissions!
    }

    public void prune(SlashCommandEvent event)
    {
        OptionMapping amountOption = event.getOption("amount"); // This is configured to be optional so check for null
        int amount = amountOption == null
                ? 100 // default 100
                : (int) Math.min(200, Math.max(2, amountOption.getAsLong())); // enforcement: must be between 2-200
        String userId = event.getUser().getId();
        event.reply("This will delete " + amount + " messages.\nAre you sure?") // prompt the user with a button menu
                .addActionRow(// this means "<style>(<id>, <label>)" the id can be spoofed by the user so setup some kinda verification system
                        Button.secondary(userId + ":delete", "Nevermind!"),
                        Button.danger(userId + ":prune:" + amount, "Yes!")) // the first parameter is the component id we use in onButtonClick above
                .queue();
    }

    @Override
    public void onButtonClick(ButtonClickEvent event)
    {
        // users can spoof this id so be careful what you do with this
        String[] id = event.getComponentId().split(":"); // this is the custom id we specified in our button
        String authorId = id[0];
        String type = id[1];
        // When storing state like this is it is highly recommended to do some kind of verification that it was generated by you, for instance a signature or local cache
        if (!authorId.equals(event.getUser().getId()))
            return;
        event.deferEdit().queue(); // acknowledge the button was clicked, otherwise the interaction will fail

        MessageChannel channel = event.getChannel();
        switch (type)
        {
            case "prune":
                int amount = Integer.parseInt(id[2]);
                event.getChannel().getIterableHistory()
                        .skipTo(event.getMessageIdLong())
                        .takeAsync(amount)
                        .thenAccept(channel::purgeMessages);
                // fallthrough delete the prompt message with our buttons
            case "delete":
                    event.getHook().deleteOriginal().queue();
        }
    }
    public void ban(SlashCommandEvent event, User user, Member member)
    {
        event.deferReply(true).queue(); // Let the user know we received the command before doing anything else
        InteractionHook hook = event.getHook(); // This is a special webhook that allows you to send messages without having permissions in the channel and also allows ephemeral messages
        hook.setEphemeral(true); // All messages here will now be ephemeral implicitly
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS))
        {
            hook.sendMessage("You do not have the required permissions to ban users from this server.").queue();
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.BAN_MEMBERS))
        {
            hook.sendMessage("I don't have the required permissions to ban users from this server.").queue();
            return;
        }

        if (member != null && !selfMember.canInteract(member))
        {
            hook.sendMessage("This user is too powerful for me to ban.").queue();
            return;
        }

        int delDays = 0;
        OptionMapping option = event.getOption("del_days");
        if (option != null) // null = not provided
            delDays = (int) Math.max(0, Math.min(7, option.getAsLong()));
        // Ban the user and send a success response
        event.getGuild().ban(user, delDays)
                .flatMap(v -> hook.sendMessage("Banned user " + user.getAsTag()))
                .queue();
    }


}
