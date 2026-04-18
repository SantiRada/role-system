package Tenzinn.Interactions;

import Tenzinn.Systems.RoleController;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.awt.Color;
import javax.annotation.Nullable;

public class DetectBenchInteract extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    private static final String BENCH_BLOCK_ID = "Medicbench";

    public DetectBenchInteract() { super(UseBlockEvent.Pre.class); }
    @Override
    public void handle(int i, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, UseBlockEvent.Pre event) {
        if (!BENCH_BLOCK_ID.equals(event.getBlockType().getId())) return;

        Ref<EntityStore> ref = chunk.getReferenceTo(i);

        Player    player    = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || playerRef == null) return;

        boolean isMedic = "MEDIC".equals(RoleController.getOnePlayer(player.getDisplayName()));

        if (!isMedic) {
            event.setCancelled(true);
            playerRef.sendMessage(Message.raw("If you are not a DOCTOR, you cannot use this bench.").color(Color.YELLOW));
        }
    }
    @Nullable @Override
    public Query<EntityStore> getQuery() { return PlayerRef.getComponentType(); }
}