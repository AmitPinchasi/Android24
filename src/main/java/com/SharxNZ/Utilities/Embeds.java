package com.SharxNZ.Utilities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public abstract class Embeds {

    /*    private static String displaySql = "SELECT " +
            "$Name, $Abbreviated, Cost, MinimalLevel, AttackPowerUp, DefencePowerUp, SpeedPowerUp, KiConsumption, Description, ?, Gif" +
            " FROM android24.#s" +
            " JOIN" +
            " android24.Shop ON $Name = Name" +
            " WHERE $Name = '?' OR $Abbreviated = ?;";*/



    public static MessageEmbed savedEmbed(){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.green);
        embedBuilder.addField("All changes has been saved!", "", true);
        embedBuilder.setImage("https://user-images.githubusercontent.com/11019190/45695978-d59faa00-bb62-11e8-8f37-7b447356d237.png");
        return embedBuilder.build();
    }

    public static MessageEmbed discardEmbed(){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.red);
        embedBuilder.addField("Your changes has been discarded!", "", true);
        embedBuilder.setImage("https://static.thenounproject.com/png/2025351-200.png");
        return embedBuilder.build();
    }

    public static MessageEmbed errorEmbed(){
        return new EmbedBuilder().setImage("https://www.computerhope.com/jargon/e/error.png").setColor(Color.red).build();
    }

    public static MessageEmbed errorEmbed(String text){
        return new EmbedBuilder().setTitle(text).setColor(Color.red).build();
    }

    public static MessageEmbed successEmbed(String text){
        return new EmbedBuilder().setTitle(text).setColor(Color.green).build();
    }

    public static MessageEmbed timeout(){
        return new EmbedBuilder().setTitle("Time out").setDescription("The time for this actions is over and the buttons will not dork.\n" +
                "You need to use this command again to make them work...").setImage("https://icon-library.com/images/d10240929c.png").build();
    }

}
