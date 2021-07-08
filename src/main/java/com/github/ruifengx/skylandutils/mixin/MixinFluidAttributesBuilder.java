package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.util.IFluidAttributesExtra;
import net.minecraftforge.fluids.FluidAttributes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidAttributes.Builder.class)
public class MixinFluidAttributesBuilder implements IFluidAttributesExtra {
    private boolean isLavaLike;
    private boolean isWaterLike;

    @Override
    public FluidAttributes.Builder lavaLike() {
        this.isLavaLike = true;
        return (FluidAttributes.Builder) (Object) this;
    }

    @Override
    public boolean isLavaLike() { return this.isLavaLike; }

    @Override
    public FluidAttributes.Builder waterLike() {
        this.isWaterLike = true;
        return (FluidAttributes.Builder) (Object) this;
    }

    @Override
    public boolean isWaterLike() { return this.isWaterLike; }
}
