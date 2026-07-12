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

import com.google.common.collect.TreeRangeMap;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Unmodifiable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Represents the Minecraft bitmap font provider.
 *
 * @see <a href="https://minecraft.wiki/w/Font#Bitmap_provider">Bitmap provider - Font - Minecraft Wiki</a>
 */
public class BitmapGlyphDefinitionProvider implements GlyphDefinitionProvider {
    public static final int DEFAULT_HEIGHT = 8;

    private final TreeRangeMap<Integer, Float> charWidthRangeMap;

    /**
     * Creates a new code provider based on a given bitmap and the codepoints contained.
     *
     * @param codepointTable codepoints to map each glyph to
     * @param bitmapPath     path to the bitmap which contains all glyphs
     * @param height         height of each glyph
     * @throws IOException if unable to read the given bitmap file.
     */
    public BitmapGlyphDefinitionProvider(final int[][] codepointTable,
                                         final Path bitmapPath,
                                         final int height) throws IOException {
        final TreeMap<Integer, Float> charWidthMap = new TreeMap<>();

        try (final InputStream charImageInputStream = Files.newInputStream(bitmapPath)) {
            final BufferedImage charImage = ImageIO.read(charImageInputStream);

            final int cellHeight = Math.floorDiv(charImage.getHeight(), codepointTable.length);
            final int cellWidth = Math.floorDiv(charImage.getWidth(), codepointTable[0].length);
            final float scale = (float) height / cellHeight;

            int cursorY = 0;
            for (final int[] codePoints : codepointTable) {
                int cursorX = 0;
                for (final int codepoint : codePoints) {
                    if (codepoint != 0) {
                        final BufferedImage cell = charImage.getSubimage(cursorX, cursorY, cellWidth, cellHeight);
                        charWidthMap.put(codepoint, (findRightImageBound(cell) + 2) * scale); // Add one to account for rendered shadow (shadow takes up space)
                    }
                    cursorX += cellWidth;
                }
                cursorY += cellHeight;
            }
        }

        this.charWidthRangeMap = ProviderUtil.coalesceCharMap(charWidthMap);
    }

    /**
     * Creates a new code provider based on a given bitmap and the codepoints contained.
     *
     * @param codepointTable codepoints to map each glyph to
     * @param bitmapPath     path to the bitmap which contains all glyphs
     * @throws IOException if unable to read the given bitmap file
     */
    public BitmapGlyphDefinitionProvider(final int[][] codepointTable, final Path bitmapPath) throws IOException {
        this(codepointTable, bitmapPath, DEFAULT_HEIGHT);
    }

    @Override
    public Optional<Float> tryGetWidthOf(final int codepoint, final Style style) {
        return Optional.ofNullable(this.charWidthRangeMap.get(codepoint))
                .map(width -> width + (style.hasDecoration(TextDecoration.BOLD) ? 1 : 0));
    }

    @Override
    public @Unmodifiable Map<Integer, Float> getSpaceCodepoints() {
        return Map.of(); // Bitmaps cannot have spaces
    }

    private static int findRightImageBound(final BufferedImage image) {
        for (int cursorX = image.getWidth() - 1; cursorX >= 0; cursorX--) {
            for (int cursorY = image.getHeight() - 1; cursorY >= 0; cursorY--) {
                if (image.getColorModel().getAlpha(image.getRaster().getDataElements(cursorX, cursorY, null)) != 0)
                    return cursorX;
            }
        }

        return -1;
    }
}
