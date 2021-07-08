package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.SkylandUtils;
import com.github.ruifengx.skylandutils.util.FluidUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

public abstract class WaterLike extends ForgeFlowingFluid {
    public WaterLike(Properties properties) {
        super(properties);
    }

    public static boolean isWaterLike(Fluid fluid) {
        return fluid instanceof WaterLike || Fluids.WATER.isEquivalentTo(fluid);
    }

    @Override
    protected boolean canDisplace(FluidState toState, IBlockReader world, BlockPos toPos,
                                  Fluid fromFluid, Direction direction) {
        return direction == Direction.DOWN && !isEquivalentTo(fromFluid)
            || FluidUtil.canDisplace(toState, world, toPos, direction);
    }

    public static class Flowing extends WaterLike {
        public Flowing(Properties properties) {
            super(properties);
            setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
        }

        protected void fillStateContainer(@NotNull StateContainer.Builder<Fluid, FluidState> builder) {
            super.fillStateContainer(builder);
            builder.add(LEVEL_1_8);
        }

        public int getLevel(FluidState state) { return state.get(LEVEL_1_8); }
        public boolean isSource(@NotNull FluidState state) { return false; }
    }

    public static class Source extends WaterLike {
        public Source(Properties properties) { super(properties); }
        public int getLevel(@NotNull FluidState state) { return 8; }
        public boolean isSource(@NotNull FluidState state) { return true; }
    }
}
