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

package dev.hboyd.chasm.text;

import dev.hboyd.chasm.UIContainer;
import dev.hboyd.chasm.font.StyledGlyph;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Arrays;
import java.util.Locale;

/**
 * Utilities for spacing {@link Component}s.
 */
// TODO: Handle multi-line components and wrapping when Adventure 5.0 releases
public final class ComponentSpacer {

    private ComponentSpacer() {}

    /**
     * Left aligns the component by padding it with the provided glyph.
     *
     * @param component      the component to center
     * @param paddingGlyph   the glyph to pad with
     * @param widthProvider  the width provider to use
     * @param containerWidth the container width
     * @param locale         the locale to translate with
     * @return a copy of the component left aligned
     */
    public static Component alignLeft(final Component component,
                                      final StyledGlyph paddingGlyph,
                                      final TextWidthProvider widthProvider,
                                      final float containerWidth,
                                      final Locale locale) {
        final int paddingCount = findNeededPaddingCount(component, paddingGlyph, widthProvider, containerWidth, locale);

        return Component.text()
                .append(component)
                .append(Component.text(Character.toString(paddingGlyph.codepoint())
                        .repeat(paddingCount), paddingGlyph.style()))
                .build();
    }

    /**
     * Left aligns the component by padding it with the provided glyph.
     *
     * @param component     the component to center
     * @param paddingGlyph  the glyph to pad with
     * @param widthProvider the width provider to use
     * @return a copy of the component left aligned
     */
    public static Component alignLeft(final Component component,
                                       final StyledGlyph paddingGlyph,
                                       final TextWidthProvider widthProvider) {
        return alignLeft(component,
                paddingGlyph,
                widthProvider,
                UIContainer.CHAT.width(),
                Locale.getDefault());
    }

    /**
     * Center aligns the component by padding it with the provided glyph.
     *
     * @param component     the component to center
     * @param paddingGlyph  the glyph to pad with
     * @param widthProvider the width provider to use
     * @param containerWidth the container width
     * @param locale         the locale to translate with
     * @return a copy of the component centered
     */
    public static Component alignCenter(final Component component,
                                        final StyledGlyph paddingGlyph,
                                        final TextWidthProvider widthProvider,
                                        final float containerWidth,
                                        final Locale locale) {
        final float paddingGlyphCount = findNeededPaddingCount(component, paddingGlyph, widthProvider, containerWidth, locale);
        final Component paddingComponent = Component.text(
                Character.toString(paddingGlyph.codepoint()).repeat((int) (paddingGlyphCount / 2)),
                paddingGlyph.style());

        final TextComponent.Builder builder = Component.text()
                .append(paddingComponent)
                .append(component)
                .append(paddingComponent);

        if (paddingGlyphCount % 2 >= 1)
            builder.append(paddingGlyph.toComponent());

        return builder.build();
    }

    /**
     * Center aligns the component by padding it with the provided glyph.
     *
     * @param component     the component to center
     * @param paddingGlyph  the glyph to pad with
     * @param widthProvider the width provider to use
     * @return a copy of the component centered
     */
    public static Component alignCenter(final Component component,
                                        final StyledGlyph paddingGlyph,
                                        final TextWidthProvider widthProvider) {
        return alignCenter(component,
                paddingGlyph,
                widthProvider,
                UIContainer.CHAT.width(),
                Locale.getDefault());
    }

    /**
     * Right aligns the component by padding it with the provided glyph.
     *
     * @param component     the component to center
     * @param paddingGlyph  the glyph to pad with
     * @param widthProvider the width provider to use
     * @param containerWidth the container width
     * @param locale         the locale to translate with
     * @return a copy of the component right aligned
     */
    public static Component alignRight(final Component component,
                                       final StyledGlyph paddingGlyph,
                                       final TextWidthProvider widthProvider,
                                       final float containerWidth,
                                       final Locale locale) {
        final int paddingCount = findNeededPaddingCount(component, paddingGlyph, widthProvider, containerWidth, locale);

        return Component.text()
                .append(Component.text(Character.toString(paddingGlyph.codepoint())
                                .repeat(paddingCount), paddingGlyph.style()))
                .append(component)
                .build();
    }

