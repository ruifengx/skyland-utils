package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.SkylandUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public enum AllFluidTags implements ITag.INamedTag<Fluid> {
    ULTRA_WARM,     // more active in ultrawarm world
    VAPORIZING,     // whether this fluid should vaporize in ultrawarm worlds

    ALLOW_SWIMMING, // controls swimming and fall damage cancellation
    CLEAR_FIRE,     // extinguishes fires on entities
    SCALDING,       // serves as damage source of high temperature
    IGNITING,       // ignites entities
    ;

    private final ITag.INamedTag<Fluid> tag;

    AllFluidTags() { this(SkylandUtils.MODID); }
    AllFluidTags(String modId) {
        this.tag = FluidTags.bind(modId + ":" + this.name().toLowerCase());
    }

    public static void register() {}

    @Override @NotNull public ResourceLocation getName() { return this.tag.getName(); }
    @Override public boolean contains(@NotNull Fluid element) { return this.tag.contains(element); }
    @Override @NotNull public List<Fluid> getValues() { return this.tag.getValues(); }
    @Override @NotNull public Fluid getRandomElement(@NotNull Random random) {
        return this.tag.getRandomElement(random);
    }
}
