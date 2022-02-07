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

import static com.SharxNZ.Gifs.ActionGif.fixSql;

public class ResultGif extends AGif {
    protected static String statementSql = """
            SELECT
                SUM(CASE WHEN
                    (Power BETWEEN ? - 15 AND ? + 15)
                        AND AAttack = ?
                        AND (DAttack = ? OR DAttack IS NULL)
                        AND ARace = ?
                        AND ATransformation # ?
                        AND (DRace = ? OR DRace IS NULL)
                        AND (DTransformation = ?
                        OR DTransformation IS NULL)
                THEN 1 ELSE 0 END) AS 'precise',
                SUM(CASE WHEN
                        (Power BETWEEN ? - 15 AND ? + 15)
                            AND AAttack = ?
                            AND (DAttack = ? OR DAttack IS NULL)
                            AND (ARace = ?
                            OR ATransformation # ?)
                            AND (DRace = ? OR DRace IS NULL)
                            AND (DTransformation = ?
                            OR DTransformation IS NULL)
                    THEN 1 ELSE 0 END) AS 'half',
                SUM(CASE WHEN
                        (Power BETWEEN ? - 15 AND ? + 15)
                            AND AAttack = ?
                            AND (DAttack = ? OR DAttack IS NULL)
                    THEN 1 ELSE 0 END) AS 'other'
            FROM
                gifs.result
            """;
    protected static String preciseSql = """
            SELECT Length ,Gif FROM gifs.result WHERE
                (Power BETWEEN ?-15 AND ?+15)
                    AND AAttack = ?
                    AND (DAttack = ? OR DAttack IS NULL)
                    AND ARace = ? AND ATransformation # ? AND (DRace = ? OR DRace is null) AND (DTransformation = ? OR DTransformation is null)
                    ORDER BY RAND() LIMIT 1;
            """;
    protected static String halfSql = """
            SELECT Length ,Gif FROM gifs.result WHERE
                (Power BETWEEN ?-15 AND ?+15)
                    AND AAttack = ?
                    AND (DAttack = ? OR DAttack IS NULL)
                    AND (ARace = ? OR ATransformation # ?) AND (DRace = ? OR DRace is null) AND (DTransformation = ? OR DTransformation is null)
                    ORDER BY RAND() LIMIT 1;
            """;
    protected static String otherSql = """
            SELECT Length ,Gif as 'total' FROM gifs.result WHERE
                (Power BETWEEN ?-15 AND ?+15)
                    AND AAttack = ?
                    AND (DAttack = ? OR DAttack IS NULL)
                    ORDER BY RAND() LIMIT 1;
            """;
    protected String aTransformation;
    protected String aAttack;
    protected String dRace;
    protected String dTransformation;
    protected String dAttack;
    protected int power;

    public ResultGif(@Nullable Race aRace, @NotNull Transformation aTransformation, @NotNull Attack aAttack,
                     @Nullable Race dRace, @NotNull Transformation dTransformation, @Nullable Attack dAttack, long power, String link) throws ImageProcessingException, IOException {
        this.setGifAnimatedTimeLengthFromUrl(link);
        this.race = aRace != null ? aRace.getName() : null;
        this.aTransformation = aTransformation.getAbbreviated();
        this.aAttack = aAttack.getAbbreviated();
        this.dRace = dRace != null ? dRace.getName() : null;
        this.dTransformation = dTransformation.getAbbreviated();
        this.dAttack = dAttack != null ? dAttack.getAbbreviated() : null;
        power = Math.max(-1, power);
        this.power = (int) Math.min(100, power);
    }

