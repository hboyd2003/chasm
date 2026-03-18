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

package dev.hboyd.chasm.font;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.hboyd.chasm.font.provider.*;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Contract;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Generates and registers {@link MinecraftFont}s from a resource pack
 */
public class MinecraftFontGenerator {
    private final Key fontKey;
    private @MonotonicNonNull Path resourcePackPath;
    private MinecraftFontRegistry fontRegistry;

    private MinecraftFontGenerator(Key fontKey) {
        this.fontKey = fontKey;
        this.fontRegistry = MinecraftFontRegistry.GLOBAL;
    }

    /**
     * Creates a new MinecraftFontGenerator with the given font key.
     *
     * @param fontKey the key of the font
     * @return a new MinecraftFontGenerator
     */
    @Contract("_ -> new")
    public static MinecraftFontGenerator create(Key fontKey) {
        Objects.requireNonNull(fontKey, "font key");
        return new MinecraftFontGenerator(fontKey);
    }

    /**
     * Sets the MinecraftFontRegistry to register the generated font to and to get referenced fonts from.
     *
     * @param fontRegistry the font registry to set
     * @return this
     */
    public MinecraftFontGenerator fontRegistry(MinecraftFontRegistry fontRegistry) {
        Objects.requireNonNull(fontRegistry, "font registry");
        this.fontRegistry = fontRegistry;
        return this;
    }

    /**
     * Sets the resource pack to generate the font with.
     *
     * @param path the path to the resource pack
     * @return this
     */
    public MinecraftFontGenerator resourcePack(Path path) {
        Objects.requireNonNull(path, "resource pack path");
        if (Files.notExists(path)) throw new IllegalArgumentException("Resource pack path does not exist: " + path);

        this.resourcePackPath = path;

        return this;
    }

    /**
     * Sets the resource pack to generate the font with.
     *
     * @param uri the uri of the resource pack
     * @return this
     */
    public MinecraftFontGenerator resourcePack(URI uri) {
        Objects.requireNonNull(uri, "resource pack uri");
        this.resourcePackPath =  Path.of(uri);
        return this;
    }

    /**
     * Generates the font or gets the font from the set MinecraftFontRegistry if it already exists.
     * If the font does not already exist it will be registered to the set font registry.
     *
     * @return the generated font or existing font from the font registry
     * @throws IOException when the font fails to generate
     */
    public MinecraftFont generateOrGet() throws IOException {
        return generateOrGet(fontKey, resourcePackPath, fontRegistry);
    }

    /**
     * Generates the font.
     * The generated font will be registered with the set font registry.
     *
     * @return the generated font
     * @throws IOException when the font fails to generate
     */
    public MinecraftFont generate() throws IOException {
        MinecraftFont font = generate(fontKey, resourcePackPath, fontRegistry);
        fontRegistry.addFont(font);
        return font;
    }

    private static MinecraftFont generateOrGet(Key fontKey, Path resourcePackPath, MinecraftFontRegistry fontRegistry) throws IOException {
        Objects.requireNonNull(resourcePackPath, "resource pack URI or path");

        MinecraftFont font = fontRegistry.getFont(fontKey);
        if (font == null) {
            font = generate(fontKey, resourcePackPath, fontRegistry);
            fontRegistry.addFont(font);
        }

        return font;
    }

