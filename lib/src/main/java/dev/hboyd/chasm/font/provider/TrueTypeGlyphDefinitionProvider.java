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

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the Minecraft ttf font provider.
 *
 * @see <a href="https://minecraft.wiki/w/Font#TTF_provider">TTF provider - Font - Minecraft Wiki</a>
 */
public class TrueTypeGlyphDefinitionProvider implements GlyphDefinitionProvider {
    final Font font;
    private final List<Integer> exclusions;

    /**
     * Creates a new TrueType glyph provider based on the TrueType file provided.
     *
     * @param ttfPath the path to the TrueType file
     * @param fontSize relative scale of the glyphs
     * @param exclusions a string of characters to ignore when ingesting the TrueType file
     * @throws IOException if unable to read the TrueType file
     * @throws FontFormatException if the given TrueType file is not in the correct format
     */
    public TrueTypeGlyphDefinitionProvider(final Path ttfPath, final float fontSize, final String exclusions) throws IOException, FontFormatException {
        if (!Files.exists(ttfPath))
            throw new IllegalArgumentException("ttf files does not exist");

        try (final InputStream is = Files.newInputStream(ttfPath)) {
            this.font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(fontSize);
            this.exclusions = exclusions.codePoints().boxed().toList();
        }
    }

    @Override
    public Optional<Float> tryGetWidthOf(final int codepoint, final Style style) {
        if (this.exclusions.contains(codepoint) || !this.font.canDisplay(codepoint))
            return Optional.empty();

        Font styledFont = this.font;
        if (style.hasDecoration(TextDecoration.BOLD)) {
            styledFont = this.font.deriveFont(Font.BOLD);
        } else if (style.hasDecoration(TextDecoration.ITALIC)) {
            styledFont = this.font.deriveFont(Font.ITALIC);
        }

        return Optional.of((float) styledFont.getStringBounds(String.valueOf((char) codepoint),
                new FontRenderContext(null, true, true)).getWidth());
    }

    @Override
    public @Unmodifiable Map<Integer, Float> getSpaceCodepoints() {
        return Map.of(); // ttf provider cannot have spaces
    }
}
