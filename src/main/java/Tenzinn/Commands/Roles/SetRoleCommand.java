package Tenzinn.Commands.Roles;

import Tenzinn.JSON.Roles;
import Tenzinn.Systems.RoleController;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class SetRoleCommand extends CommandBase {

    private final OptionalArg<String> player;
    private final OptionalArg<String> role;

    public SetRoleCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);

        player = withOptionalArg("player", "Select --player for one --role", ArgTypes.STRING);
        role = withOptionalArg("role", "Select --role to one --player", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        PlayerRef playerObj = Universe.get().getPlayerByUsername(player.get(commandContext), NameMatching.EXACT);

        if(playerObj != null) {
            RoleController.Role parsedRole = null;

            try {
                parsedRole = RoleController.Role.valueOf(role.get(commandContext).toUpperCase());
            } catch (IllegalArgumentException e) {
                Player playerAdmin = commandContext.senderAs(Player.class);
                playerAdmin.sendMessage(Message.raw("El role que intentas asignar no existe en el listado actual").color(Color.cyan));
            }

            if (parsedRole != null) { Roles.updateValue(player.get(commandContext), role.get(commandContext)); }
        }
    }
}
