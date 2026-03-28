package Tenzinn.UI;

import Tenzinn.UI.Events.BasicEvent;
import Tenzinn.Systems.RoleController;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;
import java.util.Collection;

public class RoleListAdminPage extends InteractiveCustomUIPage<BasicEvent> {

    private UICommandBuilder uiBuilder;
    private String filterRole;

    public RoleListAdminPage(PlayerRef playerRef, RoleController.Role role) {
        super(playerRef, CustomPageLifetime.CanDismiss, Tenzinn.UI.Events.BasicEvent.CODEC);
        filterRole = (role != null) ? role.toString().toUpperCase() : "";
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref,@NonNullDecl UICommandBuilder uiCommandBuilder,@NonNullDecl UIEventBuilder uiEventBuilder,@NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("ListPlayers.ui");
        uiBuilder = uiCommandBuilder;

        Collection<PlayerRef> onlinePlayers = Universe.get().getPlayers();
        for (PlayerRef player : onlinePlayers) { loadContent(player); }

        if(!filterRole.isEmpty()) uiBuilder.set("#Title.TextSpans", Message.raw("List Players [role: " + filterRole + "]"));

        sendUpdate();
    }
    private void loadContent(PlayerRef player) {
        String name = player.getUsername();

        String role = RoleController.getOnePlayer(name).toUpperCase();
        String status = RoleController.getStatusPlayer(name);

        if(!filterRole.isEmpty()) { if(!role.toUpperCase().equalsIgnoreCase(filterRole)) return; }

        String inlineUI =
                "Group {\n" +
                        "    FlexWeight: 1;\n" +
                        "    LayoutMode: Middle;\n" +
                        "    Padding: (Horizontal: 24);\n" +
                        "    Anchor: (Height: 60, Bottom: 6);\n" +
                        "    Background: (Color: #FFFFFF(0.05));\n" +
                        "\n" +
                        "    Group {\n" +
                        "        LayoutMode: Left;\n" +
                        "\n" +
                        "        Label {\n" +
                        "            Text: \"" + escapeUiString(name) + "\";\n" +
                        "            Anchor: (Right: 12, Width: 200);\n" +
                        "            Style: (TextColor: #FFFFFF, RenderBold: true, FontSize: 18);\n" +
                        "        }\n" +
                        "\n" +
                        "        " +
                        "Label { FlexWeight: 2; Text: \"" + status + "\"; " +
                        "Style: (TextColor: #FFFFFF, FontSize: 14, Alignment: Center); }\n" +
                        "        " +
                        "Label { FlexWeight: 2; Text: \"" + role + "\"; " +
                        "Style: (TextColor: #FFFFFF, FontSize: 14, Alignment: Center); }\n" +
                        "    }\n" +
                        "}";

        uiBuilder.appendInline("#Users", inlineUI);
    }
    private static String escapeUiString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}