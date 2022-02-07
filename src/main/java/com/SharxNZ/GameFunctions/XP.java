package com.SharxNZ.GameFunctions;

import com.SharxNZ.Android24;
import com.SharxNZ.Commands.Level;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.naming.NameNotFoundException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class XP extends ListenerAdapter {

    // Long = userID | long[0] = xp, long[1] = timestamp
    public static final ConcurrentHashMap<Long, long[]> xpMap = new ConcurrentHashMap<>();
    public static final Set<Long> voiceSet = new HashSet<>();
    private final Random rand = new Random();

    public XP() throws SQLException {
        updateDatabase();
        voiceXP();
    }

    private short giveXP() {
        return (short) rand.nextInt(Android24.xp + 1);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent guildMessage) {
        if (!guildMessage.getAuthor().isBot()) { //If not bot
            long[] xpAndTime = new long[2];

            //userID
            long userID = guildMessage.getAuthor().getIdLong();
            if (xpMap.containsKey(userID)) { // If exists in the database
                long newDate = new Date().getTime();
                if (newDate - xpMap.get(userID)[1] > 20000) { // If 20 seconds passed //20000
                    xpAndTime[0] = xpMap.get(userID)[0] + giveXP(); // Give random XP from 0 to 20
                    xpAndTime[1] = newDate;
                    xpMap.replace(userID, xpAndTime);
                    //guildMessage.getChannel().sendMessage("XP UP").queue();
                }
            } else {
                xpMap.put(userID, new long[]{(long) giveXP(), new Date().getTime()}); // Adds him to the database with XP
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent guildVoiceJoin) {
        voiceSet.add(guildVoiceJoin.getMember().getIdLong());
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent guildVoiceLeaveEvent) {
        voiceSet.remove(guildVoiceLeaveEvent.getMember().getIdLong());
    }

    private void voiceXP() {
        Timer timer = new Timer();
        long[] xpAndTime = new long[2];
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Set<Long> tempSet = new HashSet<>(voiceSet);
                for (long key : tempSet) {
                    if (xpMap.containsKey(key)) {
                        xpAndTime[0] = xpMap.get(key)[0] + giveXP();
                        xpAndTime[1] = xpMap.get(key)[1];
                        xpMap.replace(key, xpAndTime);
                    } else
                        xpMap.put(key, new long[]{(long) giveXP(), 0});
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 120000); // Do the check every 120 seconds //120000
    }

    private void updateDatabase() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try (
                        Connection con = Android24.getConnection();
                        PreparedStatement getXP = con.prepareStatement(
                                "SELECT XP FROM `android24`.users_data where UserID = ?;");
                        PreparedStatement updateData = con.prepareStatement(
                                "UPDATE `android24`.`users_data` SET `XP` = ?, `Zeni` = `Zeni` + ?," +
                                        " `PowerPoints` = `PowerPoints` + ? WHERE `UserID` = ?;");
                        PreparedStatement insertUser = con.prepareStatement(
                                "INSERT INTO `android24`.`users_data` (`UserID`, `UserName`, `XP`, `Zeni`, `PowerPoints`)" +
                                        " VALUES (?, ?, ?, ?, ?);");
                ) {
                    //ConcurrentHashMap<Long, long[]> xpMap;
                    long previousXP, totalXP, zeni;
                    int lvlShouldBe, currentLvl, powerPoints;
                    // key = userID
                    for (Map.Entry<Long, long[]> entry : xpMap.entrySet()) {
                        try {
                            getXP.setLong(1, entry.getKey());
                            ResultSet resultSet = getXP.executeQuery();
                            if (resultSet.next()) {
                                previousXP = resultSet.getLong(1);
                                totalXP = entry.getValue()[0] + previousXP;
                                zeni = entry.getValue()[0];
                                currentLvl = Level.calculateLevel(previousXP);
                                lvlShouldBe = Level.calculateLevel(totalXP);
                                powerPoints = 4 * (lvlShouldBe - currentLvl);

                                Android24.jda.retrieveUserById(entry.getKey()).queue(user -> Android24.jda.getTextChannelById(Android24.debugChannelID).sendMessage(user.getAsTag() + " gets " + entry.getValue()[0] + " XP").queue());

                                updateData.setLong(1, totalXP);
                                updateData.setLong(2, zeni);
                                updateData.setInt(3, powerPoints);
                                updateData.setLong(4, entry.getKey());
                                updateData.executeUpdate();

                            } else {
                                totalXP = zeni = entry.getValue()[0];
                                powerPoints = 4 * (Level.calculateLevel(totalXP));
                                insertUser.setLong(1, entry.getKey());
                                insertUser.setString(2, Android24.jda.retrieveUserById(entry.getKey()).complete().getAsTag());
                                insertUser.setLong(3, totalXP);
                                insertUser.setLong(4, zeni);
                                insertUser.setInt(5, powerPoints);
                                insertUser.executeUpdate();
                            }
                            resultSet.close();
                        } catch (Exception throwables) {
                            Android24.logError(throwables);
                        }
                    }
                    xpMap.clear();
                } catch (SQLException throwables) {
                    Android24.logError(throwables);
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 60000, 60000); // Do the check every 60 seconds //60000

    }
}


