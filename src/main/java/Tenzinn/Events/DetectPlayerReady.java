package Tenzinn.Events;

import Tenzinn.RoleSystem;
import Tenzinn.UI.DiePage;
import Tenzinn.Systems.RoleController;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

import java.util.UUID;

public class DetectPlayerReady {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        RoleController.setActivePlayer(player.getDisplayName());

        PlayerRef playerRef = Universe.get().getPlayerByUsername(player.getDisplayName(), NameMatching.EXACT);
        assert playerRef != null;
        UUID uuid = playerRef.getUuid();

        if (!RoleSystem.DEAD_PLAYERS.contains(uuid)) return;

        assert player.getWorld() != null;
        player.getWorld().execute(() -> {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) return;

            Store<EntityStore> store = ref.getStore();
            player.getPageManager().openCustomPage(ref, store, new DiePage(playerRef));
        });
    }
}