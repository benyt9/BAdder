package b.bplugins.badder.pack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackManager {

    private final File dataFolder;

    public PackManager(File dataFolder) {
        this.dataFolder = dataFolder;
        initializeDefaultPack();
    }

    public void initializeDefaultPack() {
        File basePackDir = new File(dataFolder, "contents/_bassets/resourcepack");
        File mcmetaFile = new File(basePackDir, "pack.mcmeta");

        if (!mcmetaFile.exists()) {
            basePackDir.mkdirs();

            String defaultMcmeta = "{\n" +
                    "  \"pack\": {\n" +
                    "    \"description\": \"BAdder Default Resource Pack\",\n" +
                    "    \"min_format\": [46, 0],\n" +
                    "    \"max_format\": [90, 0]\n" +
                    "  }\n" +
                    "}";

            try (FileWriter writer = new FileWriter(mcmetaFile)) {
                writer.write(defaultMcmeta);
                System.out.println("[BAdder] Standard pack.mcmeta wurde erfolgreich erstellt.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Baut das Resource Pack aus allen Ordnern in contents/ zusammen und speichert es als ZIP.
     */
    public boolean buildResourcePack(File targetZipFile) {
        File contentsDir = new File(dataFolder, "contents");
        if (!contentsDir.exists()) {
            contentsDir.mkdirs();
            return false;
        }

        // Sicherstellen, dass der Output-Ordner existiert
        File parentDir = targetZipFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZipFile))) {
            // Alle Ordner in contents/ durchgehen (z.B. _bassets und eigene Module)
            File[] packs = contentsDir.listFiles(File::isDirectory);
            if (packs != null) {
                for (File packDir : packs) {
                    File resourcePackSubDir = new File(packDir, "resourcepack");

                    // Wenn ein Modul einen 'resourcepack'-Ordner hat, dessen Inhalt zippen
                    File sourceToZip = resourcePackSubDir.exists() ? resourcePackSubDir : packDir;

                    // Bei _bassets wollen wir direkt den Inhalt ab resourcepack oder direkt reinwerfen,
                    // je nachdem wie die Minecraft-Struktur sein soll (assets/ im Root der ZIP).
                    addFolderToZip(sourceToZip, sourceToZip.getName().equals("resourcepack") ? sourceToZip : packDir, zos);
                }
            }
            System.out.println("[BAdder] Resource Pack erfolgreich erstellt unter: " + targetZipFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addFolderToZip(File folder, File rootFolder, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // System-Ordner wie 'configs' überspringen, da sie nicht ins Resource Pack gehören
                if (file.getName().equals("configs")) continue;
                addFolderToZip(file, rootFolder, zos);
            } else {
                // Relativen Pfad für den ZIP-Eintrag berechnen
                String filePath = file.getAbsolutePath();
                String rootPath = rootFolder.getAbsolutePath();

                // Relativen Pfad ermitteln, damit assets/minecraft/... direkt an root der ZIP liegt
                String entryName = filePath.substring(rootPath.length() + 1).replace("\\", "/");

                // Falls wir uns im _bassets/resourcepack Ordner befinden, soll es direkt ins Root der Zip
                if (rootFolder.getName().equals("resourcepack")) {
                    entryName = filePath.substring(rootFolder.getAbsolutePath().length() + 1).replace("\\", "/");
                }

                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                }
                zos.closeEntry();
            }
        }
    }
}