package Tenzinn;

import Tenzinn.JSON.Roles;
import Tenzinn.Events.DetectDiePlayer;
import Tenzinn.Commands.RespawnCommand;
import Tenzinn.Events.DetectRejoinDead;
import Tenzinn.Events.DetectPlayerReady;
import Tenzinn.Commands.Roles.RoleCommands;
import Tenzinn.Events.DetectPlayerDisconnect;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public class RoleSystem extends JavaPlugin {

    public static final Set<UUID> DEAD_PLAYERS = ConcurrentHashMap.newKeySet();

    public RoleSystem(@Nonnull JavaPluginInit init) { super(init); }

    @Override
    protected void setup() {
        Roles.load();

        // Events
        this.getEntityStoreRegistry().registerSystem(new DetectDiePlayer());
        this.getEntityStoreRegistry().registerSystem(new DetectRejoinDead());
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, DetectPlayerReady::onPlayerReady);
        this.getEventRegistry().register(PlayerDisconnectEvent.class, DetectPlayerDisconnect::onPlayerDisconnect);

        // Commands
        this.getCommandRegistry().registerCommand(new RoleCommands("roles", "Manage content for all player roles."));
        this.getCommandRegistry().registerCommand(new RespawnCommand("respawn", "Respawn to Player"));
    }
}