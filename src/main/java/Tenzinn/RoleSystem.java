package Tenzinn;

import Tenzinn.Events.DetectPlayerDisconnect;
import Tenzinn.Events.DetectPlayerReady;
import Tenzinn.Commands.Roles.RoleCommands;

import Tenzinn.JSON.Roles;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

import javax.annotation.Nonnull;

public class RoleSystem extends JavaPlugin {

    public RoleSystem(@Nonnull JavaPluginInit init) { super(init); }

    @Override
    protected void setup() {
        // JSON Loads
        Roles.load();

        // Events
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, DetectPlayerReady::onPlayerReady);
        this.getEventRegistry().register(PlayerDisconnectEvent.class, DetectPlayerDisconnect::onPlayerDisconnect);

        // Commands
        this.getCommandRegistry().registerCommand(new RoleCommands("roles", "Manage content for all player roles."));
    }
}