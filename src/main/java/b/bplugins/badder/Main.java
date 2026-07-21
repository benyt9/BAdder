package b.bplugins.badder;

import b.bplugins.badder.commands.BadderCommand;
import b.bplugins.badder.hosting.SelfHostServer;
import b.bplugins.badder.utils.MessageUtils;
import b.bplugins.badder.listeners.PlayerJoinListener;
import b.bplugins.badder.pack.PackManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {
    private static Main instance;
    private SelfHostServer selfHostServer;
    private PackManager packManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Config & Sprachdateien laden
        saveDefaultConfig();
        loadLanguageFile();

        this.packManager = new PackManager(getDataFolder());

        // 2. Contents-Ordner Struktur sicherstellen & scannen
        setupContentsFolder();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // 3. Internen Self-Hosting Webserver starten
        selfHostServer = new SelfHostServer(this);
        selfHostServer.start();

        // 4. Native Paper Commands registrieren (/badder etc.)
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(BadderCommand.createCommand(this), "BAdder main command", java.util.List.of("badder"));
        });

        getLogger().info("BAdder wurde erfolgreich aktiviert!");
    }

    @Override
    public void onDisable() {
        if (selfHostServer != null) {
            selfHostServer.stop();
        }
        getLogger().info("BAdder wurde deaktiviert.");
    }

    private void loadLanguageFile() {
        File msgFolder = new File(getDataFolder(), "messages");
        if (!msgFolder.exists()) msgFolder.mkdirs();

        String lang = getConfig().getString("language", "en");
        MessageUtils.load(lang);
    }

    private void setupContentsFolder() {
        File contentsFolder = new File(getDataFolder(), "contents");
        if (!contentsFolder.exists()) {
            contentsFolder.mkdirs();
            getLogger().info("Contents-Ordner wurde erstellt.");
        }

        scanContents(contentsFolder);
    }

    public void scanContents(File contentsFolder) {
        File[] packs = contentsFolder.listFiles(File::isDirectory);
        if (packs == null) return;

        for (File pack : packs) {
            getLogger().info("Gefundenes Pack / Namespace: " + pack.getName());

            // Struktur validieren: configs/ und resourcepack/
            File configsDir = new File(pack, "configs");
            File resourcePackDir = new File(pack, "resourcepack");

            if (!configsDir.exists()) {
                getLogger().warning("Pack '" + pack.getName() + "' besitzt keinen 'configs/' Ordner!");
            }
            if (!resourcePackDir.exists()) {
                getLogger().warning("Pack '" + pack.getName() + "' besitzt keinen 'resourcepack/' Ordner!");
            }
        }
    }

    public PackManager getPackManager() {
        return packManager;
    }

    public void reloadConfigs() {
        // Folia-kompatibler Async-Scheduler
        Bukkit.getAsyncScheduler().runNow(this, task -> {
            this.reloadConfig();
            String lang = getConfig().getString("language", "en");
            MessageUtils.load(lang);

            // Contents beim Reload erneut scannen
            File contentsFolder = new File(getDataFolder(), "contents");
            scanContents(contentsFolder);

            // Webserver bei Config-Änderungen ebenfalls neu starten
            if (selfHostServer != null) {
                selfHostServer.stop();
            }
            selfHostServer = new SelfHostServer(this);
            selfHostServer.start();

            getLogger().info("BAdder Konfigurationen und Packs erfolgreich neu geladen!");
        });
    }

    public String getResourcePackUrl() {
        String mode = getConfig().getString("hosting.mode", "self");
        if (mode.equalsIgnoreCase("self")) {
            String ip = getConfig().getString("hosting.self-host.public-address", "127.0.0.1");
            int port = getConfig().getInt("hosting.self-host.port", 8163);
            return "http://" + ip + ":" + port + "/pack.zip";
        } else {
            return getConfig().getString("hosting.external-host.url", "");
        }
    }

    public static Main getInstance() {
        return instance;
    }
}