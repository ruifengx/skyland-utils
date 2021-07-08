package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.util.FluidUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.tconstruct.fluids.fluids.SlimeFluid;

@Mixin(SlimeFluid.class)
public abstract class MixinSlimeFluid extends ForgeFlowingFluid {
    protected MixinSlimeFluid(Properties properties) { super(properties); }

    @Override
    protected boolean canDisplace(FluidState toState, IBlockReader world, BlockPos toPos,
                                  Fluid fromFluid, Direction direction) {
        return direction == Direction.DOWN && !isEquivalentTo(fromFluid)
            || FluidUtil.canDisplace(toState, world, toPos, direction);
    }
}
