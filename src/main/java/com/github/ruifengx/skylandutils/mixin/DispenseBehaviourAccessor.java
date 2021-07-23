package com.github.ruifengx.skylandutils.mixin;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DispenserBlock.class)
public interface DispenseBehaviourAccessor {
    @Accessor("DISPENSER_REGISTRY")
    static Map<Item, IDispenseItemBehavior> getDispenserRegistry() {
        throw new RuntimeException("cannot access DISPENSER_REGISTRY");
    }
}
