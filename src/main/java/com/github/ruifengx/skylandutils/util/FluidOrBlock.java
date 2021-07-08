package com.github.ruifengx.skylandutils.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public interface FluidOrBlock {
    static FluidOrBlock from(Fluid fluid) {
        return (fluidState, blockState) -> fluidState.getFluid().isEquivalentTo(fluid);
    }

    static FluidOrBlock from(Block block) {
        return (fluidState, blockState) -> blockState.matchesBlock(block);
    }

    static FluidOrBlock fromExact(Fluid fluid) {
        return (fluidState, blockState) -> fluidState.getFluid() == fluid;
    }

    static Predicate<Fluid> exact(Fluid fluid) {
        return otherFluid -> otherFluid == fluid;
    }

    static Predicate<Fluid> matches(Fluid fluid) {
        return otherFluid -> otherFluid.isEquivalentTo(fluid);
    }

    static Predicate<BlockState> matches(Block block) {
        return otherBlock -> otherBlock.matchesBlock(block);
    }

    static <T> Predicate<T> whatever() { return x -> true; }

    boolean check(FluidState fluidState, BlockState blockState);
}
