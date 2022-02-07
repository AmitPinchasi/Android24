package com.SharxNZ.Commands.ModeretionCommands;

import com.SharxNZ.Android24;
import com.SharxNZ.Utilities.Server;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class refreshRoles extends Command {

    public refreshRoles() {
        super.name = "refreshRoles";
        super.aliases = new String[]{"rr", "rf rl"};
        super.help = "Refreshes the transformations roles base on the location of the role that sets them";
        super.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        try (
                Connection con = Android24.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT TransformationName FROM android24.transformations;");
        ) {
            Server server = new Server(commandEvent.getGuild().getIdLong());
            Role transRole = commandEvent.getJDA().getRoleById(server.getTransRole());
            if (transRole == null) {
                commandEvent.reply("You need to set a role for the transformations. You can do it with `/server_setup`");
                return;
            }
            ResultSet resultSet = statement.executeQuery();
            List<Role> tempRoleList;
            RoleOrderAction roleOrderAction = commandEvent.getGuild().modifyRolePositions();
            while (resultSet.next()) {
                tempRoleList = commandEvent.getGuild().getRolesByName(resultSet.getString(1), true);
                if (!tempRoleList.isEmpty())
                    roleOrderAction.selectPosition(tempRoleList.get(0))
                            .moveTo(transRole.getPosition() - 1);
            }
            roleOrderAction.queue();

            commandEvent.reply("The roles positions has been changed successfully!");

        } catch (SQLException throwables) {
            Android24.logError(throwables);
        }
    }
}
