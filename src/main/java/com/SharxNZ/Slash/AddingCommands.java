package com.SharxNZ.Slash;

import com.SharxNZ.Android24;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public interface AddingCommands {

    static void AddingCommands() {
        Android24.addCommands(

                // server setup
                new CommandData("server_setup", "Change the server's settings (only person with permissions can do it)")
                        .addOptions(new OptionData(OptionType.CHANNEL, "cmd_channel", "Set the server's commands channel"))
                        .addOptions(new OptionData(OptionType.CHANNEL, "wlc_channel", "Set the server's welcome channel"))
                        .addOptions(new OptionData(OptionType.CHANNEL, "battles_category", "Set where the bot should open the battles channels"))
                        .addOptions(new OptionData(OptionType.CHANNEL, "logg_channel", "Set the logging channel"))
                        .addOptions(new OptionData(OptionType.ROLE, "trans_role", "Set the role to control the transformations (default is ---transformations---)"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "allow_trans_gif", "Choose if to allow transformations gifs globally")),

                // Power Points
                new CommandData("get_power_points", "Display and let you edit your power points")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "display", "display your stats"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "refresh",
                                "Refreshes the image for every change. (Might by slow if display is false)")),
                // Shop
                new CommandData("shop", "All the operations that you can do in the Shop")
                        .addSubcommands(new SubcommandData("view", "View the items in the Shop")
                                .addOptions(new OptionData(OptionType.STRING, "of", "The type of the Shop you want to view")
                                        .addChoice("Special Attacks", "Special Attacks")
                                        .addChoice("Transformations", "Transformations")
                                        //.addChoice("Others", "Others")
                                        .setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "type", "List or individual")
                                        .addChoice("List", "List")
                                        .addChoice("Individual", "Individual")))
                        .addSubcommands(new SubcommandData("buy", "Buy items from the Shop")
                                .addOptions(new OptionData(OptionType.STRING, "item", "The name of the item you want to buy (Can be the abbreviated name)")
                                        .setRequired(true))),
                // Inventory
                new CommandData("inventory", "Display your inventory")
                        .addOptions(new OptionData(OptionType.STRING, "of", "The type of the inventory")
                                .addChoice("Special Attacks", "Special Attacks")
                                .addChoice("Transformations", "Transformations")
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.STRING, "type", "List or individual")
                                .addChoice("List", "List")
                                .addChoice("Individual", "Individual")),

                //Transform
                new CommandData("transform", "Transform to one of your transformations")
                        .addOptions(new OptionData(OptionType.STRING, "name", "The name of the transformation. (To revert back choose base)")
                                .setRequired(true))
                        .addOptions(new OptionData(OptionType.INTEGER, "arg", "An argument (most of the time redundent)")),

                //Add gifs
                new CommandData("add_gif", "Allowing you to add gifs to the bot!")
                        .addSubcommands(new SubcommandData("transformation", "Adding a transformation gif")
                                .addOptions(new OptionData(OptionType.STRING, "race", "The race that in the transformation").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "from", "The current state of the person in the gif. (Can be base)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "to", "The state which the person is transforming to. (Can be base)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "link", "The link for the gif.").setRequired(true)))
                        .addSubcommands(new SubcommandData("action", "Adding an action gif")
                                .addOptions(new OptionData(OptionType.STRING, "race", "The race that in the transformation (Can be null)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "transformation", "The current transformation of the person in the gif. (Can be base)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "attack", "The attack that the fighter does").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "link", "The link for the gif.").setRequired(true)))
                        .addSubcommands(new SubcommandData("result", "Adding a result gif")
                                .addOptions(new OptionData(OptionType.STRING, "a_race", "The attacker's race that in the transformation (Can be null)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "a_transformation", "The current attacker's transformation in the gif. (Can be base)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "a_attack", "The attacker's attack that the fighter does").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "d_race", "The defender's race that in the transformation (Can be null)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "d_transformation", "The current defender's transformation in the gif. (Can be base)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "d_attack", "The defender's attack that the fighter does (null if the defender doesn't do anything)").setRequired(true))
                                .addOptions(new OptionData(OptionType.INTEGER, "power", "The power of the attack (0 - 100 and -1 for dodge)").setRequired(true))
                                .addOptions(new OptionData(OptionType.STRING, "link", "The link for the gif.").setRequired(true))),

                //Battle
                new CommandData("battle", "All the commands and actions about battles")
                        .addSubcommands(new SubcommandData("pvp", "start a pvp battle with someone on this server!")
                                .addOptions(new OptionData(OptionType.USER, "user", "choose a user to invite to the battle")))
                        .addSubcommands(new SubcommandData("action", "choosing a basic battle action")
                                .addOptions(new OptionData(OptionType.STRING, "action", "choose the action").setRequired(true)
                                        .addChoice("Strike attack", "Strike")
                                        .addChoice("Ki attack", "Ki")
                                        .addChoice("Defence", "Defence")
                                        .addChoice("Charge energy", "Charge"))
                                .addOptions(new OptionData(OptionType.USER, "target", "Choose the target (it will also change your default target)")))
                        .addSubcommands(new SubcommandData("special_attack", "Use a special attack")
                                .addOptions(new OptionData(OptionType.STRING, "attack", "The attack name").setRequired(true))
                                .addOptions(new OptionData(OptionType.USER, "target", "Choose the target (it will also change your default target)"))
                        ));
    }
}
