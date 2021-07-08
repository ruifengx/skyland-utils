package com.github.ruifengx.skylandutils.util;

import net.minecraft.world.IWorldReader;

public interface IFlowingFluidAccessor {
    int levelDecreasePerBlock(IWorldReader worldIn);
}