    /**
     * Right aligns the component by padding it with the provided glyph.
     *
     * @param component     the component to center
     * @param paddingGlyph  the glyph to pad with
     * @param widthProvider the width provider to use
     * @return a copy of the component right aligned
     */
    public static Component alignRight(final Component component,
                                       final StyledGlyph paddingGlyph,
                                       final TextWidthProvider widthProvider) {
        return alignRight(component,
                paddingGlyph,
                widthProvider,
                UIContainer.CHAT.width(),
                Locale.getDefault());
    }

    /**
     * Justifies the given component.
     *
     * @param component      the component to justify
     * @param widthProvider  the width provider to use
     * @param containerWidth the container width
     * @param locale         the locale to translate with
     * @return the justified component
     */
    public static Component justify(final Component component,
                                    final TextWidthProvider widthProvider,
                                    final float containerWidth,
                                    final Locale locale) {
        // TODO: Dynamically grab the smallest possible space and use it to more accurately justify the text
        final String minimessage = MiniMessage.miniMessage().serialize(component.compact());

        final int spacesNeeded = findNeededPaddingCount(component, new StyledGlyph(' '), widthProvider, containerWidth, locale);
        final int[] codepoints = minimessage.codePoints().toArray();
        final int whitespaceCount = (int) Arrays.stream(codepoints)
                .filter(Character::isWhitespace)
                .count();
        if (whitespaceCount == 0) return component;

        final String spacesPerWhitespace = " ".repeat(Math.toIntExact(spacesNeeded / whitespaceCount));
        int remainder = spacesNeeded % whitespaceCount;

        int depth = 0;
        final StringBuilder builder = new StringBuilder();
        for (final int codepoint : codepoints) {
            if (depth == 0 && Character.isWhitespace(codepoint)) {
                builder.append(spacesPerWhitespace);
                if (remainder > 0) {
                    builder.append(" ");
                    remainder--;
                }
            } else if (codepoint == '<')
                depth++;
            else if (codepoint == '>' && depth > 0)
                depth--;

            builder.appendCodePoint(codepoint);
        }

        return MiniMessage.miniMessage().deserialize(builder.toString());
    }

    /**
     * Justifies the given component.
     *
     * @param component     the component to justify
     * @param widthProvider the width provider to use
     * @return the justified component
     */
    public static Component justify(final Component component,
                                    final TextWidthProvider widthProvider) {
        return justify(component, widthProvider, UIContainer.CHAT.width(), Locale.getDefault());
    }

    /**
     * Splits the components between the left and the right.
     * The left component will be left aligned while the right component will be right aligned.
     *
     * @param leftComponent  the component to be on the left
     * @param rightComponent the component to be on the right
     * @param widthProvider  the width provider to use
     * @param containerWidth the container width
     * @param locale         the locale to translate with
     * @return a copy of the component split
     */
    public static Component split(final Component leftComponent,
                                  final Component rightComponent,
                                  final TextWidthProvider widthProvider,
                                  final float containerWidth,
                                  final Locale locale) {
        final int spacesNeeded = findNeededPaddingCount(leftComponent.append(rightComponent),
                new StyledGlyph(' '),
                widthProvider,
                containerWidth,
                locale);

        return Component.text()
                .append(leftComponent)
                .append(Component.text(Character.toString(' ').repeat(spacesNeeded), Style.empty()))
                .append(rightComponent)
                .build();
    }

    /**
     * Splits the components between the left and the right.
     * The left component will be left aligned while the right component will be right aligned.
     *
     * @param leftComponent  the component to be on the left
     * @param rightComponent the component to be on the right
     * @param widthProvider  the width provider to use
     * @return a copy of the component split
     */
    public static Component split(final Component leftComponent,
                                  final Component rightComponent,
                                  final TextWidthProvider widthProvider) {
        return split(leftComponent, rightComponent, widthProvider, UIContainer.CHAT.width(), Locale.getDefault());
    }

    // TODO: Implement a flex-box like column container.

    private static int findNeededPaddingCount(final Component component,
                                              final StyledGlyph paddingGlyph,
                                              final TextWidthProvider widthProvider,
                                              final float containerWidth,
                                              final Locale locale) {
        final float componentWidth = widthProvider.widthOf(component, locale);
        if (componentWidth > containerWidth)
            throw new IllegalArgumentException("Width of component exceeds the container width of " + containerWidth);

        final float paddingGlyphWidth = widthProvider.widthOf(paddingGlyph);
        if (paddingGlyphWidth == 0)
            throw new IllegalArgumentException("Width of padding glyph cannot be 0");

        return (int) ((containerWidth - componentWidth) / paddingGlyphWidth);
    }
}
