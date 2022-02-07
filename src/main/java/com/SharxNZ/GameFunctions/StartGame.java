package com.SharxNZ.GameFunctions;

import com.SharxNZ.Android24;
import com.SharxNZ.Game.Race;
import com.SharxNZ.Utilities.Utils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class StartGame {

    protected static SelectionMenu selectionMenu;

    public static void StartGame() {
        try (
                Connection con = Android24.getConnection();
                ResultSet resultSet = con.prepareStatement("SELECT RaceName FROM android24.races;").executeQuery();
        ) {

            OptionData optionData = new OptionData(OptionType.STRING, "race", "choose the race you want to play").setRequired(true);
            SelectionMenu.Builder smBuilder = SelectionMenu.create("start game")
                    .setPlaceholder("Choose you race")
                    .setRequiredRange(1, 1);
            while (resultSet.next()) {
                optionData.addChoice(resultSet.getString(1), resultSet.getString(1));
                smBuilder.addOption(resultSet.getString(1), resultSet.getString(1));
            }
            selectionMenu = smBuilder.build();
            Android24.addCommands(new CommandData("start_game", "Let's you start the game and choose your race")
                    .addOptions(optionData));
            Android24.jda.getTextChannelById(890891683166818305L).retrieveMessageById(890894750100652042L).queue(message ->
                    message.editMessageComponents().setActionRow(selectionMenu).queue());

            //startGameButton();

        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }


    private static void startGameButton() {
        Android24.jda.getTextChannelById(869983208698163250L).sendMessage("Choose your race")
                .setActionRow(Button.primary("saiyan", "Saiyan"),
                        Button.danger("frieza", "Frieza")).queue();

    }

    public static String startGame(long userID, Race race) {
        try (
                Connection con1 = Android24.getConnection();
                PreparedStatement setRace = con1.prepareStatement(
                        "UPDATE `android24`.`users_data` SET `Race` = ? WHERE (`UserID` = ?);");
        ) {
            // Setting it in th SQL
            if (Utils.checkInGame(userID))
                return "You're already in the game";
                //Adding the race to the user
            else if (con1.prepareStatement("SELECT `UserID` FROM `android24`.`users_data` where `UserID` = " + userID + ";").executeQuery().next()) {
                setRace.setString(1, race.getName());
                setRace.setLong(2, userID);
                setRace.executeUpdate();
            }
            //Creating a whole mew user
            else {
                Android24.jda.retrieveUserById(userID).queue(user -> {
                    try (
                            Connection con2 = Android24.getConnection();
                            PreparedStatement insertUser = con2.prepareStatement(
                                    "INSERT INTO `android24`.`users_data` (`UserID`, `UserName`, `Race`)" +
                                            " VALUES (?, ?, ?);")
                    ) {
                        insertUser.setLong(1, userID);
                        insertUser.setString(3, race.getName());
                        insertUser.setString(2, user.getAsTag());
                        insertUser.executeUpdate();
                    } catch (SQLException throwables) {
                        Android24.logError(throwables);
                    }
                });
            }
            return "You have been added successfully";
        } catch (IllegalArgumentException exception) {
            return "The role ID isn't correct";
        } catch (Exception throwables) {
            Android24.logError(throwables);
            return "Some error accrued";
        }
    }

    public static SelectionMenu getSelectionMenu() {
        return selectionMenu;
    }
}
