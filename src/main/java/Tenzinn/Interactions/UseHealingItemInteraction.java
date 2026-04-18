package Tenzinn.Interactions;

import Tenzinn.RoleSystem;
import Tenzinn.Items.HealingItem;
import Tenzinn.Systems.RoleController;
import Tenzinn.Items.HealingStaffSystem;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;

import java.util.Map;
import java.util.UUID;
import java.awt.Color;
import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UseHealingItemInteraction extends SimpleInstantInteraction {

    private final HealingItem healingItem;

    public static final BuilderCodec<UseHealingItemInteraction> CODEC_BANDAGE = BuilderCodec.builder(UseHealingItemInteraction.class,
                    () -> new UseHealingItemInteraction(HealingItem.BANDAGE), SimpleInstantInteraction.CODEC).build();

    public static final BuilderCodec<UseHealingItemInteraction> CODEC_MEDICAL_KIT = BuilderCodec.builder(UseHealingItemInteraction.class,
                    () -> new UseHealingItemInteraction(HealingItem.MEDICAL_KIT), SimpleInstantInteraction.CODEC).build();

    private static final Map<UUID, ScheduledFuture<?>> activeHeals = new ConcurrentHashMap<>();

    public UseHealingItemInteraction(HealingItem healingItem) {
        this.healingItem = healingItem;
    }

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        Player player = context.getCommandBuffer().getComponent(context.getEntity(), Player.getComponentType());
        if (player == null) { context.getState().state = InteractionState.Failed; return; }

        PlayerRef playerRef = context.getCommandBuffer().getComponent(context.getEntity(), PlayerRef.getComponentType());
        if (playerRef == null) { context.getState().state = InteractionState.Failed; return; }

        String  username = player.getDisplayName();
        String  role     = RoleController.getOnePlayer(username);
        boolean isMedic  = "MEDIC".equals(role);

        // Punto 2 — bloquear MedicalKit si no es MEDIC, devolver el ítem
        if (healingItem.medicOnly && !isMedic) {
            devolverItem(player);
            playerRef.sendMessage(Message.raw("Only doctors can use " + healingItem.itemId + ".").color(Color.RED));
            context.getState().state = InteractionState.Failed;
            return;
        }

        UUID uuid = playerRef.getUuid();
        if (activeHeals.containsKey(uuid)) {
            devolverItem(player);
            playerRef.sendMessage(Message.raw("You are already applying a cure.").color(Color.YELLOW));
            context.getState().state = InteractionState.Failed;
            return;
        }

        Store<EntityStore> store = playerRef.getReference().getStore();
        World world = store.getExternalData().getWorld();

        Player  target      = HealingStaffSystem.getLookedAtPlayer(playerRef, store);
        boolean healingSelf = (target == null) || !isMedic;

        if (target != null && !isMedic) {
            healingSelf = true;
            target = null;
        }

        final Player  fTarget = target;
        final boolean fSelf   = healingSelf;
        String targetName     = fSelf ? "You" : fTarget.getDisplayName();

        PlayerRef targetRef = fSelf ? playerRef : HealingStaffSystem.getPlayerRef(fTarget, store);
        if (targetRef == null) { context.getState().state = InteractionState.Failed; return; }

        // Punto 3 — avisar si el target ya tiene vida al 100%, devolver el ítem
        boolean targetIsDead = !fSelf && fTarget != null
                && RoleSystem.DEAD_PLAYERS.contains(targetRef.getUuid());

        if (!targetIsDead) {
            EntityStatMap statMap = store.getComponent(targetRef.getReference(),
                    EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap != null) {
                EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
                if (health != null && health.get() >= health.getMax()) {
                    devolverItem(player);
                    playerRef.sendMessage(Message.raw(fSelf
                            ? "Your health is already at maximum, the item will have no effect."
                            : targetName + " already has full health, the item will have no effect."
                    ).color(Color.YELLOW));
                    context.getState().state = InteractionState.Failed;
                    return;
                }
            }
        }
        final PlayerRef fTargetRef = targetRef;
        final PlayerRef fPlayerRef = playerRef;

        // ── MedicalKit: lógica especial con revive ────────────────────
        if (healingItem == HealingItem.MEDICAL_KIT) {
            applyMedicalKit(uuid, fPlayerRef, fTargetRef, fTarget, fSelf, targetName, store, world, username);
            context.getState().state = InteractionState.Finished;
            return;
        }

        // ── Bandage: lógica estándar ──────────────────────────────────
        float healPercent = fSelf ? healingItem.getHealSelfPercent(isMedic) : healingItem.getHealOtherPercent();
        int   timeToHeal  = healingItem.getTimeToHeal(isMedic);

        world.execute(() -> {
            EntityStatMap statMap = store.getComponent(fTargetRef.getReference(),
                    EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return;

            EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
            if (health == null) return;

            if (health.get() >= health.getMax()) {
                fPlayerRef.sendMessage(Message.raw(fSelf ? "You're already living life to the fullest." : "The player " + fTarget.getDisplayName() + " already has a full life.").color(Color.YELLOW));
                return;
            }

            float totalHeal   = health.getMax() * healPercent;
            float healPerTick = totalHeal / timeToHeal;

            fPlayerRef.sendMessage(Message.raw(
                    "Applying a cure to " + targetName + "... (" + timeToHeal + "s)"
            ).color(Color.CYAN));

            int[] ticksRemaining = { timeToHeal };

            ScheduledFuture<?> task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
                ticksRemaining[0]--;

                world.execute(() -> {
                    EntityStatMap sm = store.getComponent(fTargetRef.getReference(), EntityStatsModule.get().getEntityStatMapComponentType());
                    if (sm == null) return;

                    EntityStatValue currentHealth = sm.get(DefaultEntityStatTypes.getHealth());
                    if (currentHealth == null) return;

                    if (currentHealth.get() >= currentHealth.getMax()) {
                        ScheduledFuture<?> self = activeHeals.remove(uuid);
                        if (self != null) self.cancel(false);
                        fPlayerRef.sendMessage(Message.raw(targetName + "'s life is already at its maximum.").color(Color.GREEN));
                        if (!fSelf && fTarget != null) {
                            fTargetRef.sendMessage(Message.raw("Your life is at its best.").color(Color.GREEN));
                        }
                        return;
                    }

                    sm.addStatValue(DefaultEntityStatTypes.getHealth(), healPerTick);

                    if (ticksRemaining[0] <= 0) {
                        ScheduledFuture<?> self = activeHeals.remove(uuid);
                        if (self != null) self.cancel(false);
                        fPlayerRef.sendMessage(Message.raw("Cure completed in " + targetName + ".").color(Color.GREEN));
                        if (!fSelf) { fTargetRef.sendMessage(Message.raw(username + " finished healing you.").color(Color.GREEN)); }
                    }
                });

            }, 1000L, 1000L, TimeUnit.MILLISECONDS);

            activeHeals.put(uuid, task);
        });

        context.getState().state = InteractionState.Finished;
    }
    private void devolverItem(Player player) {
        com.hypixel.hytale.server.core.inventory.ItemStack item = new com.hypixel.hytale.server.core.inventory.ItemStack(healingItem.itemId, 1);
        player.getInventory().getCombinedHotbarFirst().addItemStack(item);
    }
    private void applyMedicalKit(UUID uuid, PlayerRef playerRef, PlayerRef targetRef, Player target, boolean healingSelf, String targetName, Store<EntityStore> store, World world, String username) {
        final int TIME_TO_HEAL = 20;

        boolean targetIsDead = !healingSelf && target != null && RoleSystem.DEAD_PLAYERS.contains(targetRef.getUuid());

        // Porcentaje de cura según situación
        float healPercent = (targetIsDead) ? 0.75f : 1.0f;

        if (targetIsDead) {
            // ── Revivir primero, luego curar ─────────────────────────
            playerRef.sendMessage(Message.raw("Reviving " + targetName + "... (20s)").color(Color.CYAN));
            target.sendMessage(Message.raw(username + " is reviving you... (20s)").color(Color.CYAN));

            int[] ticksRemaining = { TIME_TO_HEAL };
            final PlayerRef fTargetRef = targetRef;
            final PlayerRef fPlayerRef = playerRef;

            ScheduledFuture<?> task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
                ticksRemaining[0]--;

                if (ticksRemaining[0] <= 0) {
                    ScheduledFuture<?> self = activeHeals.remove(uuid);
                    if (self != null) self.cancel(false);

                    // Revivir — sin TP, queda en su posición actual
                    world.execute(() -> {
                        Ref<EntityStore> targetEntityRef = fTargetRef.getReference();
                        if (targetEntityRef == null || !targetEntityRef.isValid()) return;

                        Store<EntityStore> targetStore = targetEntityRef.getStore();

                        DeathComponent deathComp = (DeathComponent) targetStore.getComponent(targetEntityRef, DeathComponent.getComponentType());
                        if (deathComp == null) return;

                        // 1. Cancelar timer y cerrar DiePage
                        Tenzinn.UI.DiePage.cancelTimerForPlayer(fTargetRef);
                        Player targetPlayer = (Player) targetStore.getComponent(
                                targetEntityRef, Player.getComponentType());
                        if (targetPlayer != null) {
                            targetPlayer.getPageManager().setPage(targetEntityRef, targetStore, com.hypixel.hytale.protocol.packets.interface_.Page.None);
                        }

                        // 2. Leer el máximo de vida antes del reset
                        EntityStatMap sm = targetStore.getComponent(targetEntityRef, EntityStatsModule.get().getEntityStatMapComponentType());
                        float maxHealth = 100f;
                        if (sm != null) { EntityStatValue h = sm.get(DefaultEntityStatTypes.getHealth()); if (h != null) maxHealth = h.getMax(); }

                        final float fMaxHealth  = maxHealth;
                        final float initialHeal = fMaxHealth * 0.10f;  // 10% inmediato
                        final float remainHeal  = fMaxHealth * 0.65f;  // 65% progresivo en 10s

                        // 3. Remover DeathComponent y aplicar 10% inmediato en el mismo tick
                        targetStore.tryRemoveComponent(targetEntityRef, DeathComponent.getComponentType());
                        if (sm != null) { sm.setStatValue(DefaultEntityStatTypes.getHealth(), initialHeal); }

                        // 4. Sacar de DEAD_PLAYERS
                        RoleSystem.DEAD_PLAYERS.remove(fTargetRef.getUuid());

                        // 5. Aplicar EntityEffect de movilidad reducida
                        com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent effectController =
                                targetStore.getComponent(targetEntityRef, com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent.getComponentType());

                        if (effectController != null) {
                            int effectIndex = com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect
                                    .getAssetMap().getIndex("ReviveDebuff");
                            com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect effect =
                                    com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect.getAssetMap().getAsset(effectIndex);

                            if (effect != null) { effectController.addEffect(targetEntityRef, effect, targetStore); }
                        }

                        // 6. Mensajes
                        fPlayerRef.sendMessage(Message.raw("You revived " + targetName + " — recuperando vida...").color(Color.GREEN));
                        fTargetRef.sendMessage(Message.raw(username + " revived you. Regaining life in 10s...").color(Color.GREEN));

                        // 7. Curación progresiva del 65% en 10 ticks
                        final int REGEN_TICKS   = 10;
                        final float healPerTick = remainHeal / REGEN_TICKS;
                        final int[] ticksLeft   = { REGEN_TICKS };

                        final ScheduledFuture<?>[] regenTask = { null };

                        regenTask[0] = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
                            ticksLeft[0]--;

                            world.execute(() -> {
                                Ref<EntityStore> ref2 = fTargetRef.getReference();
                                if (ref2 == null || !ref2.isValid()) { if (regenTask[0] != null) regenTask[0].cancel(false); return; }

                                EntityStatMap sm2 = ref2.getStore().getComponent(ref2, EntityStatsModule.get().getEntityStatMapComponentType());
                                if (sm2 == null) return;

                                sm2.addStatValue(DefaultEntityStatTypes.getHealth(), healPerTick);

                                if (ticksLeft[0] <= 0) {
                                    if (regenTask[0] != null) regenTask[0].cancel(false);
                                    fTargetRef.sendMessage(Message.raw("Full recovery.").color(Color.GREEN));
                                }
                            });

                        }, 1000L, 1000L, TimeUnit.MILLISECONDS);
                    });
                }

            }, 1000L, 1000L, TimeUnit.MILLISECONDS);

            activeHeals.put(uuid, task);

        } else {
            // ── Curar al 100% (vivo, propio o ajeno) ─────────────────
            world.execute(() -> {
                EntityStatMap statMap = store.getComponent(targetRef.getReference(), EntityStatsModule.get().getEntityStatMapComponentType());
                if (statMap == null) return;

                EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
                if (health == null) return;

                if (health.get() >= health.getMax()) {
                    playerRef.sendMessage(Message.raw(healingSelf ? "You're already living life to the fullest." : target.getDisplayName() + " already has life at its peak.").color(Color.YELLOW));
                    return;
                }

                float totalHeal   = health.getMax() * healPercent;
                float healPerTick = totalHeal / TIME_TO_HEAL;

                playerRef.sendMessage(Message.raw("Applying MedicalKit to " + targetName + "... (" + TIME_TO_HEAL + "s)").color(Color.CYAN));

                int[] ticksRemaining = { TIME_TO_HEAL };
                final Player fTarget = target;
                final PlayerRef fTargetRef = targetRef;
                final PlayerRef fPlayerRef = playerRef;

                ScheduledFuture<?> task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
                    ticksRemaining[0]--;

                    world.execute(() -> {
                        EntityStatMap sm = store.getComponent(fTargetRef.getReference(),
                                EntityStatsModule.get().getEntityStatMapComponentType());
                        if (sm == null) return;

                        EntityStatValue currentHealth = sm.get(DefaultEntityStatTypes.getHealth());
                        if (currentHealth == null) return;

                        if (currentHealth.get() >= currentHealth.getMax()) {
                            ScheduledFuture<?> self = activeHeals.remove(uuid);
                            if (self != null) self.cancel(false);
                            fPlayerRef.sendMessage(Message.raw("Priest arrested — " + targetName + " already has life at its peak.").color(Color.GREEN));
                            if (!healingSelf && fTarget != null) { fTargetRef.sendMessage(Message.raw("Your life is at its peak, the cure has stopped.").color(Color.GREEN)); }
                            return;
                        }

                        sm.addStatValue(DefaultEntityStatTypes.getHealth(), healPerTick);

                        if (ticksRemaining[0] <= 0) {
                            ScheduledFuture<?> self = activeHeals.remove(uuid);
                            if (self != null) self.cancel(false);
                            fPlayerRef.sendMessage(Message.raw("Cure completed in " + targetName + ".").color(Color.GREEN));
                            if (!healingSelf && fTarget != null) {
                                fTargetRef.sendMessage(Message.raw(username + " finished healing you.").color(Color.GREEN));
                            }
                        }
                    });

                }, 1000L, 1000L, TimeUnit.MILLISECONDS);

                activeHeals.put(uuid, task);
            });
        }
    }
}