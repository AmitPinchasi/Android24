package com.SharxNZ.Buttons;

import com.SharxNZ.Android24;
import com.SharxNZ.Battle.Battle;
import com.SharxNZ.Battle.Fighter;
import com.SharxNZ.GameFunctions.StartGame;
import com.SharxNZ.Utilities.Embeds;
import com.SharxNZ.Utilities.Server;
import com.SharxNZ.Utilities.Utils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BattleButtons extends ListenerAdapter {


    @Override
    public void onButtonClick(@NotNull ButtonClickEvent buttonClickEvent) {

        // battle#command#button#userID1:userID2(optional)
        // userID1 - the user how requested the battle
        // userID2 - the user how needs to approve the battle (if exists)
        String[] split = buttonClickEvent.getComponentId().split("#");
        if (!split[0].equals("battle"))
            return;

        User clickedUser = buttonClickEvent.getUser();
        Category category = Android24.jda.getCategoryById(new Server(buttonClickEvent.getGuild().getIdLong()).getBattlesCt());

        if (category == null) {
            buttonClickEvent.replyEmbeds(Embeds.errorEmbed("This server hasn't enable battles! Tell the server's admins to set a battles category")).setEphemeral(true).queue();
//            Utils.notifyProblem(buttonClickEvent.getGuild(), "This server doesn't have a category for battles, please use the `/server_setup` command to set one ☺");
            return;
        }

        String command = split[1];
        String button = split[2];
        String[] args = split[3].split(":");

        if (clickedUser.getId().equals(args[0])) {
            buttonClickEvent.replyEmbeds(Embeds.errorEmbed("You can't fight with yourself!")).setEphemeral(true).queue();
            return;
        }

        if (!Utils.checkInGame(clickedUser.getIdLong())) {
            buttonClickEvent.replyEmbeds(Embeds.errorEmbed("You have to be in the game to participate in a battle, use the command `/start_game`!"))
                    .addActionRow(StartGame.getSelectionMenu()).setEphemeral(true).queue();
            return;
        }

        if (Fighter.getFighter(clickedUser.getIdLong()) != null) {
            buttonClickEvent.replyEmbeds(Embeds.errorEmbed("You are already in another battle! You can't join 2 battles at the same time.")).setEphemeral(true).queue();
            return;
        }

        switch (command) {
            case "pvp" -> {
                switch (button) {
                    case "fight" -> Android24.jda.retrieveUserById(args[0]).queue(user -> {
                        new Battle(category, user, clickedUser);
                        buttonClickEvent.editMessage(new MessageBuilder("The battle has started in: " + user.getName() + " vs " + clickedUser.getName()).build()).queue();
                    });
                    case "ufight" -> {
                        if (clickedUser.getId().equals(args[1])) {
                            Android24.jda.retrieveUserById(args[0]).queue(user -> {
                                new Battle(category, user, clickedUser);
                                buttonClickEvent.editMessage(new MessageBuilder("The battle has started in: " + user.getName() + " vs " + clickedUser.getName()).build()).queue();
                            });
                        } else {
                            buttonClickEvent.replyEmbeds(Embeds.errorEmbed("You are not the one how challenged!")).setEphemeral(true).queue();
                        }

                    }
                    case "udecline" -> {
                        if (clickedUser.getId().equals(args[1]))
                            buttonClickEvent.editMessageEmbeds(Embeds.errorEmbed("The request hase been declined...")).queue(interactionHook ->
                                    interactionHook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                        else {
                            buttonClickEvent.replyEmbeds(Embeds.errorEmbed("You are not the one how challenged!")).setEphemeral(true).queue();
                        }

                    }
                }
            }
        }


    }
}
