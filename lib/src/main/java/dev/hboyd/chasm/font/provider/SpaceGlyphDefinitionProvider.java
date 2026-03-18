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

import java.util.*;

/**
 * Represents the Minecraft space font provider.
 *
 * @see <a href="https://minecraft.wiki/w/Font#Space_provider">Space provider - Font - Minecraft Wiki</a>
 */
public class SpaceGlyphDefinitionProvider implements GlyphDefinitionProvider {
    private final Map<Integer, Float> charMap;

    public SpaceGlyphDefinitionProvider(Map<Integer, Float> charMap) {
        this.charMap = Map.copyOf(charMap);
    }

    @Override
    public Optional<Float> tryGetWidthOf(int codepoint, Style style) {
        return Optional.ofNullable(charMap.get(codepoint))
                .map(width -> width + (style.hasDecoration(TextDecoration.BOLD) ? 1 : 0));
    }

    @Override
    public @Unmodifiable Map<Integer, Float> getSpaceCodepoints() {
        return charMap;
    }
}
