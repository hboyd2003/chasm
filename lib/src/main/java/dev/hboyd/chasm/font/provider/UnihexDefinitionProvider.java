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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * Represents the Minecraft unihex font provider.
 *
 * @see <a href="https://minecraft.wiki/w/Font#Unihex_provider">Unihex provider - Font - Minecraft Wiki</a>
 */
public class UnihexDefinitionProvider implements GlyphDefinitionProvider {
    private final TreeRangeMap<Integer, Integer> charWidthRangeMap;

    public UnihexDefinitionProvider(Path rawHexFilePath) throws IOException {
        final TreeMap<Integer, Integer> charWidthMap = new TreeMap<>();

        if (Files.notExists(rawHexFilePath))
            throw new IllegalArgumentException("raw hex file does not exist");


        try (InputStream hexInputStream = Files.newInputStream(rawHexFilePath) ;
             InputStreamReader hexInputStreamReader = new InputStreamReader(hexInputStream);
             BufferedReader hexBufferedReader = new BufferedReader(hexInputStreamReader)) {

            String line;
            while ((line = hexBufferedReader.readLine()) != null) {
                String[] strings = line.split(":");
                int codepoint = Integer.parseInt(strings[0], 16);

                final int bytesPerRow = strings[1].length() >= 64 ? 4 : 2;
                final int pixelsPerRow = bytesPerRow * 4;
                final int filledRow = strings[1].length() >= 64 ? 0xFFFFFFFF : 0xFFFF;

                int minLeftBit = pixelsPerRow;
                int maxRightBit = 0;
                for (int cursor = 0; cursor < strings[1].length(); cursor += bytesPerRow) {
                    final int rowValue = Integer.valueOf(strings[1].substring(cursor, cursor + bytesPerRow), 16);
                    if (rowValue == 0) continue;
                    if (rowValue == filledRow) {
                        minLeftBit = 0;
                        maxRightBit = pixelsPerRow - 1;
                        break;
                    }

                    for (int leftBit = 0; leftBit < pixelsPerRow; leftBit++) {
                        if (((rowValue >> leftBit) & 1) == 1) {
                            if (leftBit < minLeftBit) minLeftBit = leftBit;
                            break;
                        }
                    }


                    for (int rightBit = pixelsPerRow - 1; rightBit > minLeftBit; rightBit--) {
                        if (((rowValue >> (rightBit - 1)) & 1) == 1) {
                            if (rightBit > maxRightBit) maxRightBit = rightBit;
                            break;
                        }
                    }
                }

                charWidthMap.put(codepoint, maxRightBit < minLeftBit ? 0 : maxRightBit - minLeftBit + 1); // Add one to account for rendered shadow
            }
        }

        this.charWidthRangeMap = ProviderUtil.coalesceCharMap(charWidthMap);
    }

    @Override
    public Optional<Float> tryGetWidthOf(int codepoint, Style style) {
        return Optional.ofNullable(charWidthRangeMap.get(codepoint))
                .map(width -> width + (style.hasDecoration(TextDecoration.BOLD) ? 1 : 0))
                .map(Integer::floatValue);
    }

    @Override
    public @Unmodifiable Map<Integer, Float> getSpaceCodepoints() {
        return Map.of(); // Unihex provider cannot have spaces
    }
}
