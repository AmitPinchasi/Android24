package com.SharxNZ.Game;

import com.SharxNZ.Android24;
import com.SharxNZ.Battle.Fighter;
import com.SharxNZ.Commands.GameCommands.Stats;
import com.SharxNZ.Commands.Level;
import com.SharxNZ.Utilities.DoublyCircularLinkedList;
import org.jetbrains.annotations.NotNull;

import javax.naming.NameNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Being { // לסדר את זה שלא יהיה סטט כי לא צריך.

    protected static String saveBeingStatementSql = """
            UPDATE `android24`.`users_data` SET `PowerPoints` = ?, `Health` = ?, `Ki` = ?, `StrikeAttack` = ?, `KiAttack` = ?,
            `Defence` = ?, `Speed` = ? WHERE `UserID` = ?
            """;
    protected static String getAttacksSql = """
            SELECT
                AttackName
            FROM
                android24.users_attacks
                    JOIN
                android24.attacks USING (AttackAbbreviated)
                Where UserID = ?;
            """;
    protected static String getTransformationsSql = """
            SELECT
                TransformationName
            FROM
                android24.users_transformations
                    JOIN
                android24.transformations USING (TransformationAbbreviated)
                Where UserID = ?;
            """;
    public String type;
    protected long userID;
    protected String name;
    protected String race;
    protected long zeni;
    protected int level;
    protected int powerPoints;
    protected long health;
    protected long ki;
    protected long strikeAttack;
    protected long kiAttack;
    protected long defence;
    protected long speed;
    protected Transformation transformation;
    protected boolean inUse;

    public Being(long userID) {
        try
                (Connection con = Android24.getConnection();
                 PreparedStatement getBeingStatement = con.prepareStatement("""
                              SELECT `Race`, `XP`, `Zeni`,`PowerPoints`, `Health`, `Ki`, `StrikeAttack`, `KiAttack`,
                              `Defence`, `Speed`, CurrentTransformation
                              FROM `android24`.users_data where `UserID` = ?;
                         """)) {

            getBeingStatement.setLong(1, userID);
            ResultSet resultSet = getBeingStatement.executeQuery();
            this.userID = userID;
            Android24.jda.retrieveUserById(userID).queue(user -> name = user.getName());
            if (!resultSet.next()) {
                return;
            }
            // resultSet.getMetaData().getColumnCount()
            this.race = resultSet.getString(1);
            this.level = Level.calculateLevel(resultSet.getLong(2));
            this.zeni = resultSet.getLong(3);
            this.powerPoints = resultSet.getInt(4);
            this.health = resultSet.getInt(5);
            this.ki = resultSet.getInt(6);
            this.strikeAttack = resultSet.getInt(7);
            this.kiAttack = resultSet.getInt(8);
            this.defence = resultSet.getInt(9);
            this.speed = resultSet.getInt(10);
            this.transformation = new Transformation(resultSet.getString(11));
            this.inUse = true;

        } catch (SQLException | NameNotFoundException throwables) {
            Android24.logError(throwables);
        }
    }

    public static DoublyCircularLinkedList<DisplayAttack> getDisplayAttacks(long userID) throws SQLException, NameNotFoundException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement getAttacks = con.prepareStatement(getAttacksSql)
        ) {
            DoublyCircularLinkedList<DisplayAttack> attacks = new DoublyCircularLinkedList<>();
            getAttacks.setLong(1, userID);
            ResultSet resultSet = getAttacks.executeQuery();
            while (resultSet.next())
                attacks.add(new DisplayAttack(resultSet.getString(1)));
            getAttacks.close();
            return attacks;
        }
    }

    public static DoublyCircularLinkedList<DisplayTransformation> getDisplayTransformations(long userID) throws SQLException, NameNotFoundException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement getTransformations = con.prepareStatement(getTransformationsSql)
        ) {
            DoublyCircularLinkedList<DisplayTransformation> transformations = new DoublyCircularLinkedList<>();
            getTransformations.setLong(1, userID);
            ResultSet resultSet = getTransformations.executeQuery();
            while (resultSet.next())
                transformations.add(new DisplayTransformation(resultSet.getString(1)));
            getTransformations.close();
            return transformations;
        }
    }

    public void save() {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement saveBeingStatement = con.prepareStatement(saveBeingStatementSql)
        ) {

            saveBeingStatement.setLong(1, getPowerPoints());
            saveBeingStatement.setLong(2, getHealth());
            saveBeingStatement.setLong(3, getKi());
            saveBeingStatement.setLong(4, getStrikeAttack());
            saveBeingStatement.setLong(5, getKiAttack());
            saveBeingStatement.setLong(6, getDefence());
            saveBeingStatement.setLong(7, getSpeed());
            saveBeingStatement.setLong(8, userID);
            saveBeingStatement.executeUpdate();

        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }

    public DoublyCircularLinkedList<DisplayAttack> getDisplayAttacks() throws SQLException, NameNotFoundException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement getAttacks = con.prepareStatement(getAttacksSql)
        ) {
            DoublyCircularLinkedList<DisplayAttack> attacks = new DoublyCircularLinkedList<>();
            getAttacks.setLong(1, userID);
            ResultSet resultSet = getAttacks.executeQuery();
            while (resultSet.next())
                attacks.add(new DisplayAttack(resultSet.getString(1)));
            getAttacks.close();
            return attacks;
        }
    }

    public DoublyCircularLinkedList<DisplayTransformation> getDisplayTransformations() throws SQLException, NameNotFoundException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement getTransformations = con.prepareStatement(getTransformationsSql)
        ) {
            DoublyCircularLinkedList<DisplayTransformation> transformations = new DoublyCircularLinkedList<>();
            getTransformations.setLong(1, userID);
            ResultSet resultSet = getTransformations.executeQuery();
            while (resultSet.next())
                transformations.add(new DisplayTransformation(resultSet.getString(1)));
            getTransformations.close();
            return transformations;
        }
    }

    public Stats getStats() {
        return new Stats(userID);
    }

    public long getUserID() {
        return this.userID;
    }

    public String getName() {
        return name;
    }

    public String getRace() {
        return this.race;
    }

    public long getZeni() {
        return zeni;
    }

    public int getLevel() {
        return level;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public long getHealth() {
        return health;
    }

    public long getKi() {
        return ki;
    }

    public long getStrikeAttack() {
        return strikeAttack;
    }

    public long getKiAttack() {
        return kiAttack;
    }

    public long getDefence() {
        return defence;
    }

    public long getSpeed() {
        return speed;
    }

    public boolean isInUse() {
        return inUse;
    }

    public Transformation getTransformation() {
        return transformation;
    }

    public void setTransformation(@NotNull Transformation trans) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement setTransformation = con.prepareStatement("UPDATE `android24`.`users_data` SET `CurrentTransformation` = ? WHERE (`UserID` = ?);")
        ) {

            setTransformation.setString(1, trans.getName());
            setTransformation.setLong(2, userID);
            setTransformation.executeUpdate();
            transformation = trans;

            Fighter fighter = Fighter.getFighter(userID);
            if (fighter != null)
                fighter.setTrans(trans);

        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }

    protected void setTrans(Transformation trans) {
        transformation = trans;
    }

    public boolean getInUse() {
        return this.inUse;
    }


    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }


}
