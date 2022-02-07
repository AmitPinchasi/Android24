package com.SharxNZ.Game;

import com.SharxNZ.Android24;

import javax.naming.NameNotFoundException;
import javax.naming.directory.Attributes;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Attack extends Ability { // ניתן לעשות cache לכל המתקפות ולשמור את זה ולעשות פעולה של get attack

    public enum ATTACK_TYPE {
        Strike, Ki, Defence, Charge
    }

    protected boolean counter;
    protected ATTACK_TYPE attackType;

    public Attack(String name) throws NameNotFoundException, SQLException {
        if (name.equals("Strike")) {
            this.name = name;
            abbreviated = name;
            kiConsumption = 10;
            attackType = ATTACK_TYPE.Strike;
            return;
        }
        if (name.equals("Ki")) {
            this.name = name;
            abbreviated = name;
            kiConsumption = 15;
            speedPowerUp = 3;
            attackType = ATTACK_TYPE.Ki;
            return;
        }
        if (name.equals("Defence")) {
            this.name = name;
            abbreviated = name;
            kiConsumption = 3;
            defencePowerUp = 3;
            attackType = ATTACK_TYPE.Defence;
            return;
        }
        if (name.equals("Charge")) {
            this.name = name;
            abbreviated = name;
            attackType = ATTACK_TYPE.Charge;
            return;
        }

        try (
                Connection con = Android24.getConnection();
                PreparedStatement getAttack = con.prepareStatement(
                        "SELECT * FROM android24.attacks where AttackName = ? or AttackAbbreviated = ?;")
        ) {

            getAttack.setString(1, name);
            getAttack.setString(2, name);
            ResultSet resultSet = getAttack.executeQuery();
            if (!resultSet.next()) {
                getAttack.close();
                throw new NameNotFoundException("The name of the attack does not exists");
            }
            this.name = resultSet.getString(1);
            abbreviated = resultSet.getString(2);
            attackPowerUp = resultSet.getInt(3);
            defencePowerUp = resultSet.getInt(4);
            speedPowerUp = resultSet.getInt(5);
            kiConsumption = resultSet.getInt(6);
            counter = resultSet.getBoolean(7);
            attackType = ATTACK_TYPE.valueOf(resultSet.getString(8));
        }
    }

    public Attack(ATTACK_TYPE type) {
        name = abbreviated = type.toString();
        attackType = type;
        switch (type) {
            case Strike -> kiConsumption = 10;
            case Ki -> {
                kiConsumption = 15;
                speedPowerUp = 3;
            }
            case Defence -> {
                kiConsumption = 3;
                defencePowerUp = 3;
            }
        }
    }

    // Because I have to call the super constructor on the first line...
    protected Attack() {
    }

    protected void setAttack(String name, String abbreviated, int attackPowerUp, int defencePowerUp,
                             int speedPowerUp, int kiConsumption, boolean counter, ATTACK_TYPE attackType) {
        this.name = name;
        this.abbreviated = abbreviated;
        this.attackPowerUp = attackPowerUp;
        this.defencePowerUp = defencePowerUp;
        this.speedPowerUp = speedPowerUp;
        this.kiConsumption = kiConsumption;
        this.counter = counter;
        this.attackType = attackType;
    }


    public boolean isCounter() {
        return counter;
    }

    public ATTACK_TYPE getAttackType() {
        return attackType;
    }

    public void setCounter(boolean counter) {
        this.counter = counter;
    }

    public void setAttackType(ATTACK_TYPE attackType) {
        this.attackType = attackType;
    }


}