    @SuppressWarnings("PatternValidation")
    @Contract("_, _, _ -> new")
    private static MinecraftFont generate(Key fontKey, Path resourcePackPath, MinecraftFontRegistry fontRegistry) throws IOException {
        if (Files.notExists(resourcePackPath))
            throw new IllegalArgumentException("Resource pack path does not exist");

        List<GlyphDefinitionProvider> glyphDefinitionProviders = new ArrayList<>();
        Path assetsPath = resourcePackPath.resolve("assets");

        Path fontDefinitionPath = assetsPath.resolve(fontKey.namespace(), "font", fontKey.value() + ".json");
        try (BufferedReader br = Files.newBufferedReader(fontDefinitionPath)) {
            JsonArray providers = new Gson()
                    .fromJson(br, JsonObject.class)
                    .get("providers")
                    .getAsJsonArray();

            for (JsonElement providerElement : providers) {
                JsonObject provider = providerElement.getAsJsonObject();

                switch (provider.get("type").getAsString()) {
                    case "reference" -> {
                        Key providerFontKey = Key.key(provider.get("id").getAsString());
                        MinecraftFont providerFont = fontRegistry.getFont(providerFontKey);
                        if (providerFontKey == fontKey)
                            throw new IllegalArgumentException("Referenced font must not be self");

                        if (providerFont == null) {
                            providerFont = generateOrGet(providerFontKey, resourcePackPath, fontRegistry);
                            fontRegistry.addFont(providerFont);
                        }

                        glyphDefinitionProviders.add(providerFont);
                    }
                    case "space" -> {
                        Map<Integer, Float> spaceCharMap = new HashMap<>();
                        for (Map.Entry<String, JsonElement> spaceEntry : provider.get("advances").getAsJsonObject().entrySet()) {
                            spaceCharMap.put((int) spaceEntry.getKey().charAt(0), spaceEntry.getValue().getAsFloat());
                        }

                        glyphDefinitionProviders.add(new SpaceGlyphDefinitionProvider(spaceCharMap));
                    }
                    case "bitmap" -> {
                        Key textureKey = Key.key(provider.get("file").getAsString());
                        int height = Optional.ofNullable(provider.get("height"))
                                .map(JsonElement::getAsInt)
                                .orElse(BitmapGlyphDefinitionProvider.DEFAULT_HEIGHT);

                        Path texturePath = assetsPath.resolve(textureKey.namespace(), "textures", textureKey.value());
                        if (Files.notExists(texturePath))
                            throw new IllegalStateException("Specified bitmap texture file " + textureKey + " does not exist");

                        JsonArray charstrings = provider.get("chars").getAsJsonArray();
                        int[][] codepoints = new int[charstrings.size()][];
                        int row = 0;
                        for (JsonElement element : charstrings) {
                            String charRow = element.getAsString();
                            codepoints[row] = charRow.codePoints().toArray();

                            row++;
                        }

                        glyphDefinitionProviders.add(new BitmapGlyphDefinitionProvider(codepoints, texturePath, height));
                    }
                    case "unihex" -> {
                        Key hexZipKey = Key.key(provider.get("hex_file").getAsString());
                        Path hexZipPath = assetsPath.resolve(hexZipKey.namespace(), "font", hexZipKey.value());
                        if (Files.notExists(hexZipPath))
                            throw new IllegalStateException("Specified unihex file " + hexZipKey + " not does exist");

                        try (ZipFile zipFile = new ZipFile(hexZipPath.toFile())) {
                            zipFile.stream()
                                    .parallel()
                                    .filter(zipEntry -> zipEntry.getName().endsWith(".hex"))
                                    .forEach(zipEntry -> {
                                        try {
                                            glyphDefinitionProviders.add(new UnihexDefinitionProvider(hexZipPath.resolve(zipEntry.getName())));
                                        } catch (IOException e) {
                                            throw new RuntimeException("Failed to load Unihex font for font " + fontKey, e);
                                        }
                                    });
                        }
                    }
                    case "ttf" -> {
                        Key ttfFileKey = Key.key(provider.get("file").getAsString());
                        int size = provider.get("size").getAsInt();

                        String charExclusions;
                        JsonElement skipElement = provider.get("skip");
                        if (skipElement.isJsonArray()) {
                            StringBuilder builder = new StringBuilder();
                            JsonArray skipArray = skipElement.getAsJsonArray();

                            skipArray.forEach(element -> builder.append(element.getAsString()));
                            charExclusions = builder.toString();
                        } else charExclusions = skipElement.getAsString();

                        Path ttfPath = assetsPath.resolve(ttfFileKey.namespace(), "font", ttfFileKey.value());

                        try {
                            glyphDefinitionProviders.add(new TrueTypeGlyphDefinitionProvider(ttfPath, size, charExclusions));
                        } catch (FontFormatException e) {
                            throw new RuntimeException("Failed to load TrueType font for font " + fontKey, e);
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected font provider type: " + provider.get("type").getAsString());
                }
            }
        }

        return new MinecraftFont(fontKey, glyphDefinitionProviders);
    }
}