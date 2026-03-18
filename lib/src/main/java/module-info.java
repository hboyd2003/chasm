/**
 * A library for calculating the width of and spacing text in Minecart: Java Edition.
 */
module chasm.chasm.lib.main {
    requires transitive org.checkerframework.checker.qual;
    requires transitive org.jetbrains.annotations;
    requires transitive org.jspecify;
    requires transitive net.kyori.adventure; // To be fixed in Adventure 5.0
    requires transitive net.kyori.adventure.key;

    requires com.google.common;
    requires com.google.gson;
    requires java.datatransfer;
    requires java.desktop;
    requires net.kyori.adventure.text.minimessage;
    requires net.kyori.examination.api;
    requires org.spongepowered.configurate;
    requires net.kyori.adventure.text.serializer.plain;
    requires net.kyori.adventure.text.serializer.legacy;

    exports dev.hboyd.chasm;
    exports dev.hboyd.chasm.font;
    exports dev.hboyd.chasm.font.provider;
    exports dev.hboyd.chasm.text;
}