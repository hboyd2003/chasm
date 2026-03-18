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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Contract;

/**
 * A single Glyph or codepoint with a style.
 */
public class StyledGlyph {
    private final int codepoint;
    private final Style style;

    /**
     * Creates a styled glyph.
     *
     * @param character the character
     * @param style the style
     */
    @Contract(pure = true)
    public StyledGlyph(char character, Style style) {
        this.codepoint = character;
        this.style = style;
    }

    /**
     * Creates a styled glyph with an empty style.
     *
     * @param character the character
     */
    @Contract(pure = true)
    public StyledGlyph(char character) {
        this(character, Style.empty());
    }

    /**
     * Creates a styled glyph.
     *
     * @param codepoint the codepoint
     * @param style the style
     */
    @Contract(pure = true)
    public StyledGlyph(int codepoint, Style style) {
        this.codepoint = codepoint;
        this.style = style;
    }

    /**
     * Creates a styled glyph with an empty style.
     *
     * @param codepoint the codepoint
     */
    @Contract(pure = true)
    public StyledGlyph(int codepoint) {
        this(codepoint, Style.empty());
    }

    /**
     * Builds a {@link TextComponent} with the codepoint and style.
     *
     * @return the built component
     */
    public TextComponent toComponent() {
        return Component.text(Character.toString(codepoint), style);
    }

    /**
     * Gets the codepoint.
     *
     * @return the codepoint
     */
    public int codepoint() {
        return codepoint;
    }


    /**
     * Gets the style.
     *
     * @return the style
     */
    public Style style() {
        return style;
    }
}
