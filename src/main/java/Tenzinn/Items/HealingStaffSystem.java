package Tenzinn.Items;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;

import javax.annotation.Nullable;

public class HealingStaffSystem {

    public static final float  MAX_RANGE  = 8.0f;

    @Nullable
    public static Player getLookedAtPlayer(PlayerRef shooterRef, Store<EntityStore> store) {

        TransformComponent shooterTransform = store.getComponent(shooterRef.getReference(), TransformComponent.getComponentType());
        if (shooterTransform == null) return null;

        Vector3d  origin = shooterTransform.getPosition();
        Direction look   = shooterTransform.getSentTransform().lookOrientation;
        if (look == null) return null;

        double dirX = -Math.sin(look.yaw)  * Math.cos(look.pitch);
        double dirY =  Math.sin(look.pitch);
        double dirZ = -Math.cos(look.yaw)  * Math.cos(look.pitch);
        double len  = Math.sqrt(dirX*dirX + dirY*dirY + dirZ*dirZ);
        if (len == 0) return null;
        dirX /= len; dirY /= len; dirZ /= len;

        double eyeX = origin.x;
        double eyeY = origin.y + 1.62;
        double eyeZ = origin.z;

        final double HALF_W = 0.3;
        final double HEIGHT = 1.8;
        final double fDirX  = dirX, fDirY = dirY, fDirZ = dirZ;

        final Player[]  closest  = { null };
        final double[]  closestT = { Double.MAX_VALUE };

        store.forEachChunk((chunk, buffer) -> {
            for (int i = 0; i < chunk.size(); i++) {
                Ref<EntityStore> ref = chunk.getReferenceTo(i);

                Player    candidate    = store.getComponent(ref, Player.getComponentType());
                PlayerRef candidateRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (candidate == null || candidateRef == null) continue;
                if (candidateRef.equals(shooterRef)) continue;

                TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                if (t == null) continue;

                Vector3d pos = t.getPosition();

                double minX = pos.x - HALF_W, maxX = pos.x + HALF_W;
                double minY = pos.y,           maxY = pos.y + HEIGHT;
                double minZ = pos.z - HALF_W,  maxZ = pos.z + HALF_W;

                double tMin = 0, tMax = MAX_RANGE;

                if (Math.abs(fDirX) > 1e-8) {
                    double t1 = (minX - eyeX) / fDirX, t2 = (maxX - eyeX) / fDirX;
                    if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                    tMin = Math.max(tMin, t1); tMax = Math.min(tMax, t2);
                } else if (eyeX < minX || eyeX > maxX) continue;

                if (Math.abs(fDirY) > 1e-8) {
                    double t1 = (minY - eyeY) / fDirY, t2 = (maxY - eyeY) / fDirY;
                    if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                    tMin = Math.max(tMin, t1); tMax = Math.min(tMax, t2);
                } else if (eyeY < minY || eyeY > maxY) continue;

                if (Math.abs(fDirZ) > 1e-8) {
                    double t1 = (minZ - eyeZ) / fDirZ, t2 = (maxZ - eyeZ) / fDirZ;
                    if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                    tMin = Math.max(tMin, t1); tMax = Math.min(tMax, t2);
                } else if (eyeZ < minZ || eyeZ > maxZ) continue;

                if (tMax < tMin || tMax < 0) continue;

                if (tMin < closestT[0]) {
                    closestT[0] = tMin;
                    closest[0]  = candidate;
                }
            }
        });

        return closest[0];
    }

    @Nullable
    public static PlayerRef getPlayerRef(Player target, Store<EntityStore> store) {
        final PlayerRef[] result = { null };
        store.forEachChunk((chunk, buffer) -> {
            for (int i = 0; i < chunk.size(); i++) {
                Ref<EntityStore> ref = chunk.getReferenceTo(i);
                Player p = store.getComponent(ref, Player.getComponentType());
                if (p == target) {
                    result[0] = store.getComponent(ref, PlayerRef.getComponentType());
                    return;
                }
            }
        });
        return result[0];
    }
}