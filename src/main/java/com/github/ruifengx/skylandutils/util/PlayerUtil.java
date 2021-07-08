package com.github.ruifengx.skylandutils.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class PlayerUtil {
    // Avoid compiler optimizing by assuming 'MixinEntity' is not an 'Entity'
    public static boolean isPlayerEntity(Entity entity) {
        return entity instanceof PlayerEntity;
    }
}
