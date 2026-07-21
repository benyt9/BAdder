package b.bplugins.badder.hosting;

import b.bplugins.badder.Main;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.net.InetSocketAddress;

public class SelfHostServer {

    private final Main plugin;
    private HttpServer server;

    public SelfHostServer(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        String mode = plugin.getConfig().getString("hosting.mode", "self");
        if (!mode.equalsIgnoreCase("self")) {
            return;
        }

        String ip = plugin.getConfig().getString("hosting.self-host.ip", "0.0.0.0");
        int port = plugin.getConfig().getInt("hosting.self-host.port", 8163);

        try {
            server = HttpServer.create(new InetSocketAddress(ip, port), 0);
            server.createContext("/pack.zip", new PackHandler(plugin));
            server.setExecutor(null); // Default executor
            server.start();
            plugin.getLogger().info("Internal self-hosting webserver started on http://" + ip + ":" + port + "/pack.zip");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start internal self-hosting webserver: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Internal self-hosting webserver stopped.");
        }
    }

    private static class PackHandler implements HttpHandler {
        private final Main plugin;

        public PackHandler(Main plugin) {
            this.plugin = plugin;
        }

        @Override
        public void handle(HttpExchange exchange) throws java.io.IOException {
            File zipFile = new File(plugin.getDataFolder(), "output/badder-resourcepack.zip");
            if (!zipFile.exists()) {
                String response = "Resource pack not generated yet!";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, zipFile.length());
            OutputStream os = exchange.getResponseBody();
            Files.copy(zipFile.toPath(), os);
            os.close();
        }
    }
}