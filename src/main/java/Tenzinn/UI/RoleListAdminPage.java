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
        String name   = player.getUsername();
        String role   = RoleController.getOnePlayer(name).toUpperCase();
        String status = RoleController.getStatusPlayer(name);

        if (!filterRole.isEmpty() && !role.equalsIgnoreCase(filterRole)) return;

        // Color según estado
        String statusColor = switch (status) {
            case "ACTIVE"     -> "#5ECC7B";  // verde
            case "UNDEFINED"  -> "#E0A752";  // amarillo
            default           -> "#8B9BAE";  // gris
        };

        // Color según rol
        String roleColor = switch (role) {
            case "MEDIC"    -> "#52A8E0";   // azul
            case "SOLDIER"  -> "#E05252";   // rojo
            default         -> "#8B9BAE";   // gris si no tiene rol
        };

        String roleText   = role.isEmpty()   ? "Sin rol"   : role;
        String statusText = status.isEmpty() ? "Inactivo"  : status;

        String inlineUI =
                "Group {\n" +
                        "    LayoutMode: Middle;\n" +
                        "    Padding: (Horizontal: 20);\n" +
                        "    Anchor: (Height: 52, Bottom: 3);\n" +
                        "    Background: (Color: #FFFFFF(0.04));\n" +
                        "\n" +
                        "    Group {\n" +
                        "        LayoutMode: Left;\n" +
                        "\n" +
                        "        Label {\n" +
                        "            Text: \"" + escapeUiString(name) + "\";\n" +
                        "            Anchor: (Width: 220);\n" +
                        "            Style: (TextColor: #FFFFFF, RenderBold: true, FontSize: 15);\n" +
                        "        }\n" +
                        "\n" +
                        "        Label {\n" +
                        "            FlexWeight: 2;\n" +
                        "            Text: \"" + statusText + "\";\n" +
                        "            Style: (TextColor: " + statusColor + ", FontSize: 13, Alignment: Center);\n" +
                        "        }\n" +
                        "\n" +
                        "        Label {\n" +
                        "            FlexWeight: 2;\n" +
                        "            Text: \"" + roleText + "\";\n" +
                        "            Style: (TextColor: " + roleColor + ", RenderBold: true, FontSize: 13, Alignment: Center);\n" +
                        "        }\n" +
                        "    }\n" +
                        "}";

        uiBuilder.appendInline("#Users", inlineUI);
    }
    private static String escapeUiString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}