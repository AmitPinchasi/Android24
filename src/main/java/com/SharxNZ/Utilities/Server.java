package com.SharxNZ.Utilities;

import com.SharxNZ.Android24;
import org.jetbrains.annotations.Nullable;

import javax.management.relation.Role;
import javax.naming.NameNotFoundException;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.SharxNZ.Android24.jda;

public class Server {
    protected long guildID;
    protected long commandsCh;
    protected long welcomeCh;
    protected long battlesCt;
    protected long loggingCh;
    protected long transRole;
    protected boolean allowTransGif;


    public Server(long guildID) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement create = con.prepareStatement(
                        "SELECT CommandsCh, WelcomeCh, BattlesCt, LoggingCh, TransRole, AllowTransGif FROM guilds.guilds_data WHERE GuildID = ?;")
        ) {

            create.setLong(1, guildID);
            ResultSet resultSet = create.executeQuery();
            if (!resultSet.next()) {
                create.close();
                throw new NameNotFoundException("The guild name has not found");
            }
            this.guildID = guildID;
            commandsCh = resultSet.getLong(1);
            welcomeCh = resultSet.getLong(2);
            battlesCt = resultSet.getLong(3);
            loggingCh = resultSet.getLong(4);
            transRole = resultSet.getLong(5);
            allowTransGif = resultSet.getBoolean(6);
        } catch (SQLException | NameNotFoundException throwables) {
            Android24.logError(throwables);
        }
    }

    public void setServer() {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement set = con.prepareStatement(
                        "UPDATE guilds.guilds_data SET CommandsCh = ?, WelcomeCh = ?, BattlesCt = ?, LoggingCh = ?,TransRole = ?, AllowTransGif = ? WHERE GuildID = ?;")
        ) {
            set.setLong(1, commandsCh);
            set.setLong(2, welcomeCh);
            set.setLong(3, battlesCt);
            set.setLong(4, loggingCh);
            set.setLong(5, transRole);
            set.setBoolean(6, allowTransGif);
            set.setLong(7, guildID);
            set.executeUpdate();
        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }

    public long getGuildID() {
        return guildID;
    }

    public long getCommandsCh() {
        return commandsCh;
    }

    public long getWelcomeCh() {
        return welcomeCh;
    }

    public long getBattlesCt() {
        return battlesCt;
    }

    public long getLoggingCh() {
        return loggingCh;
    }

    public long getTransRole() {
        return transRole;
    }

    public boolean isAllowTransGif() {
        return allowTransGif;
    }

    public void setGuildID(long guildID) {
        this.guildID = guildID;
    }

    public void setCommandsCh(long commandsCh) {
        this.commandsCh = commandsCh;
    }

    public void setWelcomeCh(long welcomeCh) {
        this.welcomeCh = welcomeCh;
    }

    public void setBattlesCt(long battlesCt) {
        this.battlesCt = battlesCt;
    }

    public void setLoggingCh(long loggingCh) {
        this.loggingCh = loggingCh;
    }

    public void setTransRole(long transRole) {
        this.transRole = transRole;
    }

    public void setAllowTransGif(boolean allowTransGif) {
        this.allowTransGif = allowTransGif;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("Command Channel: ").append(commandsCh);
        return string.toString();
    }
}
