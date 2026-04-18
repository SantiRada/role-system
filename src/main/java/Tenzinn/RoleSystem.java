package Tenzinn;

import Tenzinn.JSON.Roles;
import Tenzinn.Events.DetectDiePlayer;
import Tenzinn.Commands.RespawnCommand;
import Tenzinn.Events.DetectRejoinDead;
import Tenzinn.Events.DetectPlayerReady;
import Tenzinn.Commands.Roles.RoleCommands;
import Tenzinn.Events.DetectPlayerDisconnect;
import Tenzinn.Interactions.DetectBenchInteract;
import Tenzinn.Interactions.UseHealingItemInteraction;

import Tenzinn.Systems.CurrentPositionRespawnController;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;

import java.awt.*;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public class RoleSystem extends JavaPlugin {

    public static final Set<UUID> DEAD_PLAYERS = ConcurrentHashMap.newKeySet();

    public RoleSystem(@Nonnull JavaPluginInit init) { super(init); }

    @Override
    protected  void start() {
        getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent.class, event -> {
                    try {
                        World world = event.getWorld();
                        com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig deathConfig = world.getDeathConfig();

                        java.lang.reflect.Field field = com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig.class.getDeclaredField("respawnController");
                        field.setAccessible(true);
                        field.set(deathConfig, CurrentPositionRespawnController.INSTANCE);
                    } catch (Exception e) { e.printStackTrace(); }
                }
        );
    }

    @Override
    protected void setup() {
        this.getCodecRegistry(Interaction.CODEC).register("use_bandage", UseHealingItemInteraction.class, UseHealingItemInteraction.CODEC_BANDAGE);
        this.getCodecRegistry(Interaction.CODEC).register("use_medical_kit", UseHealingItemInteraction.class, UseHealingItemInteraction.CODEC_MEDICAL_KIT);

        Roles.load();

        // Events
        this.getEntityStoreRegistry().registerSystem(new DetectDiePlayer());
        this.getEntityStoreRegistry().registerSystem(new DetectRejoinDead());
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, DetectPlayerReady::onPlayerReady);
        this.getEventRegistry().register(PlayerDisconnectEvent.class, DetectPlayerDisconnect::onPlayerDisconnect);

        // Event of Items
        this.getEntityStoreRegistry().registerSystem(new DetectBenchInteract());
        // Commands
        this.getCommandRegistry().registerCommand(new RoleCommands("roles", "Manage content for all player roles."));
        this.getCommandRegistry().registerCommand(new RespawnCommand("respawn", "Respawn to Player"));
    }
}