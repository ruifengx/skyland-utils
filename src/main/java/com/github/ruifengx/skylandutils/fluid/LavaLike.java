package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.util.FluidUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.StateContainer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

public abstract class LavaLike extends ForgeFlowingFluid {
    public final LavaLikeDamage asDamageSource = new LavaLikeDamage(this);

    public static class LavaLikeDamage extends DamageSource {
        private final LavaLike sourceFluid;

        public LavaLikeDamage(LavaLike sourceFluid) {
            super("lava_like");
            this.setFireDamage();
            this.sourceFluid = sourceFluid;
        }

        @Override
        public @NotNull ITextComponent getDeathMessage(@NotNull LivingEntity entityLivingBaseIn) {
            LivingEntity livingentity = entityLivingBaseIn.getAttackingEntity();
            final String msgIdRaw = "death.attack.lava_like";
            final String msgIdPlayer = msgIdRaw + ".player";
            final ResourceLocation fluidRegName = this.sourceFluid.getRegistryName();
            final ITextComponent fluidName = new TranslationTextComponent(fluidRegName == null
                ? "skyland-utils.unknown_lavalike_fluid"
                : Util.makeTranslationKey("fluid", this.sourceFluid.getRegistryName()));
            if (livingentity != null) {
                return new TranslationTextComponent(msgIdPlayer,
                    entityLivingBaseIn.getDisplayName(),
                    fluidName, livingentity.getDisplayName());
            } else {
                return new TranslationTextComponent(msgIdRaw, entityLivingBaseIn.getDisplayName(), fluidName);
            }
        }
    }

    public LavaLike(Properties properties) {
        super(properties);
    }

    public static boolean isLavaLike(Fluid fluid) {
        return fluid instanceof LavaLike || Fluids.LAVA.isEquivalentTo(fluid);
    }

    @Override
    protected int getLevelDecreasePerBlock(IWorldReader worldIn) {
        return worldIn.getDimensionType().isUltrawarm() ? 1 : 2;
    }

    @Override
    public int getTickRate(IWorldReader worldIn) {
        return worldIn.getDimensionType().isUltrawarm() ? 10 : 30;
    }

    @Override
    protected boolean canDisplace(FluidState toState, IBlockReader world, BlockPos toPos, Fluid fromFluid, Direction direction) {
        return toState.getActualHeight(world, toPos) >= 0.44444445F && WaterLike.isWaterLike(fromFluid)
            || FluidUtil.canDisplace(toState, world, toPos, direction);
    }

    private void triggerEffects(IWorld world, BlockPos pos) {
        world.playEvent(1501, pos, 0);
    }

    @Override
    protected void flowInto(@NotNull IWorld worldIn, @NotNull BlockPos pos,
                            @NotNull BlockState blockStateIn, @NotNull Direction direction,
                            @NotNull FluidState fluidStateIn) {
        if (direction == Direction.DOWN) {
            Fluid otherFluid = worldIn.getFluidState(pos).getFluid();
            if (WaterLike.isWaterLike(otherFluid)) {
                // TODO: determine how WaterLike fluids are to vaporize/solidify.
                // WaterLike waterFluid = (WaterLike) otherFluid;
                if (blockStateIn.getBlock() instanceof FlowingFluidBlock) {
                    worldIn.setBlockState(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(
                        worldIn, pos, pos, Blocks.STONE.getDefaultState()), 3);
                }

                this.triggerEffects(worldIn, pos);
                return;
            }
        }

        super.flowInto(worldIn, pos, blockStateIn, direction, fluidStateIn);
    }

    public static class Flowing extends LavaLike {
        public Flowing(Properties properties) {
            super(properties);
            setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
        }

        protected void fillStateContainer(@NotNull StateContainer.Builder<Fluid, FluidState> builder) {
            super.fillStateContainer(builder);
            builder.add(LEVEL_1_8);
        }

        public int getLevel(FluidState state) { return state.get(LEVEL_1_8); }
        public boolean isSource(@NotNull FluidState state) { return false; }
    }

    public static class Source extends LavaLike {
        public Source(Properties properties) { super(properties); }
        public int getLevel(@NotNull FluidState state) { return 8; }
        public boolean isSource(@NotNull FluidState state) { return true; }
    }
}
