package com.github.ruifengx.skylandutils.item;

import com.github.ruifengx.skylandutils.SkylandUtils;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public enum AllItemTags implements ITag.INamedTag<Item> {
    BUCKETS("forge"),   // all buckets
    ;

    private final ITag.INamedTag<Item> tag;

    AllItemTags() { this(SkylandUtils.MODID); }
    AllItemTags(String modId) {
        this.tag = ItemTags.makeWrapperTag(modId + ":" + this.name().toLowerCase());
    }

    @Override @NotNull public ResourceLocation getName() { return this.tag.getName(); }
    @Override public boolean contains(@NotNull Item element) { return this.tag.contains(element); }
    @Override @NotNull public List<Item> getAllElements() { return this.tag.getAllElements(); }
    @Override @NotNull public Item getRandomElement(@NotNull Random random) {
        return this.tag.getRandomElement(random);
    }
}
