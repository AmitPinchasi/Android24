package com.SharxNZ.GameFunctions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GFButtons extends ListenerAdapter {
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent buttonClickEvent) {
        String command = buttonClickEvent.getComponentId();
        long userID = buttonClickEvent.getUser().getIdLong();

//        switch (command) {
//            case "saiyan" -> buttonClickEvent.reply(StartGame.startGame(userID, Race.Saiyan)).setEphemeral(true).queue();
//            case "frieza" -> buttonClickEvent.reply(StartGame.startGame(userID, Race.Frieza)).setEphemeral(true).queue();
//        }
    }
}
