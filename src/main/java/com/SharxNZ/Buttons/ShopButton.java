package com.SharxNZ.Buttons;

import com.SharxNZ.Shop.Shop;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ShopButton extends ListenerAdapter {

    @Override
    public void onButtonClick(ButtonClickEvent buttonClickEvent) {
        if(buttonClickEvent.getComponentId().startsWith("shop#")) {
            buttonClickEvent.deferReply(true).queue();
            buttonClickEvent.getHook().sendMessageEmbeds(Shop.tryToBuy(buttonClickEvent.getComponentId()
                    .substring(5), buttonClickEvent.getUser().getIdLong())).queue();
        }
    }
}
