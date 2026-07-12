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

package dev.hboyd.chasm.font.provider;

import dev.hboyd.chasm.font.StyledGlyph;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Provides definitions for Glyphs.
 */
public interface GlyphDefinitionProvider {

    /**
     * Attempts to get the width of the codepoint with the given style when rendered in Minecraft.
     * Returns empty if the provider does not have a definition for this codepoint.
     * For characters with a shadow, the width will include it.
     *
     * @param codepoint the codepoint to get the width of
     * @param style     the style to adapt the width to
     * @return the rendered width of the codepoint or empty
     */
    Optional<Float> tryGetWidthOf(int codepoint, Style style);

    /**
     * Gets the width of the codepoint with the given style when rendered in Minecraft.
     * For characters with a shadow, the width will include it.
     *
     * @param codepoint the codepoint to get the width of
     * @param style     the style to adapt the width to
     * @return the rendered width of the codepoint or default
     * @throws NoSuchElementException when the provider does not have the codepoint
     */
    default float getWidthOf(final int codepoint, final Style style) throws NoSuchElementException {
        return this.tryGetWidthOf(codepoint, style)
                .orElseThrow(() -> new NoSuchElementException("No such codepoint " + codepoint));
    }

    /**
     * Attempts to get the width of the StyledGlyph when rendered in Minecraft.
     * Returns empty if the provider does not have a definition for this codepoint
     * For characters with a shadow, the width will include it.
     *
     * @param styledGlyph the StyledGlyph to get the width of
     * @return the rendered width of the StyledGlyph or empty
     */
    default Optional<Float> tryGetWidthOf(final StyledGlyph styledGlyph) {
        return this.tryGetWidthOf(styledGlyph.codepoint(), styledGlyph.style());
    }

    /**
     * Gets the width of the codepoint with the given style when rendered in Minecraft.
     * For characters with a shadow, the width will include it.
     *
     * @param styledGlyph the StyledGlyph to get the width of
     * @return the rendered width of the StyledGlyph
     * @throws NoSuchElementException when the provider does not have the glyph
     */
    default float getWidthOf(final StyledGlyph styledGlyph) {
        return this.getWidthOf(styledGlyph.codepoint(), styledGlyph.style());
    }

    /**
     * Gets a map of space codepoints and their widths.
     * Spaces must be defined by a space provider and are unique in that they can be any width.
     * Additionally, spaces have no shadow allowing them to have a width of one, zero, or even negative.
     *
     * @return all spaces provided by this provider
     */
    @Unmodifiable
    Map<Integer, Float> getSpaceCodepoints();
}
