package com.github.ruifengx.skylandutils.mixin;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FlowingFluid.class)
public interface FlowingFluidAccessor {
    @Invoker("getDropOff")
    int dropOff(IWorldReader worldIn);
}
