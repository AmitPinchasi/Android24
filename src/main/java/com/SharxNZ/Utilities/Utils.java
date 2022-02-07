package com.SharxNZ.Utilities;

import com.SharxNZ.Android24;
import com.SharxNZ.Game.Being;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Utils {


    public static String checkRace(long userID) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "select Race from android24.users_data where userID = ?;")
        ) {
            statement.setLong(1, userID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return resultSet.getString(1);
            else
                return null;
        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return null;
        }
    }

    public static void notifyProblem(Guild guild, String text) {
        notifyProblem(guild, new Server(guild.getIdLong()), text);
    }

    public static void notifyProblem(@NotNull Guild guild, Server server, String text) {
        guild.retrieveOwner().queue(owner -> {
            owner.getUser().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage(text).queue(null, throwable -> {
                    TextChannel textChannel = guild.getTextChannelById(server.getLoggingCh());
                    if (textChannel != null)
                        textChannel.sendMessage(owner.getAsMention() + text).queue();
                });
            });
        });
    }

    public static boolean checkInGame(long userID) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement inGameStatement = con.prepareStatement(
                        "SELECT `Race` FROM `android24`.`users_data` WHERE `UserID` = ?;")
        ) {

            inGameStatement.setLong(1, userID);
            ResultSet resultSet = inGameStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getString(1) != null;
            else
                return false;
        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return false;
        }
    }

    public static <T> boolean inUse(T instance) {
        if (instance instanceof Being being)
            return being.getInUse();
        else
            return false;

        // In Java's next version:
        /*switch (instance){
            case Being b:
        }*/
    }

    public static <T> void setInUse(T instance) {
        switch (instance) {
            case Being being -> being.setInUse(false);

            default -> throw new IllegalStateException("Unexpected value: " + instance);
        }
    }

    public static <T1, T2> void garbageCollector(@NotNull HashMap<T1, T2> hashMap) {
        Lock lock = new ReentrantLock();
        Timer timer = new Timer();
        final int[] i = {0};
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                HashMap<T1, T2> tempMap = (HashMap<T1, T2>) hashMap.clone();
                for (T1 key : tempMap.keySet()) {
                    lock.lock();
                    if (inUse(tempMap.get(key))) {
                        setInUse(tempMap.get(key));
                        System.out.println(" === Prolonged === ");
                    } else {
                        hashMap.remove(key);
                        System.out.println(" === deleted === ");
                    }
                    lock.unlock();
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 60000, 60000); // 60000 -> 60 seconds
    }

    public static void getImageUrl(byte[] image, @NotNull AtomicReference<String> value) {
        EmbedBuilder wrapper = new EmbedBuilder();
        value.set(
                Android24.jda.getTextChannelById(Android24.cacheChannelID)
                        .sendFile(image, "png.png")
                        .setEmbeds(wrapper.setImage("attachment://png.png").build())
                        .complete().getEmbeds().get(0).getImage().getUrl());
    }
}
