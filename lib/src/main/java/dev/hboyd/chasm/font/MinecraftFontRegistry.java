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

import dev.hboyd.chasm.font.provider.BuiltinGlyphDefinitionProvider;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry of {@link MinecraftFont}.
 */
public class MinecraftFontRegistry {
    /**
     * Global static registry, includes a simplified version of the <code>minecraft:default</code> font.
     *
     * @see BuiltinGlyphDefinitionProvider
     */
    public static final MinecraftFontRegistry GLOBAL = new MinecraftFontRegistry();

    static {
        GLOBAL.addFont(MinecraftFont.BUILTIN);
    }

    private final Map<Key, MinecraftFont> fontMap;

    /**
     * Creates a new empty Minecraft font registry.
     */
    @Contract(pure = true)
    public MinecraftFontRegistry() {
        this.fontMap = new HashMap<>();
    }

    /**
     * Adds the given font to the registry.
     * If a font exists with the same key it will be replaced.
     *
     * @param font the font to add
     */
    public void addFont(final MinecraftFont font) {
        this.fontMap.put(font.key(), font);
    }

    /**
     * Gets the font instance with the given key or null if not registered.
     *
     * @param key the key of the font to get
     * @return the font or null
     */
    public @Nullable MinecraftFont getFont(final Key key) {
        return this.fontMap.get(key);
    }

    /**
     * Removes the font with the given key from the registry or
     * does nothing if the font key is not registered.
     *
     * @param key the key of the font to remove
     * @return the removed font or null
     */
    public @Nullable MinecraftFont removeFont(final Key key) {
        return this.fontMap.remove(key);
    }

    /**
     * Checks if the registry has the font with the given key.
     *
     * @param key the key to check for
     * @return if the registry contains the font
     */
    public boolean hasFont(final Key key) {
        return this.fontMap.containsKey(key);
    }

    /**
     * Gets all fonts registered with this registry.
     *
     * @return registered fonts
     */
    public @Unmodifiable List<MinecraftFont> fonts() {
        return List.copyOf(this.fontMap.values());
    }
}
