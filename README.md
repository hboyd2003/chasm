# Chasm

[![Hboyd's Repo Version Badge](https://repo.hboyd.dev/api/badge/latest/releases/dev/hboyd/chasm-lib?&name=Hboyds%20Release%20Repo&prefix=v)](https://repo.hboyd.dev/#/releases/dev/hboyd/chasm-lib) [![Hboyd's Repo Version Badge](https://repo.hboyd.dev/api/badge/latest/snapshots/dev/hboyd/chasm-lib?&name=Hboyds%20Snapshot%20Repo&prefix=v)](https://repo.hboyd.dev/#/releases/dev/hboyd/chasm-lib)

A library for calculating the width of and spacing text/components in Minecraft: Java Edition.

Chasm provides the ability to calculate the display width of any text using any font. By default, it provides a
simplified version of Minecraft's default font but provided with a resource pack it can load the fonts within and
provide width calculations for them. For loading of all of Minecraft's default fonts the chasm-plugin is available.

## Usage

Use Chasm as a library dependency from your Gradle project.

### Installing

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.hboyd.dev/snapshots/")
}

dependencies {
    implementation("dev.hboyd:chasm-lib:1.0.1")
}
```

### Glyph

A glyph is the specific visual representation of character or a 'slot' in text. It isn't necessarily a single character,
however. A glyph can be made up of multiple characters that are then visually displayed as a single character or
text 'unit'. 

For example the glyph 'x̣' visually appears as one character but is actually made up of the Unicode characters/codepoints
'0074' and '0323'.

For more examples and a detailed see the
[Unicode 17.0.0 standard 2.2.3](https://www.unicode.org/versions/Unicode17.0.0/core-spec/chapter-2/#G8085).

### Glyph Definition Providers

A `GlyphDefinitionProvider` provides the widths for a set of glyphs. There are five standard providers,
`BitmapGlyphDefinitionProvider`, `SpaceGlyphDefinitionProvider`, `TrueTypeGlypDefinitionProvider`
and `UnihexDefinitionProvider` which represent the Minecraft [bitmap](https://minecraft.wiki/w/Font#Bitmap_provider),
[space](https://minecraft.wiki/w/Font#Space_provider), [TTF](https://minecraft.wiki/w/Font#TTF_provider),
and [Unihex](https://minecraft.wiki/w/Font#Unihex_provider) font providers respectively. Additionally, there is the
`BuiltinGlyphDefinitionProvider` which is a hard coded singleton provider of the glyphs in the default
`minecraft:font/include` and `minecraft:include/space` fonts.

### Minecraft Font

A `MinecraftFont` is a keyed set of multiple `GlyphDefinitionProvider`s. A static `BUILTIN` instance is accessible which
contains the previously mentioned `BuiltinGlyphDefinitionProvider` provider.

### Minecraft Font Registry

A `MinecraftFontRegistry` is a mutable set of multiple `MinecraftFont`s with accessors for font's based on key. A static
`GLOBAL` instance is accessible which contains the previously mentioned `BUILTIN` `MinecraftFont` instance.
Additionally, the Chasm companion plugin adds the fonts it generates to this instance.

### Minecraft Font Generator

The `MinecraftFontGenerator` builds `GlyphDefinitionProvider`s to form `MinecraftFont`s using a given resource pack and
registers the fonts to the set registry.

### Width calculation

`TextWidthProvider` calculates the rendered width of Adventure components, strings, codepoints, and `StyledGlyph`s. The
`TextWidthProvider` uses fonts registered with the `FontRegistry` it is created with.

## License

This project is licensed under the GNU Lesser General Public License v3.0. See [LICENSE.md](LICENSE.md).
