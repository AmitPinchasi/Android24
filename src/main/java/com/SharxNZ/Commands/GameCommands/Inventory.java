package com.SharxNZ.Commands.GameCommands;

import com.SharxNZ.Android24;
import com.SharxNZ.Game.*;
import com.SharxNZ.Utilities.DoublyCircularLinkedList;
import com.SharxNZ.Utilities.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import javax.naming.NameNotFoundException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;


public abstract class Inventory {

    public static void start() {
    }

    public static MessageEmbed attacks(User user) {
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            DoublyCircularLinkedList<DisplayAttack> attacks = Being.getDisplayAttacks(user.getIdLong());
            embedBuilder.setTitle(user.getName());
            embedBuilder.setDescription("Here you can see all the attacks that you have. To take a deep view on " +
                    "each one use the `/shop buy` command");
            for(DisplayAttack attack: attacks)
                embedBuilder.addField(attack.getName(), attack.getAbbreviated(), true);
            return embedBuilder.build();
        } catch (SQLException | NameNotFoundException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }
    }
    public static MessageEmbed transformations(User user) {
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            DoublyCircularLinkedList<DisplayTransformation> transformations = Being.getDisplayTransformations(user.getIdLong());
            embedBuilder.setTitle(user.getName());
            embedBuilder.setDescription("Here you can see all the attacks that you have. To take a deep view on " +
                    "each one use the `/shop buy` command");
            for(DisplayTransformation transformation: transformations)
                embedBuilder.addField(transformation.getName(), transformation.getAbbreviated(), true);
            return embedBuilder.build();
        } catch (SQLException | NameNotFoundException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }
    }


}
