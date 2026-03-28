package Tenzinn.Systems;

import Tenzinn.JSON.Roles;

import java.util.ArrayList;

public class RoleController {

    private static ArrayList<String> activePlayers = new ArrayList<String>();
    private static ArrayList<String> undefinedPlayers = new ArrayList<String>();

    public enum Role { MEDIC, SOLDIER }

    public static ArrayList<String> getListActivePlayers() { return activePlayers; }
    public static void setActivePlayer(String name) {
        String prevUser = activePlayers.stream().filter(user -> user.equalsIgnoreCase(name)).findFirst().orElse("");

        // Añadir al player a un listado de usuarios SIN ROL ASIGNADO
        if(Roles.getValue(name).isEmpty()) { undefinedPlayers.add(name); }

        // Añadir al player a un listado de usuario CON ROL ASIGNADO
        if (prevUser.isEmpty()) { activePlayers.add(name); }
    }
    public static void setInactivePlayer(String name) {
        String equals = activePlayers.stream().filter(item -> item.equalsIgnoreCase(name)).findFirst().orElse("");
        if(!equals.isEmpty()) { activePlayers.remove(name); }
    }
    public static String getOnePlayer(String player) { return Roles.getValue(player); }
    public static String getStatusPlayer(String player) {
        String statue;

        statue = activePlayers.stream().filter(name -> name.equalsIgnoreCase(player)).findFirst().orElse("");

        if(!statue.isEmpty()) return "ACTIVE";

        statue = undefinedPlayers.stream().filter(name -> name.equalsIgnoreCase(player)).findFirst().orElse("");
        if(!statue.isEmpty()) return "UNDEFINED";

        return statue;
    }
}