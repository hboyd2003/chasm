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

package dev.hboyd.chasm.configurate;

import dev.hboyd.chasm.font.StyledGlyph;
import net.kyori.adventure.text.format.Style;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * A configurate serializer for {@link StyledGlyph}s. Only the first codepoint is used.
 * <p>
 * Requires a {@link net.kyori.adventure.text.format.Style} serializer.
 */
public class StyledGlyphSerializer implements TypeSerializer<StyledGlyph> {
    public static final StyledGlyphSerializer INSTANCE = new StyledGlyphSerializer();

    private static final String CHARACTER_NODE_PATH = "character";
    private static final String STYLE_NODE_PATH = "style";

    private StyledGlyphSerializer() {}

    @Override
    public @Nullable StyledGlyph deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (!node.hasChild(CHARACTER_NODE_PATH))
            throw new SerializationException("StyledGlyph is missing required character node!");

        final Optional<Integer> codepoint = Optional.ofNullable(node.node(CHARACTER_NODE_PATH).get(String.class))
                .filter(string -> !string.isEmpty())
                .map(string -> string.codePointAt(0));

        if (codepoint.isEmpty()) throw new SerializationException(node, type, "Missing or empty character");

        Style style = node.node(STYLE_NODE_PATH).get(Style.class);
        if (style == null) style = Style.empty();

        return new StyledGlyph(codepoint.get(), style);
    }

    @Override
    public void serialize(Type type, @Nullable StyledGlyph styledGlyph, ConfigurationNode node) throws SerializationException {
        if (styledGlyph == null) return;

        node.node(CHARACTER_NODE_PATH).set(String.class, Character.toString(styledGlyph.codepoint()));
        node.node(STYLE_NODE_PATH).set(Style.class, styledGlyph.style());
    }
}
