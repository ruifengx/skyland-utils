package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.SkylandUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

public class ScaldingDamage extends DamageSource {
    private final Fluid sourceFluid;

    public ScaldingDamage(Fluid sourceFluid) {
        super("scalding");
        this.setIsFire();
        this.sourceFluid = sourceFluid;
    }

    @Override
    public @NotNull ITextComponent getLocalizedDeathMessage(@NotNull LivingEntity entityLivingBaseIn) {
        LivingEntity livingentity = entityLivingBaseIn.getLastHurtByMob();
        final String msgIdRaw = "death.attack.scalding";
        final String msgIdPlayer = msgIdRaw + ".player";
        final ResourceLocation fluidRegName = this.sourceFluid.getRegistryName();
        final ITextComponent fluidName = new TranslationTextComponent(fluidRegName == null
            ? SkylandUtils.MODID + ".unknown_scalding_fluid"
            : Util.makeDescriptionId("fluid", this.sourceFluid.getRegistryName()));
        if (livingentity != null) {
            return new TranslationTextComponent(msgIdPlayer,
                entityLivingBaseIn.getDisplayName(),
                fluidName, livingentity.getDisplayName());
        } else {
            return new TranslationTextComponent(msgIdRaw, entityLivingBaseIn.getDisplayName(), fluidName);
        }
    }
}
