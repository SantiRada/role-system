package Tenzinn.Systems;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.asset.type.gameplay.respawn.RespawnController;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class CurrentPositionRespawnController implements RespawnController {

    public static final CurrentPositionRespawnController INSTANCE = new CurrentPositionRespawnController();

    @Override
    public CompletableFuture<Void> respawnPlayer(@Nonnull World world, @Nonnull Ref<EntityStore> playerReference, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        TransformComponent transform = (TransformComponent) componentAccessor.getComponent(playerReference, TransformComponent.getComponentType());
        Transform currentTransform;

        if (transform != null) {
            Vector3d pos = transform.getPosition();
            Vector3f rot = new Vector3f(0, 0, 0);
            currentTransform = new Transform(new Vector3d(pos.x, pos.y, pos.z), rot);
        } else { return com.hypixel.hytale.server.core.asset.type.gameplay.respawn.HomeOrSpawnPoint.INSTANCE.respawnPlayer(world, playerReference, componentAccessor); }

        return CompletableFuture.runAsync(() -> {
            if (playerReference.isValid()) {
                Teleport teleportComponent = Teleport.createForPlayer(currentTransform);
                playerReference.getStore().addComponent(playerReference, Teleport.getComponentType(), teleportComponent);
            }
        }, world);
    }
}