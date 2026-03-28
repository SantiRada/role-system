package Tenzinn.Commands.Roles;

import Tenzinn.Systems.RoleController;
import Tenzinn.UI.RoleListAdminPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class GetRoleCommand extends CommandBase {

    private final OptionalArg<String> role;

    public GetRoleCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);

        role = withOptionalArg("role", "View panel with filter to --role", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        Player player = commandContext.senderAs(Player.class);
        PlayerRef playerRef = Universe.get().getPlayerByUsername(player.getDisplayName(), NameMatching.EXACT);

        Ref<EntityStore> ref = player.getReference();
        Store<EntityStore> store = ref.getStore();

        String roleArg = role.get(commandContext);
        if (roleArg == null) {
            player.getPageManager().openCustomPage(ref, store, new RoleListAdminPage(playerRef, null));
        } else {
            RoleController.Role parsedRole = null;

            try {
                parsedRole = RoleController.Role.valueOf(roleArg);
            } catch (IllegalArgumentException e) {
                player.sendMessage(Message.raw("El role que intentas asignar no existe en el listado actual").color(Color.cyan));
            }

            if (parsedRole != null) {
                player.getPageManager().openCustomPage(ref, store, new RoleListAdminPage(playerRef, parsedRole));
            } else {
                player.sendMessage(Message.raw("No se encontró el rol --" + roleArg).color(Color.cyan));
            }
        }
    }
}
