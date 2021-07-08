package com.github.ruifengx.skylandutils.util;

import com.github.ruifengx.skylandutils.mixin.DispenseBehaviourAccessor;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import slimeknights.mantle.registration.FluidBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FluidUtil {
    public interface FluidBuilderAccessor {
        Supplier<? extends Item> getBucketItem();
    }

    private static final IDispenseItemBehavior dispenseBucketBehaviour
        = DispenseBehaviourAccessor.getDispenseBehaviourRegistry().get(Items.WATER_BUCKET);

    private static final List<Supplier<BucketItem>> ALL_BUCKETS = new ArrayList<>();

    public static void registerBucketForDispenser(Supplier<BucketItem> bucket) { ALL_BUCKETS.add(bucket); }
    public static void registerAllBuckets() {
        for (Supplier<BucketItem> bucket : ALL_BUCKETS)
            DispenserBlock.registerDispenseBehavior(bucket.get(), dispenseBucketBehaviour);
    }

    public static Supplier<BucketItem> getBucket(FluidBuilder builder) {
        return (() -> (BucketItem) ((FluidBuilderAccessor) builder).getBucketItem().get());
    }

    public static int getLevelDecreasePerBlock(FluidState fluidState, IWorldReader world) {
        if (fluidState.getFluid() instanceof FlowingFluid) {
            return ((IFlowingFluidAccessor) fluidState.getFluid()).levelDecreasePerBlock(world);
        }
        return 1;
    }

    public static boolean canDisplace(FluidState toState, IBlockReader world, BlockPos toPos, Direction direction) {
        BlockPos fromPos = toPos.offset(direction.getOpposite());
        FluidState fromState = world.getFluidState(fromPos);
        double h = fromState.getLevel();
        double hOther = toState.getLevel();
        int dec = getLevelDecreasePerBlock(fromState, (IWorldReader) world);
        return !fromState.getFluid().isEquivalentTo(toState.getFluid())
            && !toState.isSource() && h > hOther + dec;
    }
}
