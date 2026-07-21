package b.bplugins.badder.api;

import b.bplugins.badder.Main;
import b.bplugins.badder.pack.PackManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BAdderAPI {

    private static Main getPlugin() {
        return Main.getInstance();
    }

    public static PackManager getPackManager() {
        return getPlugin().getPackManager();
    }

    public static void reloadPlugin() {
        getPlugin().reloadConfigs();
    }

    public static String getResourcePackUrl() {
        return getPlugin().getResourcePackUrl();
    }

    public static void sendResourcePack(Player player) {
        String url = getResourcePackUrl();
        if (url == null || url.isEmpty()) return;

        UUID packId = UUID.nameUUIDFromBytes("badder_pack".getBytes());
        player.setResourcePack(
                packId,
                url,
                (byte[]) null,
                Component.text("BAdder Resource Pack wird geladen..."),
                true
        );
    }
}