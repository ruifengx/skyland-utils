package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.util.IFlowingFluidAccessor;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid implements IFlowingFluidAccessor {
    @Shadow protected abstract int getLevelDecreasePerBlock(IWorldReader worldIn);

    @Override
    public int levelDecreasePerBlock(IWorldReader worldIn) {
        return this.getLevelDecreasePerBlock(worldIn);
    }
}
