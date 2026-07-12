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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides width calculation of text.
 */
public class TextWidthProvider implements Buildable<TextWidthProvider, TextWidthProvider.Builder> {
    /**
     * Default instance with the default font as <code>minecraft:default</code> and using the global font registry.
     */
    public static final TextWidthProvider DEFAULT = new TextWidthProvider();

    private static final float NODEF_WIDTH = 9.0f;

    private final MinecraftFontRegistry fontRegistry;
    private final Key defaultFont;

    @Contract(pure = true)
    protected TextWidthProvider(final MinecraftFontRegistry fontRegistry, final Key defaultFont) {
        this.fontRegistry = fontRegistry;
        this.defaultFont = defaultFont;
    }

    private TextWidthProvider() {
        this.fontRegistry = MinecraftFontRegistry.GLOBAL;
        this.defaultFont = Key.key(Key.MINECRAFT_NAMESPACE, "default");
    }

    /**
     * Gets the width of the component when rendered in Minecraft.
     *
     * <p>The provider's default font is used when the style does not include one.</p>
     * If the component has a codepoint in which the font has no definition for, the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned. For characters with a shadow, the width will include it.
     *
     * @param component the component to get the width of
     * @param locale   the locale to use
     * @return the width of the component
     * @throws IllegalArgumentException when an unknown font is used
     */
    public float widthOf(final Component component, final Locale locale) {
        final AtomicReference<Float> length = new AtomicReference<>(0f);
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
                length.accumulateAndGet(TextWidthProvider.this.widthOf(text, this.currentStyle), Float::sum);
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
     *
     * <p>The provider's default font is used when the style does not include one.
     * If the component has a codepoint in which the font has no definition for, the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned. For characters with a shadow, the width will include it.</p>
     *
     * @param component the component to get the width of
     * @return the width of the component
     * @throws IllegalArgumentException when an unknown font is used
     */
    public float widthOf(final Component component) {
        return this.widthOf(component, Locale.getDefault());
    }

    /**
     * Builds a map of the codepoints and their widths for each codepoint within the given text.
     *
     * @param text the string of codepoints to use
     * @param style the style of the given text
     * @return a map of the codepoints and their widths
     */
    public Map<Integer, Float> getCodepointWidthMap(final String text, final Style style) {
        final Key fontKey = Optional.ofNullable(style.font()).orElse(this.defaultFont);
        final MinecraftFont font = this.fontRegistry.getFont(fontKey);
        if (font == null)
            throw new IllegalArgumentException("Unknown font: " + fontKey);

        final Map<Integer, Float> codePointWidthMap = new HashMap<>();
        for (int i = 0; i < text.length(); i++) {
            final int codepoint = text.codePointAt(i);
            if (!codePointWidthMap.containsKey(codepoint)) {
                codePointWidthMap.put(codepoint, font.tryGetWidthOf(i, style).orElse(NODEF_WIDTH));
            }
        }

        return codePointWidthMap;
    }

    /**
     * Gets the width of the text with the given style when rendered in Minecraft.
     *
     * <p>The provider's default font is used when the style does not include one.
     * If the text has a codepoint in which the font has no definition for, the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned. For characters with a shadow, the width will include it.</p>
     *
     * @param text  the text to get the width of
     * @param style the style to use
     * @return the width of the component
     * @throws IllegalArgumentException when an unknown font is used
     */
    public float widthOf(final String text, final Style style) {
        final Key fontKey = Optional.ofNullable(style.font()).orElse(this.defaultFont);
        final MinecraftFont font = this.fontRegistry.getFont(fontKey);
        if (font == null)
            throw new IllegalArgumentException("Unknown font: " + fontKey);

        return (float) text.codePoints().mapToDouble(i -> font.tryGetWidthOf(i, style).orElse(NODEF_WIDTH)).sum();
    }

    /**
     * Gets the width of the codepoint with the given style when rendered in Minecraft.
     *
     * <p>The provider's default font is used when the style does not include one.
     * If the font does not have a definition for the codepoint the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned. For characters with a shadow, the width will include it.</p>
     *
     * @param codepoint the codepoint to get the width of
     * @param style     the style to adapt the width to
     * @return the rendered width of the codepoint or default
     * @throws IllegalArgumentException when an unknown font is specified
     */
    public float widthOf(final int codepoint, final Style style) throws NoSuchElementException {
        final Key fontKey = Optional.ofNullable(style.font()).orElse(this.defaultFont);
        final MinecraftFont font = this.fontRegistry.getFont(fontKey);
        if (font == null)
            throw new IllegalArgumentException("Unknown font: " + fontKey);

        return font.tryGetWidthOf(codepoint, style).orElse(NODEF_WIDTH);
    }

    /**
     * Gets the width of the styled glyph when rendered in Minecraft.
     *
     * <p>The provider's default font is used when the style does not include one.
     * If the font glyph does not have a definition for it the width of the hardcoded
     * <a href="https://minecraft.wiki/w/Missing_textures_and_models#Missing_font_character">.notdef</a>
     * character is returned. For characters with a shadow, the width will include it.</p>
     *
     * @param styledGlyph the StyledGlyph to get the width of
     * @return the rendered width of the StyledGlyph
     * @throws IllegalArgumentException when an unknown font is specified
     */
    public float widthOf(final StyledGlyph styledGlyph) {
        return this.widthOf(styledGlyph.codepoint(), styledGlyph.style());
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
    private static ComponentFlattener buildFlattener(final Locale locale) {
        return ComponentFlattener.basic().toBuilder()
                .complexMapper(TranslatableComponent.class, (component, componentConsumer) -> {
                    final Component renderedComponent = GlobalTranslator.render(component, locale);
                    // We need to check if the translation failed to render
                    if (renderedComponent instanceof final TranslatableComponent translatableComponent) {
                        String fallback = translatableComponent.fallback();
                        if (fallback == null) fallback = translatableComponent.key();

                        componentConsumer.accept(Component.text(fallback, translatableComponent.style()));
                    } else componentConsumer.accept(renderedComponent);
                })
                .build();
    }

    /**
     * The builder for {@link TextWidthProvider}.
     */
    public static class Builder implements Buildable.Builder<TextWidthProvider>, AbstractBuilder<TextWidthProvider> {
        private MinecraftFontRegistry fontRegistry;
        private Key defaultFont;

        @Contract(pure = true)
        protected Builder(final TextWidthProvider textWidthProvider) {
            this.fontRegistry = textWidthProvider.fontRegistry;
            this.defaultFont = textWidthProvider.defaultFont;
        }

        /**
         * Sets the font registry to use.
         *
         * @param fontRegistry the font registry
         * @return this
         */
        public @This Builder fontRegistry(final MinecraftFontRegistry fontRegistry) {
            this.fontRegistry = fontRegistry;
            return this;
        }

        /**
         * Sets the font to use when no font is specified.
         *
         * @param defaultFont the font.
         * @return this
         */
        public @This Builder defaultFont(final Key defaultFont) {
            this.defaultFont = defaultFont;
            return this;
        }

        /**
         * Builds the configured text width provider.
         *
         * @return a new text width provider
         */
        @Contract(" -> new")
        public TextWidthProvider build() {
            if (!this.fontRegistry.hasFont(this.defaultFont))
                throw new IllegalStateException("No font width source for default font key");
            return new TextWidthProvider(this.fontRegistry, this.defaultFont);
        }
    }
}
