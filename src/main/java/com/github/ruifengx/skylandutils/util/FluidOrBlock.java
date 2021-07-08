package com.github.ruifengx.skylandutils.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface FluidOrBlock {
    static FluidOrBlock from(Fluid fluid) {
        return (fluidState, blockState) -> fluidState.getFluid().isEquivalentTo(fluid);
    }

    static FluidOrBlock from(Block block) {
        return (fluidState, blockState) -> blockState.matchesBlock(block);
    }

    boolean check(FluidState fluidState, BlockState blockState);
}
