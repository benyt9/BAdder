package b.bplugins.badder.listeners;

import b.bplugins.badder.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final Main plugin;

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Holt die URL jetzt zentral über die Main-Klasse
        String url = plugin.getResourcePackUrl();

        if (url == null || url.isEmpty()) {
            return;
        }

        // Folia-kompatibel: Entity Scheduler statt Bukkit.getScheduler()
        player.getScheduler().runDelayed(plugin, (task) -> {
            if (!player.isOnline()) return;

            try {
                UUID packId = UUID.nameUUIDFromBytes("badder_pack".getBytes());

                player.setResourcePack(
                        packId,
                        url,
                        (byte[]) null,
                        Component.text("BAdder Resource Pack wird geladen..."),
                        true
                );
                plugin.getLogger().info("Resource Pack wurde an " + player.getName() + " gesendet (" + url + ")");
            } catch (Exception e) {
                plugin.getLogger().warning("Konnte Resource Pack nicht an " + player.getName() + " senden: " + e.getMessage());
            }
        }, null, 20L); // 20 Ticks = 1 Sekunde Verzögerung
    }
}