package Tenzinn.Events;

import Tenzinn.UI.DiePage;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Set;

public class DetectRejoinDead extends HolderSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Player.getComponentType(),
                PlayerRef.getComponentType(),
                DeathComponent.getComponentType()
        );
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.BEFORE, PlayerSystems.PlayerAddedSystem.class));
    }

    @Override
    public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason,
                            @Nonnull Store<EntityStore> store) {

        DeathComponent deathComponent = (DeathComponent) holder.getComponent(DeathComponent.getComponentType());
        if (deathComponent == null) return;

        PlayerRef playerRef = (PlayerRef) holder.getComponent(PlayerRef.getComponentType());
        if (playerRef == null) return;

        // Remover DeathComponent del holder ANTES de que entre al store
        // Así PlayerAddedSystem no lo ve y no abre RespawnPage
        holder.removeComponent(DeathComponent.getComponentType());

        // Abrir nuestra UI cuando el cliente esté listo
        var world = store.getExternalData() instanceof EntityStore es ? es.getWorld() : null;
        if (world == null) return;

        world.execute(() -> {
            Ref<EntityStore> currentRef = playerRef.getReference();
            if (currentRef == null || !currentRef.isValid()) return;

            Store<EntityStore> currentStore = currentRef.getStore();
            Player currentPlayer = (Player) currentStore.getComponent(currentRef, Player.getComponentType());
            if (currentPlayer == null) return;

            // Re-agregar el DeathComponent para que el jugador siga muerto
            currentStore.addComponent(currentRef, DeathComponent.getComponentType(), deathComponent);

            // Abrir nuestra UI
            currentPlayer.getPageManager().openCustomPage(currentRef, currentStore, new DiePage(playerRef));
        });
    }

    @Override
    public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason,
                                @Nonnull Store<EntityStore> store) {
    }
}