    @Nullable
    public static Gif getResultGif(@Nullable String aRace, @Nullable String aTransformation, @NotNull String aAttack,
                                   @Nullable String dRace, @Nullable String dTransformation, @Nullable String dAttack, int power) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement(fixSql(aTransformation, statementSql))
        ) {
            final int multiplier = 4;
            Random rand = new Random();
            int precise;
            int half = 0;
            int other = 0;

            if (aAttack.equalsIgnoreCase("Defence"))
                aAttack = "Strike";

            if (power == -1)
                power = -15;

            statement.setInt(1, power);
            statement.setInt(2, power);
            statement.setString(3, aAttack);
            statement.setString(4, dAttack);
            statement.setString(5, aRace);
            statement.setString(6, aTransformation);
            statement.setString(7, dRace);
            statement.setString(8, dTransformation);
            statement.setInt(9, power);
            statement.setInt(10, power);
            statement.setString(11, aAttack);
            statement.setString(12, dAttack);
            statement.setString(13, aRace);
            statement.setString(14, aTransformation);
            statement.setString(15, dRace);
            statement.setString(16, dTransformation);
            statement.setInt(17, power);
            statement.setInt(18, power);
            statement.setString(19, aAttack);
            statement.setString(20, dAttack);

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;
            precise = resultSet.getInt(1);
            half = resultSet.getInt(2) - precise;
            other = resultSet.getInt(3) - (half + precise);
            statement.close();
            System.out.println("precise: " + precise);
            System.out.println("half: " + half);
            System.out.println("other: " + other);
            if ((precise + half + other) <= 0) {
                if (dAttack != null && dAttack.equalsIgnoreCase("Strike"))
                    return getResultGif(aRace, aTransformation, aAttack, dRace, dTransformation, "Defence", power);
                else
                    return null;
            }
            precise = precise * multiplier;
            half = half * multiplier / 2;

            if (precise > rand.nextInt(precise + half + other + 1)) {
                System.out.println("precise-gif");
                PreparedStatement pStatement = con.prepareStatement(fixSql(aTransformation, preciseSql));
                pStatement.setInt(1, power);
                pStatement.setInt(2, power);
                pStatement.setString(3, aAttack);
                pStatement.setString(4, dAttack);
                pStatement.setString(5, aRace);
                pStatement.setString(6, aTransformation);
                pStatement.setString(7, dRace);
                pStatement.setString(8, dTransformation);
                ResultSet pResultSet = pStatement.executeQuery();
                if (!pResultSet.next())
                    return null;
                System.out.println(pResultSet.getString(2));
                return Gif.getGif(pResultSet.getShort(1), pResultSet.getString(2));
            } else if (half > rand.nextInt(precise + half + other + 1)) {
                System.out.println("half-gif");
                PreparedStatement hStatement = con.prepareStatement(fixSql(aTransformation, halfSql));
                hStatement.setInt(1, power);
                hStatement.setInt(2, power);
                hStatement.setString(3, aAttack);
                hStatement.setString(4, dAttack);
                hStatement.setString(5, aRace);
                hStatement.setString(6, aTransformation);
                hStatement.setString(7, dRace);
                hStatement.setString(8, dTransformation);
                ResultSet hResultSet = hStatement.executeQuery();
                if (!hResultSet.next())
                    return null;
                System.out.println(hResultSet.getString(2));
                return Gif.getGif(hResultSet.getShort(1), hResultSet.getString(2));
            } else {
                System.out.println("other-gif");
                PreparedStatement oStatement = con.prepareStatement(otherSql);
                oStatement.setInt(1, power);
                oStatement.setInt(2, power);
                oStatement.setString(3, aAttack);
                oStatement.setString(4, dAttack);
                ResultSet oResultSet = oStatement.executeQuery();
                if (!oResultSet.next())
                    return null;
                System.out.println(oResultSet.getString(2));
                return Gif.getGif(oResultSet.getShort(1), oResultSet.getString(2));
            }

        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return null;
        }

    }

    public void checkGif(@NotNull User user, String id) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Result Gif");
        builder.addField("Attacker's race", race == null ? "null" : race, false);
        builder.addField("Attacker's transformation", aTransformation != null ? aTransformation : "base", false);
        builder.addField("Attacker's attack", aAttack, false);
        builder.addField("Defender's race", dRace == null ? "null" : dRace, false);
        builder.addField("Defender's transformation", dTransformation != null ? dTransformation : "base", false);
        builder.addField("Defender's attack", dAttack == null ? "null" : dAttack, false);
        builder.addField("Power", String.valueOf(power), false);
        builder.addField("Length", String.valueOf(length), false);
        builder.setImage(link);
        builder.setColor(Color.RED);
        builder.setFooter("Sent by: " + user.getAsTag(), user.getAvatarUrl());

        sendGifCheck(builder.build(), user, id);

    }

    @Override
    public boolean saveGif() {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO `gifs`.`result` (`ARace`, `ATransformation`, `AAttack`, `DRace`, `DTransformation`, `DAttack`, `power`, `Length`, `Gif`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);")
        ) {
            statement.setString(1, race);
            statement.setString(2, aTransformation);
            statement.setString(3, aAttack);
            statement.setString(4, dRace);
            statement.setString(5, dTransformation);
            statement.setString(6, dAttack);
            statement.setInt(7, power);
            statement.setShort(8, length);
            statement.setString(9, link);
            statement.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            return false;
        }
    }
}
