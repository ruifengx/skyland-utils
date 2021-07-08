package com.github.ruifengx.skylandutils.mixin;

import com.github.ruifengx.skylandutils.util.FluidUtil;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import slimeknights.mantle.registration.FluidBuilder;

import java.util.function.Supplier;

@Mixin(FluidBuilder.class)
public class MixinFluidBuilder implements FluidUtil.FluidBuilderAccessor {
    @Shadow(remap = false)
    private Supplier<? extends Item> bucket;

    @Override
    public Supplier<? extends Item> getBucketItem() {
        return this.bucket;
    }
}
