package com.github.ruifengx.skylandutils.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public final class TranslationUtil {
    public static ITextComponent getTagDescription(String category, ResourceLocation tagName) {
        final LanguageMap langMap = LanguageMap.getInstance();
        final String id = Util.makeDescriptionId("tag." + category, tagName);
        if (langMap.has(id)) return new TranslationTextComponent(id);
        return new StringTextComponent(tagName.toString());
    }
}
