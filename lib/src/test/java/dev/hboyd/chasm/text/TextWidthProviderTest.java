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

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextWidthProviderTest {

    private static final Key TRANSLATION_STORE_KEY = Key.key("test",
            "teststore");

    private static MiniMessageTranslationStore miniMessageTranslationStore;

    @BeforeAll
    static void configureTranslationStore() {
        miniMessageTranslationStore = MiniMessageTranslationStore.create(TRANSLATION_STORE_KEY);
        miniMessageTranslationStore.register("translation.key", Locale.ENGLISH, "a trans<blue>lated <green>text");
        miniMessageTranslationStore.register("translation.arguments", Locale.ENGLISH, "a <val> translation");
        GlobalTranslator.translator().addSource(miniMessageTranslationStore);
    }

    @AfterAll
    static void removeTranslationStore() {
        GlobalTranslator.translator().removeSource(miniMessageTranslationStore);
    }

    private static Stream<Arguments> componentsAndExpectedWidthArguments() {
        return Stream.of(
                Arguments.of(Component.text("text"), 20),
                Arguments.of(Component.text("text").append(Component.text("appended")), 68),
                Arguments.of(Component.text("text").append(Component.translatable("translation.key")), 107),
                Arguments.of(Component.text("text").append(Component.translatable("translation.arguments", Argument.numeric("val", 4))), 95),
                Arguments.of(Component.text("text", Style.style(TextDecoration.BOLD)), 24),
                Arguments.of(Component.text("text")
                        .append(Component.text("more text", Style.style(TextDecoration.BOLD))), 77)
        );
    }

    @ParameterizedTest
    @MethodSource("componentsAndExpectedWidthArguments")
    void providerCorrectlyCalculatesWidthOfComponent(final Component component, final float expectedWidth) {
        assertEquals(expectedWidth, TextWidthProvider.DEFAULT.widthOf(component));
    }
}
