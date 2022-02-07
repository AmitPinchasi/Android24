package com.SharxNZ.Gifs;

import com.SharxNZ.Android24;
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

public class TransGif extends AGif { // הכלאס הזה דורש תיקון דחוף. הגיפים לא עובדים כמו שצריך

    private static String statementSql = """
            SELECT
                SUM(CASE WHEN
                    `From` # ? and `To` ~ ?
                THEN 1 ELSE 0 END) AS 'precise',
                SUM(CASE WHEN
                    `To` ~ ?
                THEN 1 ELSE 0 END) AS 'total'
            FROM
                gifs.transform
            """;
    private static String preciseSql = "SELECT Length ,Gif FROM gifs.transform where `From` # ? and `To` ~ ? ORDER BY RAND() LIMIT 1;";
    private static String otherSql = "SELECT Length ,Gif FROM gifs.transform where `To` ~ ? ORDER BY RAND() LIMIT 1;";
    protected String from;
    protected String to;


    public TransGif(int id) throws ClassNotFoundException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM gifs.transform where GifID = ?;")
        ) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                throw new ClassNotFoundException("The gif doesn't exist");
            this.id = id;
            race = resultSet.getString(2);
            from = resultSet.getString(3);
            to = resultSet.getString(4);
            length = resultSet.getShort(5);
            link = resultSet.getString(6);
        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }

    public TransGif(String link) throws ClassNotFoundException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM gifs.transform where Gif = ?;")
        ) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                throw new ClassNotFoundException("The gif doesn't exist");
            id = resultSet.getInt(1);
            race = resultSet.getString(2);
            from = resultSet.getString(3);
            to = resultSet.getString(4);
            length = resultSet.getShort(5);
            this.link = link;
        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }

    public TransGif(@NotNull Race race, @NotNull Transformation from, @NotNull Transformation to, String link) throws ImageProcessingException, IOException {
        this.setGifAnimatedTimeLengthFromUrl(link);
        this.race = race.getName();
        this.from = from.getAbbreviated();
        this.to = to.getAbbreviated();
    }

    @NotNull
    private static String fixSql(String from, String to, String sql) {
        if (from == null)
            sql = sql.replaceAll("#", "is");
        else
            sql = sql.replaceAll("#", "=");
        if (to == null)
            sql = sql.replaceAll("~", "is");
        else
            sql = sql.replaceAll("~", "=");
        return sql;
    }

    @Nullable
    public static Gif getTransGif(String from, String to) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement(fixSql(from, to, statementSql))
        ) {
            final int multiplier = 3;
            Random rand = new Random();
            int precise;
            int other = 0;
            statement.setString(1, from);
            statement.setString(2, to);
            statement.setString(3, to);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;
            precise = resultSet.getInt(1);
            other = resultSet.getInt(2) - precise;
            statement.close();
            if ((from != null && from.equals(to)) || (from == null && to == null))
                other = 0;
            if ((precise + other) == 0)
                return null;
            precise = precise * multiplier;
            if (precise >= rand.nextInt(precise + other + 1)) {
                PreparedStatement pStatement = con.prepareStatement(fixSql(from, to, preciseSql));
                pStatement.setString(1, from);
                pStatement.setString(2, to);
                ResultSet pResultSet = pStatement.executeQuery();
                if (!pResultSet.next())
                    return null;
                return Gif.getGif(pResultSet.getShort(1), pResultSet.getString(2));
            } else {
                PreparedStatement oStatement = con.prepareStatement(fixSql(from, to, otherSql));
                oStatement.setString(1, to);
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

    public void checkGif(User user, String id) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Transformation Gif");
        builder.addField("Race", race, false);
        builder.addField("From", from != null ? from : "base", false);
        builder.addField("To", to != null ? to : "base", false);
        builder.addField("Length", String.valueOf(length), false);
        builder.setImage(link);
        builder.setColor(Color.YELLOW);
        builder.setFooter("Sent by: " + user.getAsTag(), user.getAvatarUrl());

        sendGifCheck(builder.build(), user, id);

    }

    public boolean saveGif() {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO `gifs`.`transform` (`Race`, `From`, `To`, `Length`, `Gif`) VALUES (?, ?, ?, ?, ?);")
        ) {
            statement.setString(1, race);
            statement.setString(2, from);
            statement.setString(3, to);
            statement.setShort(4, length);
            statement.setString(5, link);
            statement.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            return false;
        }
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
