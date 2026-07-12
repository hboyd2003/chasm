/*
 * chasm
 * Copyright (c) 2026 Harrison Boyd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.hboyd.chasm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.hboyd.chasm.font.MinecraftFontGenerator;
import dev.hboyd.chasm.font.MinecraftFontRegistry;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Chasm is a Minecraft Paper plugin, which is a companion to the Chasm plugin. It provides the default Minecraft font's
 * to the chasm library.
 */
public final class Chasm extends JavaPlugin implements HoverEventSource<Component> {
    public static final String ID = "Chasm";
    public static final ComponentLogger LOGGER = ComponentLogger.logger(ID);
    public static final Chasm INSTANCE = new Chasm();

    private Chasm() {}

    @Override
    public void onEnable() {
        LOGGER.info("Chasm {} - Copyright (C) 2026 Harrison Boyd - Licensed under LGPLv3", this.getPluginMeta().getVersion());
        LOGGER.info("Building default Minecraft fonts");

        try {
            Files.createDirectories(this.getDataPath());
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create data directory", e);
        }

        final Path resourcePackPath;
        try {
            resourcePackPath = this.getOrDownloadClientJar(this.getDataPath());
        } catch (final IOException e) {
            throw new RuntimeException("Failed to download client", e);
        }

        try {
            this.loadAllFonts(resourcePackPath);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load default Minecraft fonts", e);
        }
    }

    private Path getOrDownloadClientJar(final Path downloadDir) throws IOException {
        final Path clientPath = downloadDir.resolve("minecraft-client-" + ServerBuildInfo.buildInfo().minecraftVersionId() + ".jar");
        if (Files.exists(clientPath)) {
            LOGGER.info("Using cached client jar for {}", ServerBuildInfo.buildInfo().minecraftVersionId());
            return clientPath;
        }

        final Instant startInstant = Instant.now();
        // Get url to this version's manifest
        URL versionManifest = null;
        try (final InputStream is = URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").toURL().openStream();
                final InputStreamReader isr = new InputStreamReader(is);
                final BufferedReader br = new BufferedReader(isr)) {
            final JsonArray versionArray = new Gson().fromJson(br, JsonObject.class).getAsJsonArray("versions");
            for (final JsonElement versionElement : versionArray) {
                final JsonObject version = versionElement.getAsJsonObject();
                if (Objects.equals(version.get("id").getAsString(), ServerBuildInfo.buildInfo().minecraftVersionId()))
                    versionManifest = URI.create(version.get("url").getAsString()).toURL();
            }
        } catch (final IOException e) {
            throw new IOException("Failed to get version manifest", e);
        }

        if (versionManifest == null) throw new IllegalStateException();

        final URL clientURL;
        try (final InputStream is = versionManifest.openStream();
                final InputStreamReader isr = new InputStreamReader(is);
                final BufferedReader br = new BufferedReader(isr)) {
            clientURL = URI.create(new Gson().fromJson(br, JsonObject.class).getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString()).toURL();
        } catch (final IOException e) {
            throw new IOException("Failed to get client download URL", e);
        }

        try (final InputStream is = clientURL.openStream();
                final OutputStream os = Files.newOutputStream(clientPath)) {
            is.transferTo(os);
        } catch (final IOException e) {
            throw new IOException("Failed to download client", e);
        }

        LOGGER.info("Downloaded client jar for {} in {}", ServerBuildInfo.buildInfo().minecraftVersionId(), Duration.between(startInstant, Instant.now()));

        return clientPath;
    }

    @SuppressWarnings("PatternValidation")
    private void loadAllFonts(final Path resourcePackZIPPath) throws IOException {
        final Instant startInstant = Instant.now();
        final URI resourcePackURI;
        resourcePackURI = URI.create("jar:" + resourcePackZIPPath.toUri() + "!/");

        try {
            FileSystems.getFileSystem(resourcePackURI).close(); // We should be the only one opening this file; this is a fail-safe
        } catch (final FileSystemNotFoundException _) {}

        try (final FileSystem resourcePackFS = FileSystems.newFileSystem(resourcePackURI, Map.of())) {
            final Path assetsDir = resourcePackFS.getPath("assets");
            final PathMatcher matcher = resourcePackFS.getPathMatcher("glob:assets/*/font/*.json");
            final List<Path> fontPaths = Files.walk(assetsDir)
                    .filter(matcher::matches)
                    .toList();

            for (final Path fontPath : fontPaths) {
                String keyFontPath = resourcePackFS.getPath("assets", "minecraft", "font")
                        .relativize(fontPath)
                        .toAbsolutePath()
                        .normalize()
                        .toString()
                        .replace(".json", "")
                        .replace(resourcePackFS.getSeparator().charAt(0), '/');
                if (keyFontPath.startsWith("/"))
                    keyFontPath = keyFontPath.substring(1);

                MinecraftFontGenerator.create(Key.key(Key.MINECRAFT_NAMESPACE, keyFontPath))
                        .fontRegistry(MinecraftFontRegistry.GLOBAL)
                        .resourcePack(resourcePackURI)
                        .generate();
            }
        }
        LOGGER.info("Finished loading fonts in {}", Duration.between(startInstant, Instant.now()));
    }

    @Override
    public ComponentLogger getComponentLogger() {
        return LOGGER;
    }

    @Override
    public Logger getSLF4JLogger() {
        return LOGGER;
    }

    @Override
    public HoverEvent<Component> asHoverEvent(final UnaryOperator<Component> op) {
        return HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, op.apply(Component.text("v" + this.getPluginMeta().getVersion())));
    }
}
