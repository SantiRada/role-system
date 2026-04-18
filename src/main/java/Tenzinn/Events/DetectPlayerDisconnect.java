package Tenzinn.Events;

import Tenzinn.UI.DiePage;
import Tenzinn.Systems.RoleController;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;

public class DetectPlayerDisconnect {

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef == null) return;

        Player player = playerRef.getReference() != null ? playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType()) : null;
        if (player != null) { RoleController.setInactivePlayer(player.getDisplayName()); }

        DiePage.cancelTimerForPlayer(playerRef);
    }
}