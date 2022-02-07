package com.SharxNZ.Game;

import com.SharxNZ.Android24;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.naming.NameNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DisplayAttack extends Attack {

    protected String forcedRace;
    protected int cost;
    protected int minimalLevel;
    protected String description;
    protected String gif;


    public DisplayAttack(String name) throws SQLException, NameNotFoundException {

        try (
                Connection con = Android24.getConnection();
                PreparedStatement getDisplayAttack = con.prepareStatement("""
                        SELECT
                            AttackName, AttackAbbreviated, AttackPowerUp,
                            DefencePowerUp, SpeedPowerUp, KiConsumption, Counter,
                            AttackType, ForcedRace, Cost, MinimalLevel, Description, Gif
                        FROM
                            android24.attacks
                                JOIN
                            android24.shop ON AttackName = Name
                        WHERE
                            AttackName = ?
                                OR AttackAbbreviated = ?;
                        """);
        ) {

            getDisplayAttack.setString(1, name);
            getDisplayAttack.setString(2, name);
            ResultSet resultSet = getDisplayAttack.executeQuery();
            if (!resultSet.next())
                throw new NameNotFoundException("The name of the attack does not exists");
            setAttack(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3),
                    resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6),
                    resultSet.getBoolean(7), ATTACK_TYPE.valueOf(resultSet.getString(8)));
            forcedRace = resultSet.getString(9);
            cost = resultSet.getInt(10);
            minimalLevel = resultSet.getInt(11);
            description = resultSet.getString(12);
            gif = resultSet.getString(13);
        }
    }

    public EmbedBuilder getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(getName());
        embedBuilder.setDescription("Short: `" + abbreviated + "`");
        embedBuilder.addField("Cost 💵", cost + "$", true);
        embedBuilder.addField("Counter Attack", counter + "", true);
        embedBuilder.addField("Attack Type", attackType + "", true);
        embedBuilder.addField("Attack power up", attackPowerUp + "", true);
        embedBuilder.addField("Defence power up", defencePowerUp + "", true);
        embedBuilder.addField("Speed power up", speedPowerUp + "", true);
        embedBuilder.addField("Ki consumption", kiConsumption + "", true);
        if (getForcedRace() != null)
            embedBuilder.addField("Only for race: ", forcedRace, true);
        embedBuilder.addField("Description", description, false);
        embedBuilder.setImage(getGif());

        return embedBuilder;
    }

    public String getForcedRace() {
        return forcedRace;
    }

    public int getCost() {
        return cost;
    }

    public int getMinimalLevel() {
        return minimalLevel;
    }

    public String getDescription() {
        return description;
    }

    public String getGif() {
        return gif;
    }

    public void setForcedRace(String forcedRace) {
        this.forcedRace = forcedRace;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setMinimalLevel(int minimalLevel) {
        this.minimalLevel = minimalLevel;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGif(String gif) {
        this.gif = gif;
    }
}
