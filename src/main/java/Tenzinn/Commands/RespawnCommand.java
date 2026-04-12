package Tenzinn.Commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class RespawnCommand extends CommandBase {
    public RespawnCommand(@NonNullDecl String name, @NonNullDecl String description) { super(name, description); }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        Player player = commandContext.senderAs(Player.class);

        assert player.getWorld() != null;
        World currentWorld = Universe.get().getWorld(player.getWorld().getName());
        assert currentWorld != null;
        currentWorld.execute(() -> {
            PlayerRef playerRef = Universe.get().getPlayerByUsername(player.getDisplayName(), NameMatching.EXACT);
            assert playerRef != null;
            Ref<EntityStore> ref = playerRef.getReference();
            assert ref != null;
            Store<EntityStore> store = ref.getStore();

            CompletableFutureUtil._catch(DeathComponent.respawn(store, ref).thenCompose((v) -> {
                Ref<EntityStore> currentRef = playerRef.getReference();

                if (currentRef != null && currentRef.isValid()) {
                    return CompletableFuture.runAsync(() -> {
                        if (currentRef.isValid()) {
                            player.getPageManager().setPage(currentRef, currentRef.getStore(), Page.None);
                        }
                    }, ((EntityStore) currentRef.getStore().getExternalData()).getWorld());
                }
                return CompletableFuture.completedFuture(null);})
            );
        });
    }
}