package com.SharxNZ.Commands;

import com.SharxNZ.Android24;
import com.SharxNZ.Utilities.Graphics;
import com.SharxNZ.Utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Level {

    public static void Level() {
        Android24.addCommands(new CommandData("level", "Returns your level")
                .addOptions(new OptionData(OptionType.BOOLEAN, "display", "display your level")));

    }

    //super.category = new Category("XP");


    public static int calculateLevel(long xp) {
        return (int) Math.floor(Math.pow(xp, Android24.difficulty));
    }


    public static byte @NotNull [] returnLevel(long guildID, long userID, String userURL) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement levelStatement = con.prepareStatement(
                        "SELECT XP FROM `android24`.users_data where UserID = ?;")
        ) {

            levelStatement.setLong(1, userID);
            ResultSet resultSet = levelStatement.executeQuery();

            if (resultSet.next()) {
                String guildName = Android24.jda.getGuildById(guildID).getName();
                long xp = resultSet.getLong(1);
                levelStatement.close();
                return Graphics.levelImage(userURL, guildName, xp);
            } else return new byte[0];
        } catch (Exception throwables) {
            Android24.logError(throwables);
            return new byte[0];
        }
    }

    public static MessageEmbed returnLevelEmbed(long guildID, long userID, String userURL) {
        AtomicReference<String> imageUrl = new AtomicReference<>();
        Utils.getImageUrl(Level.returnLevel(guildID, userID, userURL), imageUrl);
        return new EmbedBuilder().setImage(imageUrl.get()).build();
    }
}
