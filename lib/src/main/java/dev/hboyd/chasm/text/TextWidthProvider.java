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

import dev.hboyd.chasm.font.MinecraftFont;
import dev.hboyd.chasm.font.MinecraftFontRegistry;
import dev.hboyd.chasm.font.StyledGlyph;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.Buildable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.Contract;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides width calculation of text.
 */
public class TextWidthProvider implements Buildable<TextWidthProvider, TextWidthProvider.Builder> {
    /**
     * Default instance with the default font as <code>minecraft:default</code> and using the global font registry.
     */
    public static TextWidthProvider DEFAULT = new TextWidthProvider();

    private static final float NODEF_WIDTH = 9.0f;

    private final MinecraftFontRegistry fontRegistry;
    private final Key defaultFont;

    @Contract(pure = true)
    private TextWidthProvider(MinecraftFontRegistry fontRegistry, Key defaultFont) {
        this.fontRegistry = fontRegistry;
        this.defaultFont = defaultFont;
    }

    private TextWidthProvider() {
        this.fontRegistry = MinecraftFontRegistry.GLOBAL;
        this.defaultFont = Key.key(Key.MINECRAFT_NAMESPACE, "default");
    }

    /**
     * Gets the width of the component when rendered in Minecraft.
     * <p>
     * The provider's default font is used when the style does not include one.
     * If the component has a codepoint in which the font has no definition for, the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned.
     * For characters with a shadow, the width will include it.
     *
     * @param component the component to get the width of
     * @param locale   the locale to use
     * @return the width of the component
     * @throws IllegalArgumentException when an unknown font is used
     */
    public float widthOf(Component component, Locale locale) {
        AtomicReference<Float> length = new AtomicReference<>(0f);
        // TODO: Handle multi-line components
        buildFlattener(locale).flatten(component, new FlattenerListener() {
            final Stack<Style> styleStack = new Stack<>();
            Style currentStyle = Style.empty();

            @Override
            public void pushStyle(final Style style) {
                this.styleStack.add(style);
                this.calculateStyle();
            }

            @Override
            public void component(final String text) {
                length.accumulateAndGet(widthOf(text, this.currentStyle), Float::sum);
            }

            @Override
            public void popStyle(final Style style) {
                this.styleStack.removeLast();
                this.calculateStyle();
            }

            private void calculateStyle() {
                final Style.Builder styleBuilder = Style.style();
                for (final Style style : this.styleStack) {
                    styleBuilder.merge(style);
                }
                this.currentStyle = styleBuilder.build();
            }
        });

        return length.get();
    }

    /**
     * Gets the width of the component when rendered in Minecraft.
     * <p>
     * The provider's default font is used when the style does not include one.
     * If the component has a codepoint in which the font has no definition for, the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned.
     * For characters with a shadow, the width will include it.
     *
     * @param component the component to get the width of
     * @return the width of the component
     * @throws IllegalArgumentException when an unknown font is used
     */
    public float widthOf(Component component) {
        return widthOf(component, Locale.getDefault());
    }

    public Map<Integer, Float> getCodepointWidthMap(String text, Style style) {
        Key fontKey = Optional.ofNullable(style.font()).orElse(defaultFont);
        MinecraftFont font = fontRegistry.getFont(fontKey);
        if (font == null)
            throw new IllegalArgumentException("Unknown font: " + fontKey);

        Map<Integer, Float> codePointWidthMap = new HashMap<>();
        for (int i = 0; i < text.length(); i++) {
            int codepoint = text.codePointAt(i);
            if (!codePointWidthMap.containsKey(codepoint)) {
                codePointWidthMap.put(codepoint, font.tryGetWidthOf(i, style).orElse(NODEF_WIDTH));
            }
        }

        return codePointWidthMap;
    }


