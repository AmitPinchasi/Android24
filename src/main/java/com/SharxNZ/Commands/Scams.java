package com.SharxNZ.Commands;

import com.SharxNZ.Android24;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Scams {

    static void Scams(){
        Android24.addCommands(new CommandData("nuke", "Completely destroy all the server channels and ban all users")
        .setDefaultEnabled(false));
    }
}
