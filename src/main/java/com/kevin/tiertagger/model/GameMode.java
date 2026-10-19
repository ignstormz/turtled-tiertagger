package com.kevin.tiertagger.model;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.Optional;

public record GameMode(String id, String title) {
    public static final GameMode NONE = new GameMode("annoying_long_id_that_no_one_will_ever_use_just_to_make_sure", "§cNone§r");

    public boolean isNone() {
        return this.id.equals(NONE.id);
    }

    private Pair<Character, TextColor> iconAndColor() {
        return switch (this.id) {
            case "vanilla" -> Pair.of('\uE701', TextColor.fromRgb(0xc7a4ed));
            case "uhc" -> Pair.of('\uE702', TextColor.fromRgb(0xf07980));
            case "pot" -> Pair.of('\uE703', TextColor.fromRgb(0xff728f));
            case "nethop" -> Pair.of('\uE704', TextColor.fromRgb(0xa178b6));
            case "smp" -> Pair.of('\uE705', TextColor.fromRgb(0x47d0aa));
            case "sword" -> Pair.of('\uE706', TextColor.fromRgb(0x6ac8ff));
            case "axe" -> Pair.of('\uE707', TextColor.fromRgb(0x6ac8ff));
            case "mace" -> Pair.of('\uE708', TextColor.fromRgb(0x9995ae));
            case "spearmace" -> Pair.of('\uE709', TextColor.fromRgb(0xa98d83));
            case "cart" -> Pair.of('\uE70A', TextColor.fromRgb(0xc9c9c9));
            case "creeper" -> Pair.of('\uE70B', TextColor.fromRgb(0x8bdb88));
            case "diamondsmp" -> Pair.of('\uE70C', TextColor.fromRgb(0xb9a8e9));
            default -> Pair.of('•', TextColor.fromLegacyFormat(ChatFormatting.WHITE));
        };
    }

    public Optional<Character> icon() {
        Pair<Character, TextColor> pair = this.iconAndColor();

        return pair.right().getValue() == 0xFFFFFF ? Optional.empty() : Optional.of(pair.left());
    }

    public Component asStyled(boolean withDefaultDot) {
        Pair<Character, TextColor> pair = this.iconAndColor();

        if (pair.right().getValue() == 0xFFFFFF && !withDefaultDot) {
            return Component.literal(this.title);
        } else {
            Component name = Component.literal(this.title).withStyle(s -> s.withColor(pair.right()));
            return Component.literal(pair.left() + " ").append(name);
        }
    }
}
