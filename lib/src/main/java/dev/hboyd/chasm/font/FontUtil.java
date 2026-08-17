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

import net.kyori.adventure.text.format.Style;

import java.util.Map;
import java.util.Optional;

/**
 * Utilities for working with Minecraft fonts.
 */
public final class FontUtil {

    private FontUtil() {

    }

    /**
     * Find the smallest positive width space in the given registry. The returned styled glyph's style has the font that
     * the glyph exists in.
     *
     * @param fontRegistry a registry
     * @return the space
     */
    public static Optional<StyledGlyph> findSmallestPositiveSpace(final MinecraftFontRegistry fontRegistry) {
        return fontRegistry.fonts().stream()
                .map(font -> font.getSpaceCodepoints().entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .min(Map.Entry.comparingByValue())
                        .map(entry -> Map.entry(new StyledGlyph(entry.getKey(), Style.empty().font(font.key())),
                                entry.getValue())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Find the smallest positive width space in the given font.
     *
     * @param font a font
     * @return a codepoint
     */
    public static Optional<Integer> findSmallestPositiveSpace(final MinecraftFont font) {
        return font.getSpaceCodepoints().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Find the smallest negative width space in the given registry. The returned styled glyph's style has the font that
     * the glyph exists in.
     *
     * @param fontRegistry a registry
     * @return the space
     */
    public static Optional<StyledGlyph> findSmallestNegativeSpace(final MinecraftFontRegistry fontRegistry) {
        return fontRegistry.fonts().stream()
                .map(font -> font.getSpaceCodepoints().entrySet().stream()
                        .filter(entry -> entry.getValue() < 0)
                        .min(Map.Entry.comparingByValue())
                        .map(entry -> Map.entry(new StyledGlyph(entry.getKey(), Style.empty().font(font.key())),
                                entry.getValue())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Find the smallest negative width space in the given font.
     *
     * @param font a font
     * @return a codepoint
     */
    public static Optional<Integer> findSmallestNegativeSpace(final MinecraftFont font) {
        return font.getSpaceCodepoints().entrySet().stream()
                .filter(entry -> entry.getValue() < 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}
