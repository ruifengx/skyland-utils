package com.github.ruifengx.skylandutils.item;

import com.github.ruifengx.skylandutils.SkylandUtils;
import com.github.ruifengx.skylandutils.block.SkylandBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class SkylandItemGroups {
    public static final ItemGroup SKYLAND_GROUP = new SkylandGroup(SkylandUtils.MODID,
        () -> new ItemStack(SkylandBlocks.BLAZE_BLOCK.get().asItem()));

    public static class SkylandGroup extends ItemGroup {
        @NotNull private final Supplier<ItemStack> iconSupplier;

        public SkylandGroup(@NotNull final String name, @NotNull final Supplier<ItemStack> iconSupplier) {
            super(name);
            this.iconSupplier = iconSupplier;
        }

        @Override @NotNull public ItemStack makeIcon() { return iconSupplier.get(); }
    }
}
