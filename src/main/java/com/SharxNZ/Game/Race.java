package com.SharxNZ.Game;

import com.SharxNZ.Android24;

import javax.naming.NameNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Race { // זה יכול ליצר רשימה של אובייקטים קבועים כשעולה התוכנית

    protected String name;
    protected int health;
    protected int ki;
    protected int strikeAttack;
    protected int kiAttack;
    protected int defence;
    protected int speed;

    public Race(String name) throws SQLException, NameNotFoundException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement(
                        "SELECT * FROM android24.races WHERE RaceName = ?;")
        ) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                throw new NameNotFoundException("There is no such race");
            this.name = resultSet.getString(1);
            health = resultSet.getInt(2);
            ki = resultSet.getInt(3);
            strikeAttack = resultSet.getInt(4);
            kiAttack = resultSet.getInt(5);
            defence = resultSet.getInt(6);
            speed = resultSet.getInt(7);
        }
    }

    public Race(String name, int health, int ki, int strikeAttack, int kiAttack, int defence, int speed) {
        this.name = name;
        this.health = health;
        this.ki = ki;
        this.strikeAttack = strikeAttack;
        this.kiAttack = kiAttack;
        this.defence = defence;
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getKi() {
        return ki;
    }

    public void setKi(int ki) {
        this.ki = ki;
    }

    public int getStrikeAttack() {
        return strikeAttack;
    }

    public void setStrikeAttack(int strikeAttack) {
        this.strikeAttack = strikeAttack;
    }

    public int getKiAttack() {
        return kiAttack;
    }

    public void setKiAttack(int kiAttack) {
        this.kiAttack = kiAttack;
    }

    public int getDefence() {
        return defence;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
