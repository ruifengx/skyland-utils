package com.github.ruifengx.skylandutils.mixin;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DispenserBlock.class)
public interface DispenseBehaviourAccessor {
    @Accessor("DISPENSE_BEHAVIOR_REGISTRY")
    static Map<Item, IDispenseItemBehavior> getDispenseBehaviourRegistry() {
        throw new RuntimeException("cannot access DISPENSE_BEHAVIOUR_REGISTRY");
    }
}
