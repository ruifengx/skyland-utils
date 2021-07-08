package com.github.ruifengx.skylandutils.util;

import net.minecraftforge.fluids.FluidAttributes;

public interface IFluidAttributesExtra {
    FluidAttributes.Builder lavaLike();
    boolean isLavaLike();

    FluidAttributes.Builder waterLike();
    boolean isWaterLike();
}
