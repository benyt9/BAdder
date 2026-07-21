package b.bplugins.badder.utils;

import b.bplugins.badder.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class MessageUtils {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static volatile YamlConfiguration langConfig;

    private static String resolveColors(String message) {
        // Hier suchen wir nach <colors.message:key> und ersetzen es durch den Wert aus der YAML
        if (langConfig != null && langConfig.contains("mappings.colors.message")) {
            for (String key : langConfig.getConfigurationSection("mappings.colors.message").getKeys(false)) {
                String color = langConfig.getString("mappings.colors.message." + key);
                message = message.replace("<colors.message:" + key + ">", color);
            }
        }
        return message;
    }

    // Lädt oder lädt die Sprachdatei neu
    public static void load(String languageCode) {
        File folder = new File(Main.getInstance().getDataFolder(), "messages");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, languageCode + ".yml");

        // Wenn die Datei noch nicht existiert, aus der JAR kopieren
        if (!file.exists()) {
            Main.getInstance().saveResource("messages/" + languageCode + ".yml", false);
        }

        if (!file.exists()) {
            Main.getInstance().getLogger().warning("Sprachdatei " + languageCode + ".yml konnte nicht gefunden/erstellt werden!");
            return;
        }
        langConfig = YamlConfiguration.loadConfiguration(file);
    }

    public static Component getMessage(String key) {
        if (langConfig == null) {
            return MM.deserialize("<red>Language configuration not loaded!");
        }
        String prefix = langConfig.getString("mappings.prefix", "");
        String raw = langConfig.getString("messages." + key, "<red>Missing: " + key);

        // Farben auflösen, dann Prefix dazu, dann MiniMessage
        return MM.deserialize(prefix + resolveColors(raw));
    }

    public static Component getNoPermission(String permission) {
        if (langConfig == null) {
            return MM.deserialize("<red>Language configuration not loaded!");
        }
        String prefix = langConfig.getString("mappings.prefix", "");
        String raw = langConfig.getString("messages.permission.check-failed", "<red>No permission!");

        // Hier jetzt auch resolveColors nutzen!
        raw = raw.replace("<permission>", permission);
        return MM.deserialize(prefix + resolveColors(raw));
    }
}