package com.SharxNZ.Game;

import com.SharxNZ.Android24;

import javax.naming.NameNotFoundException;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Transformation extends Ability {

    protected boolean soloTransformation;
    protected Color color;


    public Transformation(String name) throws NameNotFoundException, SQLException {
        if (name == null || name.equals("base"))
            return;

        try (
                Connection con = Android24.getConnection();
                PreparedStatement getTransformation = con.prepareStatement(
                        "SELECT * FROM android24.transformations where TransformationName = ? or TransformationAbbreviated = ?;")
        ) {

            getTransformation.setString(1, name);
            getTransformation.setString(2, name);
            ResultSet resultSet = getTransformation.executeQuery();
            if (!resultSet.next())
                throw new NameNotFoundException("The name of the transformation does not exists");
            this.name = resultSet.getString(1);
            abbreviated = resultSet.getString(2);
            attackPowerUp = resultSet.getInt(3);
            defencePowerUp = resultSet.getInt(4);
            speedPowerUp = resultSet.getInt(5);
            kiConsumption = resultSet.getInt(6);
            soloTransformation = resultSet.getBoolean(7);
            color = new Color(resultSet.getInt(8));
        }
    }

    // Because I have to call the super constructor on the first line...
    protected Transformation() {
    }

    protected void setTransformation(String name, String abbreviated, int attackPowerUp, int defencePowerUp, int speedPowerUp, int kiConsumption, boolean soloTransformation, int color) {
        this.name = name;
        this.abbreviated = abbreviated;
        this.attackPowerUp = attackPowerUp;
        this.defencePowerUp = defencePowerUp;
        this.speedPowerUp = speedPowerUp;
        this.kiConsumption = kiConsumption;
        this.soloTransformation = soloTransformation;
        this.color = new Color(color);
    }

    public static boolean checkTransformation(long userID, String transName) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement checkTransformation = con.prepareStatement("""
                        SELECT
                            IF(? IN (SELECT
                                        UserID
                                    FROM
                                        android24.users_transformations
                                            JOIN
                                        android24.transformations USING (TransformationAbbreviated)
                                    WHERE
                                        (TransformationName = ?
                                            OR TransformationAbbreviated = ?)
                                            AND UserID = ?),
                                TRUE,
                                FALSE) AS 'Result'
                        """)
        ) {
            checkTransformation.setLong(1, userID);
            checkTransformation.setString(2, transName);
            checkTransformation.setString(3, transName);
            checkTransformation.setLong(4, userID);
            ResultSet resultSet = checkTransformation.executeQuery();
            while (resultSet.next())
                if (resultSet.getBoolean(1)) {
                    checkTransformation.close();
                    return true;
                }
        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
        return false;
    }

    public boolean isSoloTransformation() {
        return soloTransformation;
    }

    public void setSoloTransformation(boolean soloTransformation) {
        this.soloTransformation = soloTransformation;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
