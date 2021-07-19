package com.github.ruifengx.skylandutils.util;

import com.github.ruifengx.skylandutils.item.AllItemTags;
import com.github.ruifengx.skylandutils.mixin.DispenseBehaviourAccessor;
import com.github.ruifengx.skylandutils.mixin.FlowingFluidAccessor;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.FluidBuilder;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Supplier;

public final class FluidUtil {
    private static final IDispenseItemBehavior dispenseBucketBehaviour
        = DispenseBehaviourAccessor.getDispenseBehaviourRegistry().get(Items.WATER_BUCKET);

    public static boolean hasDispenseBehaviour(Item item) {
        return DispenseBehaviourAccessor.getDispenseBehaviourRegistry().containsKey(item);
    }

    public static void registerAllBuckets() {
        for (Item bucket : AllItemTags.BUCKETS.getAllElements())
            if (!FluidUtil.hasDispenseBehaviour(bucket))
                DispenserBlock.registerDispenseBehavior(bucket, dispenseBucketBehaviour);
    }

    public static int getLevelDecreasePerBlock(FluidState fluidState, IWorldReader world) {
        if (fluidState.getFluid() instanceof FlowingFluid) {
            return ((FlowingFluidAccessor) fluidState.getFluid()).levelDecreasePerBlock(world);
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
