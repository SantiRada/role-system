package Tenzinn.Events;

import Tenzinn.UI.DiePage;
import Tenzinn.Systems.RoleController;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DetectPlayerDisconnect extends PlayerDisconnectEvent {

    private PlayerRef playerRef;

    public DetectPlayerDisconnect(@NonNullDecl PlayerRef playerRef) { super(playerRef); this.playerRef = playerRef; }
    public void onPlayerDisconnect() {
        Ref<EntityStore> ref = getPlayerRef().getReference();
        assert ref != null;

        Store<EntityStore> store = ref.getStore();
        Player player = store.getComponent(ref, Player.getComponentType());
        assert player != null;

        RoleController.setInactivePlayer(player.getDisplayName());

        DiePage.cancelTimerForPlayer(playerRef);
    }
}
