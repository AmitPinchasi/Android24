package com.SharxNZ.Shop;

import com.SharxNZ.Android24;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TreeShopItem {
    protected String name;
    protected String linkedTo;
    protected short storey;

    public TreeShopItem(String name) {
        this.name = name;
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT LinkedTo, Storey FROM android24.shop WHERE Name = ?;");
        ) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                return;
            this.linkedTo = resultSet.getString(1);
            this.storey = resultSet.getShort(2);
        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }

    public static boolean isAvailable(@NotNull TreeShopItem down, @NotNull TreeShopItem up) {
        System.out.println("Down :\n" + down);
        System.out.println("Up :\n" + up);
        System.out.println();
        if (down.getStorey() <= up.getStorey())
            return down.getName().equalsIgnoreCase(up.getName());
        else if(down.getLinkedToName() != null)
            return isAvailable(down.getLinkedTo(), up);
        else return false;
    }

    public static TreeShopItem max(@NotNull TreeShopItem left,@NotNull TreeShopItem right){
        if (left.getStorey() >= right.getStorey())
            return left;
        else return right;
    }

    public static TreeShopItem min(@NotNull TreeShopItem left,@NotNull TreeShopItem right){
        if (left.getStorey() < right.getStorey())
            return left;
        else return right;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeShopItem getLinkedTo() {
        return new TreeShopItem(linkedTo);
    }

    public String getLinkedToName() {
        return linkedTo;
    }

    public short getStorey() {
        return storey;
    }

    public void setStorey(short storey) {
        this.storey = storey;
    }

    @Override
    public String toString() {
        return "TreeShopItem{" +
                "name='" + name + '\'' +
                ", linkedTo='" + linkedTo + '\'' +
                ", storey=" + storey +
                '}';
    }
}
