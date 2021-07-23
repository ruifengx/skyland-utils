package com.github.ruifengx.skylandutils.util;

import com.github.ruifengx.skylandutils.item.AllItemTags;
import com.github.ruifengx.skylandutils.mixin.DispenseBehaviourAccessor;
import com.github.ruifengx.skylandutils.mixin.FlowingFluidAccessor;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public final class FluidUtil {
    private static final IDispenseItemBehavior dispenseBucketBehaviour
        = DispenseBehaviourAccessor.getDispenserRegistry().get(Items.WATER_BUCKET);

    public static boolean hasDispenseBehaviour(Item item) {
        return DispenseBehaviourAccessor.getDispenserRegistry().containsKey(item);
    }

    public static void registerAllBuckets() {
        for (Item bucket : AllItemTags.BUCKETS.getValues())
            if (!FluidUtil.hasDispenseBehaviour(bucket))
                DispenserBlock.registerBehavior(bucket, dispenseBucketBehaviour);
    }

    public static int getLevelDecreasePerBlock(FluidState fluidState, IWorldReader world) {
        if (fluidState.getType() instanceof FlowingFluid) {
            return ((FlowingFluidAccessor) fluidState.getType()).slopeFindDistance(world);
        }
        return 1;
    }

    public static boolean canBeReplacedWith(FluidState toState, IBlockReader world,
                                            BlockPos toPos, Direction direction) {
        BlockPos fromPos = toPos.relative(direction.getOpposite());
        FluidState fromState = world.getFluidState(fromPos);
        double h = fromState.getAmount();
        double hOther = toState.getAmount();
        int dec = getLevelDecreasePerBlock(fromState, (IWorldReader) world);
        return !fromState.getType().isSame(toState.getType())
            && !toState.isSource() && h > hOther + dec;
    }
}
