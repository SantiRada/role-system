package Tenzinn.Events;

import Tenzinn.UI.DiePage;
import Tenzinn.RoleSystem;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.dependency.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;

import java.util.Set;
import javax.annotation.Nonnull;

public class DetectDiePlayer extends DeathSystems.OnDeathSystem {

    @Nonnull @Override
    public Query<EntityStore> getQuery() { return Query.and(Player.getComponentType(), PlayerRef.getComponentType()); }
    @Nonnull @Override
    public Set<Dependency<EntityStore>> getDependencies() { return Set.of(new SystemDependency(Order.BEFORE, DeathSystems.PlayerDeathScreen.class)); }
    @Override
    public void onComponentAdded(@Nonnull Ref ref, @Nonnull DeathComponent component, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
        Player victim = (Player) store.getComponent(ref, Player.getComponentType());
        if (victim == null) return;

        PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        RoleSystem.DEAD_PLAYERS.add(playerRef.getUuid());

        // Deshabilitar la RespawnPage nativa
        component.setShowDeathMenu(false);
        victim.getPageManager().openCustomPage(ref, store, new DiePage(playerRef));
    }
    @Override
    public void onComponentRemoved(@Nonnull Ref ref, @Nonnull DeathComponent component, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
        PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef != null) { RoleSystem.DEAD_PLAYERS.remove(playerRef.getUuid()); }
    }
}