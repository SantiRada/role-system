package Tenzinn.UI;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DiePage extends InteractiveCustomUIPage<DiePage.DiePageEventData> {

    private static final Map<PlayerRef, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();

    public ScheduledFuture<?> timerTask;
    private int timeToRespawn;
    private final int timeBaseToRespawn = 20;

    public DiePage(PlayerRef playerRef) { super(playerRef, CustomPageLifetime.CantClose, DiePageEventData.CODEC); }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append("DiePage.ui");

        cancelTimerForPlayer(playerRef);
        timeToRespawn = timeBaseToRespawn;

        commandBuilder.set("#RespawnButton.Disabled", true);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#RespawnButton", EventData.of("Action", "Respawn"));

        timerTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            timeToRespawn -= 1;

            // Nuevo builder limpio en cada tick
            UICommandBuilder tickBuilder = new UICommandBuilder();

            int minutes = timeToRespawn / 60;
            int seconds = timeToRespawn % 60;
            tickBuilder.set("#Timer.TextSpans", Message.raw(String.format("%02d:%02d", minutes, seconds)));

            if (timeToRespawn <= 0) {
                stopTimer();
                tickBuilder.set("#Timer.Visible", false);
                tickBuilder.set("#RespawnButton.Disabled", false);
            }

            sendUpdate(tickBuilder, false);
        }, 1, 1, TimeUnit.SECONDS);

        activeTimers.put(playerRef, timerTask);
    }

    public static void cancelTimerForPlayer(PlayerRef playerRef) {
        ScheduledFuture<?> existing = activeTimers.remove(playerRef);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }
    }

    private void stopTimer() {
        cancelTimerForPlayer(playerRef);
        timerTask = null;
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull DiePageEventData data) {
        if ("Respawn".equals(data.action)) {
            if (timeToRespawn > 0) {

                return;
            }

            // Deshabilitar el botón para evitar doble click (igual que RespawnPage)
            UICommandBuilder commandBuilder = new UICommandBuilder();
            commandBuilder.set("#RespawnButton.Disabled", true);
            sendUpdate(commandBuilder, false);

            // Respawnear y cerrar la UI
            CompletableFutureUtil._catch(DeathComponent.respawn(store, ref).thenCompose((v) -> {
                Ref<EntityStore> currentRef = playerRef.getReference();

                if (currentRef != null && currentRef.isValid()) {
                    return CompletableFuture.runAsync(() -> {
                        if (currentRef.isValid()) {
                            Player playerComponent = (Player) currentRef.getStore().getComponent(currentRef, Player.getComponentType());
                            if (playerComponent != null && playerComponent.getPageManager().getCustomPage() == this) {
                                playerComponent.getPageManager().setPage(currentRef, currentRef.getStore(), Page.None);
                            }
                        }
                    }, ((EntityStore) currentRef.getStore().getExternalData()).getWorld());
                }
                return CompletableFuture.completedFuture(null);})
            );
        }
    }

    // --- Event Data ---
    public static class DiePageEventData {
        public static final BuilderCodec CODEC = BuilderCodec
                .builder(DiePageEventData.class, DiePageEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING),
                        (entry, s) -> entry.action = s,
                        (entry) -> entry.action)
                .add()
                .build();

        private String action;
    }
}