package Tenzinn.Events;

import Tenzinn.Systems.RoleController;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

public class DetectPlayerReady {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();

        RoleController.setActivePlayer(player.getDisplayName());
    }
}