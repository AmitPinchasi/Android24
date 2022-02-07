package com.SharxNZ.Gifs;

import com.SharxNZ.Android24;
import com.SharxNZ.Game.Attack;
import com.SharxNZ.Game.Race;
import com.SharxNZ.Game.Transformation;
import com.drew.imaging.ImageProcessingException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class ActionGif extends AGif {
    private static String statementSql = """
            SELECT
                sum(case when
                    `Race` = ? and `Transformation` # ? and `Attack` = ?
                then 1 else 0 end) as 'precise',
                sum(case when
                    (`Race` = ? or `Transformation` # ?) and `Attack` = ?
                then 1 else 0 end) as 'half',
                sum(case when
                    `Attack` = ?
                then 1 else 0 end) as 'total'
            FROM
                gifs.action
            """;
    private static String preciseSql = "SELECT Length ,Gif FROM gifs.action where `Race` = ? and `Transformation` # ? and `Attack` = ? ORDER BY RAND() LIMIT 1;";
    private static String halfSql = "SELECT Length ,Gif FROM gifs.action where (`Race` = ? or `Transformation` # ?) and `Attack` = ? ORDER BY RAND() LIMIT 1;";
    private static String otherSql = "SELECT Length ,Gif FROM gifs.action where `Attack` = ? ORDER BY RAND() LIMIT 1;";
    protected String transformation;
    protected String attack;


    public ActionGif(@Nullable Race race, @NotNull Transformation transformation, @NotNull Attack attack, String link) throws ImageProcessingException, IOException {
        this.setGifAnimatedTimeLengthFromUrl(link);
        this.race = race != null ? race.getName() : null;
        this.transformation = transformation.getAbbreviated();
        this.attack = attack.getAbbreviated();
    }

    @Nullable
    public static Gif getActionGif(String race, String transformation, String attack) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement(fixSql(transformation, statementSql))
        ) {
            final int multiplier = 4;
            Random rand = new Random();
            int precise;
            int half = 0;
            int other = 0;
            statement.setString(1, race);
            statement.setString(2, transformation);
            statement.setString(3, attack);
            statement.setString(4, race);
            statement.setString(5, transformation);
            statement.setString(6, attack);
            statement.setString(7, attack);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;
            precise = resultSet.getInt(1);
            half = resultSet.getInt(2) - precise;
            other = resultSet.getInt(3) - (half + precise);
            statement.close();
            if ((precise + half + other) <= 0)
                return null;
            precise = precise * multiplier;
            half = half * multiplier / 2;
            if (precise > rand.nextInt(precise + half + other + 1)) {
                System.out.println("p");
                PreparedStatement pStatement = con.prepareStatement(fixSql(transformation, preciseSql));
                pStatement.setString(1, race);
                pStatement.setString(2, transformation);
                pStatement.setString(3, attack);
                ResultSet pResultSet = pStatement.executeQuery();
                if (!pResultSet.next())
                    return null;
                return Gif.getGif(pResultSet.getShort(1), pResultSet.getString(2));
            } else if (half > rand.nextInt(precise + half + other + 1)) {
                System.out.println("h");
                PreparedStatement hStatement = con.prepareStatement(fixSql(transformation, halfSql));
                hStatement.setString(1, race);
                hStatement.setString(2, transformation);
                hStatement.setString(3, attack);
                ResultSet hResultSet = hStatement.executeQuery();
                if (!hResultSet.next())
                    return null;
                return Gif.getGif(hResultSet.getShort(1), hResultSet.getString(2));
            } else {
                System.out.println("o");
                PreparedStatement oStatement = con.prepareStatement(otherSql);
                oStatement.setString(1, attack);
                ResultSet oResultSet = oStatement.executeQuery();
                if (!oResultSet.next())
                    return null;
                return Gif.getGif(oResultSet.getShort(1), oResultSet.getString(2));

            }

        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return null;
        }
    }

    @NotNull
    public static String fixSql(String transformation, String sql) {
        if (transformation == null)
            sql = sql.replaceAll("#", "is");
        else
            sql = sql.replaceAll("#", "=");
        return sql;
    }

    public void checkGif(@NotNull User user, String id) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Action Gif");
        builder.addField("Race", race != null ? race : "null", false);
        builder.addField("Transformation", transformation != null ? transformation : "base", false);
        builder.addField("Attack", attack, false);
        builder.addField("Length", String.valueOf(length), false);
        builder.setImage(link);
        builder.setColor(Color.BLUE);
        builder.setFooter("Sent by: " + user.getAsTag(), user.getAvatarUrl());

        sendGifCheck(builder.build(), user, id);

    }

    @Override
    public boolean saveGif() {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO `gifs`.`action` (`Race`, `Transformation`, `Attack`, `Length`, `Gif`) VALUES (?, ?, ?, ?, ?);")
        ) {
            statement.setString(1, race);
            statement.setString(2, transformation);
            statement.setString(3, attack);
            statement.setShort(4, length);
            statement.setString(5, link);
            statement.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            return false;
        }
    }

    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    public String getAttack() {
        return attack;
    }

    public void setAttack(String attack) {
        this.attack = attack;
    }
}
