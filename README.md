# Chasm

A library for calculating the width of and spacing text/components in Minecraft: Java Edition.

Chasm provides the ability to calculate the display width of any text using any font. By default, it provides a
simplified version of Minecraft's default font but provided with a resource pack it can load the fonts within and
provide width calculations for them. For loading of all of Minecraft's default fonts the chasm-plugin is available.

## Getting Started

Use Chasm as a library dependency from your Gradle project.

### Installing

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.hboyd.dev/snapshots/")
}

dependencies {
    implementation("dev.hboyd:chasm-lib:1.0.0")
}
```

## Usage

### Width calculation

`TextWidthProvider` calculates the rendered width of Adventure components, strings, codepoints, and `StyledGlyph`s.
The `TextWidthProvider` uses fonts registered with the `FontRegistry` it is created with. 

## License

This project is licensed under the GNU Lesser General Public License v3.0. See [LICENSE.md](LICENSE.md).