    /**
     * Gets the width of the text with the given style when rendered in Minecraft.
     * <p>
     * The provider's default font is used when the style does not include one.
     * If the text has a codepoint in which the font has no definition for, the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned.
     * For characters with a shadow, the width will include it.
     *
     * @param text  the text to get the width of
     * @param style the style to use
     * @return the width of the component
     * @throws IllegalArgumentException when an unknown font is used
     */
    public float widthOf(String text, Style style) {
        Key fontKey = Optional.ofNullable(style.font()).orElse(defaultFont);
        MinecraftFont font = fontRegistry.getFont(fontKey);
        if (font == null)
            throw new IllegalArgumentException("Unknown font: " + fontKey);

        return (float) text.codePoints().mapToDouble(i -> font.tryGetWidthOf(i, style).orElse(NODEF_WIDTH)).sum();
    }

    /**
     * Gets the width of the codepoint with the given style when rendered in Minecraft.
     * <p>
     * The provider's default font is used when the style does not include one.
     * If the font does not have a definition for the codepoint the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned.
     * For characters with a shadow, the width will include it.
     *
     * @param codepoint the codepoint to get the width of
     * @param style     the style to adapt the width to
     * @return the rendered width of the codepoint or default
     * @throws IllegalArgumentException when an unknown font is specified
     */
    public float widthOf(int codepoint, Style style) throws NoSuchElementException {
        Key fontKey = Optional.ofNullable(style.font()).orElse(defaultFont);
        MinecraftFont font = fontRegistry.getFont(fontKey);
        if (font == null)
            throw new IllegalArgumentException("Unknown font: " + fontKey);


        return font.tryGetWidthOf(codepoint, style).orElse(NODEF_WIDTH);
    }

    /**
     * Gets the width of the styled glyph when rendered in Minecraft.
     * <p>
     * The provider's default font is used when the style does not include one.
     * If the font glyph does not have a definition for it the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned.
     * For characters with a shadow, the width will include it.
     *
     * @param styledGlyph the StyledGlyph to get the width of
     * @return the rendered width of the StyledGlyph
     * @throws IllegalArgumentException when an unknown font is specified
     */
    public float widthOf(StyledGlyph styledGlyph) {
        return widthOf(styledGlyph.codepoint(), styledGlyph.style());
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Creates a new component width provider builder with default values.
     *
     * @return the new builder
     */
    @Contract(" -> new")
    public static Builder builder() {
        return new TextWidthProvider().toBuilder();
    }

    @Contract("_ -> new")
    private static ComponentFlattener buildFlattener(Locale locale) {
        return ComponentFlattener.basic().toBuilder()
                .complexMapper(TranslatableComponent.class, (translatableComponent, componentConsumer) ->
                        componentConsumer.accept(GlobalTranslator.render(translatableComponent, locale)))
                .build();
    }

    public static class Builder implements Buildable.Builder<TextWidthProvider>, AbstractBuilder<TextWidthProvider> {
        private MinecraftFontRegistry fontRegistry;
        private Key defaultFont;

        @Contract(pure = true)
        Builder(TextWidthProvider textWidthProvider) {
            fontRegistry = textWidthProvider.fontRegistry;
            defaultFont = textWidthProvider.defaultFont;
        }

        /**
         * Sets the font registry to use.
         *
         * @param fontRegistry the font registry
         * @return this
         */
        public @This Builder fontRegistry(MinecraftFontRegistry fontRegistry) {
            this.fontRegistry = fontRegistry;
            return this;
        }

        /**
         * Sets the font to use when no font is specified.
         *
         * @param defaultFont the font.
         * @return this
         */
        public @This Builder defaultFont(Key defaultFont) {
            this.defaultFont = defaultFont;
            return this;
        }

        @Contract(" -> new")
        public TextWidthProvider build() {
            if (!fontRegistry.hasFont(defaultFont))
                throw new IllegalStateException("No font width source for default font key");
            return new TextWidthProvider(fontRegistry, defaultFont);
        }
    }
}
