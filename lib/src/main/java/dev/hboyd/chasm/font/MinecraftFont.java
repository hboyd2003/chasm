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
import dev.hboyd.chasm.font.provider.GlyphDefinitionProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a font in Minecraft.
 * Made up of {@link GlyphDefinitionProvider}s.
 *
 * @see <a href="https://minecraft.wiki/w/Font">Font - Minecraft Wiki</a>
 */
public class MinecraftFont implements Keyed, GlyphDefinitionProvider {
    /**
     * A simplified version of the <code>minecraft:default</code> font.
     *
     * @see BuiltinGlyphDefinitionProvider
     */
    public static final MinecraftFont BUILTIN = new MinecraftFont(Key.key(Key.MINECRAFT_NAMESPACE, "default"),
            List.of(BuiltinGlyphDefinitionProvider.INSTANCE));

    private final Key fontKey;
    private final List<GlyphDefinitionProvider> references;

    /**
     * Constructs a new MinecraftFont using the provided font key and references.
     *
     * @param fontKey the key for the font
     * @param references the list of glyph providers that make up the font
     */
    @Contract(pure = true)
    public MinecraftFont(final Key fontKey, final List<GlyphDefinitionProvider> references) {
        this.fontKey = fontKey;
        this.references = List.copyOf(references);
    }

    @Override
    public Key key() {
        return this.fontKey;
    }

    @Override
    public Optional<Float> tryGetWidthOf(final int codepoint, final Style style) {
        Optional<Float> width = Optional.empty();
        for (final GlyphDefinitionProvider reference : this.references) {
            width = reference.tryGetWidthOf(codepoint, style);
            if (width.isPresent()) break;
        }

        return width;
    }

    @Override
    public @Unmodifiable Map<Integer, Float> getSpaceCodepoints() {
        return this.references.reversed().stream()
                .flatMap(reference -> reference.getSpaceCodepoints().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * All {@link GlyphDefinitionProvider}s that make up this font.
     *
     * @return the list of providers
     */
    public @Unmodifiable List<GlyphDefinitionProvider> references() {
        return this.references;
    }
}
