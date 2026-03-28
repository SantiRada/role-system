package Tenzinn.JSON;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import com.google.gson.*;

public class Roles {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static String getConfigPath() {
        try {
            File jar = new File(Roles.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            File configFile = new File(jar.getParentFile(), "RoleSystem/players.json");
            return configFile.getAbsolutePath();

        } catch (Exception e) {
            System.err.println("Error obteniendo path: " + e.getMessage());
            return null;
        }
    }

    // 📖 Leer JSON
    public static JsonObject load() {
        try {
            Path path = Paths.get(getConfigPath());
            String content = new String(Files.readAllBytes(path));
            return gson.fromJson(content, JsonObject.class);

        } catch (Exception e) {
            System.err.println("Error leyendo JSON: " + e.getMessage());
            return null;
        }
    }

    // ✏️ Editar valor directo
    public static void updateValue(String key, String newValue) {
        try {
            Path path = Paths.get(getConfigPath());
            String content = new String(Files.readAllBytes(path));

            JsonObject root = gson.fromJson(content, JsonObject.class);
            JsonObject revenues = root.getAsJsonObject("players");

            if (revenues == null) { System.err.println("No existe 'players'"); return; }
            revenues.addProperty(key, newValue);

            Files.write(path, gson.toJson(root).getBytes());

        } catch (Exception e) {
            System.err.println("Error actualizando JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getValue(String key) {
        String value = "";

        try {
            Path path = Paths.get(Objects.requireNonNull(getConfigPath()));
            String content = new String(Files.readAllBytes(path));

            JsonObject root = gson.fromJson(content, JsonObject.class);
            JsonObject players = root.getAsJsonObject("players");

            if (players == null) { System.err.println("No existe 'players'"); return ""; }

            for (var entry : players.entrySet()) {
                if(entry.getKey().equals(key)) {
                    value = entry.getValue().getAsString();
                    break;
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }

        return value;
    }
}