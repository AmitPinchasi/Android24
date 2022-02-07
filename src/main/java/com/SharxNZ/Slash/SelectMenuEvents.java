package com.SharxNZ.Slash;

import com.SharxNZ.Game.Race;
import com.SharxNZ.GameFunctions.StartGame;
import com.SharxNZ.Utilities.Embeds;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.naming.NameNotFoundException;
import java.sql.SQLException;

public class SelectMenuEvents extends ListenerAdapter {

    @Override
    public void onSelectionMenu(SelectionMenuEvent selectMenuEvent) {

        long userID = selectMenuEvent.getUser().getIdLong();

        try {
            switch (selectMenuEvent.getComponentId()) {
                case "start game" -> selectMenuEvent.reply(StartGame.startGame(userID, new Race(selectMenuEvent.getSelectedOptions().get(0).getValue()))).setEphemeral(true).queue();
            }
        } catch (SQLException | NameNotFoundException e) {
            selectMenuEvent.replyEmbeds(Embeds.errorEmbed()).setEphemeral(true).queue();
        }
//        System.out.println(selectMenuEvent.getComponent().getOptions().get(0).getLabel());
//        System.out.println(selectMenuEvent.getComponent().getOptions().get(0).getValue());
//        System.out.println(selectMenuEvent.getComponent().getOptions().get(0).getDescription());
//        System.out.println(selectMenuEvent.getSelectedOptions().get(0).getValue());
//        System.out.println(selectMenuEvent.getInteraction().getSelectionMenu().getOptions().get(0).getValue());
//        System.out.println(selectMenuEvent.getComponentId());
//        System.out.println();
    }
}
