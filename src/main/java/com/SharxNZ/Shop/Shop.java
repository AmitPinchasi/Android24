package com.SharxNZ.Shop;

import com.SharxNZ.Android24;
import com.SharxNZ.Game.DisplayAttack;
import com.SharxNZ.Game.DisplayTransformation;
import com.SharxNZ.Game.Transformation;
import com.SharxNZ.Utilities.DoublyCircularLinkedList;
import com.SharxNZ.Utilities.Embeds;
import com.SharxNZ.Utilities.PreparedSql;
import com.SharxNZ.Utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.naming.NameNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Shop {
    private static final String buyAttackSql;
    private static final String buyTransformationSql;
    private static final String checkTypeSql;
    private static final String maxStoreySql;
    public static String asStatementSql;
    public static String tsStatementSql;

    // Setting the SQL statements
    static {
        buyAttackSql = """
                SELECT
                    IF('Already owned' NOT IN (SELECT
                                'Already owned' AS 'Check if exist'
                            FROM
                                android24.users_attacks
                            WHERE
                                UserID = ?
                                    AND AttackAbbreviated = (SELECT
                                        AttackAbbreviated
                                    FROM
                                        android24.attacks
                                    WHERE
                                        AttackAbbreviated = ?
                                            OR AttackName = ?)),
                        IF(ForcedRace IS NULL OR Race = ForcedRace,
                            IF(POWER(XP, ?) >= MinimalLevel,
                                IF(Zeni >= Cost, 'Ok', 'Under budget'),
                                'Under Level'),
                            'Race Limited'),
                        'Already owned') AS Result
                FROM
                    android24.users_data AS u,
                    android24.shop AS s
                        JOIN
                    android24.attacks AS a ON a.AttackName = s.Name
                WHERE
                    UserID = ?
                        AND
                        (AttackName = ?
                        OR AttackAbbreviated = ?)
                        AND
                        Display = true;
                """;
        buyTransformationSql = """
                SELECT
                    IF('Already owned' NOT IN (SELECT
                                'Already owned' AS 'Check if exist'
                            FROM
                                android24.users_transformations
                            WHERE
                                UserID = ?
                                    AND TransformationAbbreviated = (SELECT
                                        TransformationAbbreviated
                                    FROM
                                        android24.transformations
                                    WHERE
                                        TransformationAbbreviated = ?
                                            OR TransformationName = ?)),
                        IF(ForcedRace IS NULL OR Race = ForcedRace,
                            IF(POWER(XP, ?) >= MinimalLevel,
                                IF(Zeni >= Cost, 'Ok', 'Under budget'),
                                'Under Level'),
                            'Race Limited'),
                        'Already owned') AS Result
                FROM
                    android24.users_data AS u,
                    android24.shop AS s
                        JOIN
                    android24.transformations AS a ON a.TransformationName = s.Name
                WHERE
                    UserID = ?
                        AND (TransformationName = ?
                        OR TransformationAbbreviated = ?);
                """;
        checkTypeSql = """
                SELECT
                    IF(? IN (SELECT
                                Name
                            FROM
                                android24.attacks
                                    JOIN
                                android24.shop ON Name = AttackName
                            WHERE
                                (shop.Display = TRUE)
                            UNION
                            SELECT
                                AttackAbbreviated
                            FROM
                                android24.attacks
                                    JOIN
                                android24.shop ON Name = AttackName
                            WHERE
                                (shop.Display = TRUE)),
                        'Attack',
                        IF(? IN (SELECT
                                Name
                            FROM
                                android24.transformations
                                    JOIN
                                android24.shop ON Name = TransformationName
                                UNION
                                SELECT
                                TransformationAbbreviated
                            FROM
                                android24.transformations
                                    JOIN
                                android24.shop ON Name = TransformationName), 'Transformation', 'Not exists')) AS 'Type'
                """;

        maxStoreySql = """
                SELECT
                    TransformationName
                FROM
                    android24.users_transformations
                        JOIN
                    android24.transformations USING (TransformationAbbreviated)
                        JOIN
                    android24.shop ON TransformationName = Name
                WHERE
                    UserID = ?
                ORDER BY Storey DESC
                LIMIT 1
                """;

        asStatementSql = """
                SELECT
                    AttackName, Cost, MinimalLevel
                FROM
                    android24.attacks
                        JOIN
                    android24.shop ON AttackName = Name
                WHERE
                    (ForcedRace IS NULL OR ForcedRace = ?)
                    AND
                    Display = true;
                """;
        tsStatementSql = """
                SELECT
                    TransformationName, Cost, MinimalLevel
                FROM
                    android24.transformations
                        JOIN
                    android24.shop ON TransformationName = Name
                WHERE
                    ForcedRace IS NULL OR ForcedRace = ?;
                """;

    }

    public static MessageEmbed shopView(String item) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement checkType = con.prepareStatement(checkTypeSql)
        ) {
            checkType.setString(1, item);
            checkType.setString(2, item);
            ResultSet resultSet = checkType.executeQuery();
            if (!resultSet.next())
                return Embeds.errorEmbed();
            switch (resultSet.getString(1)) {
                case "Attack" -> {
                    return displayAttack(item);
                }
                case "Transformation" -> {
                    return displayTransformation(item);
                }
                default -> {
                    return Embeds.errorEmbed("The requested item doesn't exist...");
                }
            }
        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }
    }

    private static boolean checkIsTransInTree(long userId, String transName) throws SQLException {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement maxStoreyStatement = con.prepareStatement(maxStoreySql);
        ) {
            maxStoreyStatement.setLong(1, userId);
            ResultSet resultSet = maxStoreyStatement.executeQuery();
            if (!resultSet.next())
                return true;
            return TreeShopItem.isAvailable(
                    TreeShopItem.max(new TreeShopItem(resultSet.getString(1)), new TreeShopItem(transName)),
                    TreeShopItem.min(new TreeShopItem(resultSet.getString(1)), new TreeShopItem(transName)));
        }
    }

    public static MessageEmbed tryToBuy(String item, long userID) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement buyAttack = con.prepareStatement(buyAttackSql)
        ) {
            //Check if attack
            buyAttack.setLong(1, userID);
            buyAttack.setString(2, item);
            buyAttack.setString(3, item);
            buyAttack.setDouble(4, Android24.difficulty);
            buyAttack.setLong(5, userID);
            buyAttack.setString(6, item);
            buyAttack.setString(7, item);
            ResultSet resultSet = buyAttack.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getString(1).equals("Ok")) {
                    buyAttack.close();
                    return purchase("Attack", item, userID);
                } else
                    return Embeds.errorEmbed(resultSet.getString(1));
            } else {
                PreparedStatement buyTransformation = con.prepareStatement(buyTransformationSql);
                //Check if transformations
                buyTransformation.setLong(1, userID);
                buyTransformation.setString(2, item);
                buyTransformation.setString(3, item);
                buyTransformation.setDouble(4, Android24.difficulty);
                buyTransformation.setLong(5, userID);
                buyTransformation.setString(6, item);
                buyTransformation.setString(7, item);
                resultSet = buyTransformation.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getString(1).equals("Ok")) {
                        if (checkIsTransInTree(userID, new Transformation(item).getName())) {
                            buyAttack.close();
                            buyTransformation.close();
                            return purchase("Transformation", item, userID);
                        } else
                            return Embeds.errorEmbed("You can't buy this transformation because you chose other path of transformations!");
                    } else
                        return Embeds.errorEmbed(resultSet.getString(1));
                } else {
                    buyAttack.close();
                    buyTransformation.close();
                    return Embeds.errorEmbed("The requested item doesn't exist...");
                }
            }


        } catch (SQLException | NameNotFoundException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }
    }
    /*max Storey

;*/

    private static MessageEmbed purchase(String type, String item, long userID) {
        String purchaseSql = "INSERT INTO android24.users_#s (UserID, $Abbreviated) " +
                "VALUES (?, (select $Abbreviated from android24.#s where $Abbreviated = ? or $Name = ?));";
        PreparedSql purchaseP = new PreparedSql();
        purchaseP.setSql(purchaseSql);
        purchaseP.setFormatAll(type);
        purchaseP.stringChange(Long.toString(userID));
        purchaseP.stringChange(item);
        purchaseP.stringChange(item);

        PreparedSql subtractMoney = new PreparedSql();
        String subtractMoneySql = "update android24.users_data set Zeni = Zeni - (SELECT Cost FROM android24.shop " +
                "join android24.#s on $Name = Name " +
                "where $Abbreviated = ? or Name = ?) where UserID = ?;";
        subtractMoney.setSql(subtractMoneySql);
        subtractMoney.setFormatAll(type);
        subtractMoney.stringChange(item);
        subtractMoney.stringChange(item);
        subtractMoney.stringChange(Long.toString(userID));

        try (Connection con = Android24.getConnection()) {
            con.prepareStatement(subtractMoney.toString()).executeUpdate();
            con.prepareStatement(purchaseP.toString()).executeUpdate();
            return Embeds.successEmbed("The item has been purchased successfully!");
        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }

    }

    public static MessageEmbed displayAttack(String name) {
        try {
            return new DisplayAttack(name).getEmbed().build();
        } catch (NameNotFoundException exception) {
            return Embeds.errorEmbed("The requested item doesn't exist...");
        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }
    }

    public static MessageEmbed displayTransformation(String name) {
        try {
            return new DisplayTransformation(name).getEmbed().build();
        } catch (NameNotFoundException exception) {
            return Embeds.errorEmbed("The requested item doesn't exist...");
        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }
    }

    public static MessageEmbed attacksShop(long userID) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement asStatement = con.prepareStatement(asStatementSql)
        ) {

            EmbedBuilder asEmbed = new EmbedBuilder();
            asEmbed.setTitle("Attacks Shop");
            asEmbed.setDescription("Here you can see the list of all the attacks that you can buy");
            asStatement.setString(1, Utils.checkRace(userID));
            ResultSet resultSet = asStatement.executeQuery();
            while (resultSet.next())
                asEmbed.addField(resultSet.getString(1), resultSet.getString(2) + "$ Lvl:" + resultSet.getString(3), true);
            asStatement.close();
            return asEmbed.build();
        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }
    }

    public static MessageEmbed transformationsShop(long userID) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement tsStatement = con.prepareStatement(tsStatementSql)
        ) {

            EmbedBuilder tsEmbed = new EmbedBuilder();
            tsEmbed.setTitle("Transformations Shop");
            tsEmbed.setDescription("Here you can see the list of all the transformations that you can buy");
            tsStatement.setString(1, Utils.checkRace(userID));
            ResultSet resultSet = tsStatement.executeQuery();
            while (resultSet.next())
                tsEmbed.addField(resultSet.getString(1), resultSet.getString(2) + "$ Lvl:" + resultSet.getString(3), true);
            tsStatement.close();
            return tsEmbed.build();
        } catch (SQLException throwables) {
            Android24.logError(throwables);
            return Embeds.errorEmbed();
        }
    }

    public static DoublyCircularLinkedList<DisplayAttack> getAttacksShop(long userID) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement asStatement = con.prepareStatement(asStatementSql)
        ) {

            DoublyCircularLinkedList<DisplayAttack> attacks = new DoublyCircularLinkedList<>();
            asStatement.setString(1, Utils.checkRace(userID));
            ResultSet resultSet = asStatement.executeQuery();
            while (resultSet.next())
                attacks.add(new DisplayAttack(resultSet.getString(1)));
            asStatement.close();
            return attacks;
        } catch (SQLException | NameNotFoundException throwables) {
            Android24.logError(throwables);
            return null;
        }
    }

    public static DoublyCircularLinkedList<DisplayTransformation> getTransformationsShop(long userID) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement tsStatement = con.prepareStatement(tsStatementSql)
        ) {

            DoublyCircularLinkedList<DisplayTransformation> transformations = new DoublyCircularLinkedList<>();
            tsStatement.setString(1, Utils.checkRace(userID));
            ResultSet resultSet = tsStatement.executeQuery();
            while (resultSet.next())
                transformations.add(new DisplayTransformation(resultSet.getString(1)));
            tsStatement.close();
            return transformations;
        } catch (SQLException | NameNotFoundException throwables) {
            Android24.logError(throwables);
            return null;
        }
    }
}

