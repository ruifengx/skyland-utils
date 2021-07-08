package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.util.FluidOrBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.fluids.TinkerFluids;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Interaction {
    public static final List<Interaction> ALL = new ArrayList<>();

    public static final Interaction netherrackGen = new Interaction(
        TinkerFluids.blazingBlood.get(), TinkerFluids.blood.get(),
        Blocks.NETHERRACK, null
    );

    private final Predicate<Fluid> sourceFluid;
    private final FluidOrBlock target;
    private final Block blockToGenerate;

    private final Predicate<BlockState> blockExpectedBelow;

    public Interaction(Predicate<Fluid> sourceFluid, FluidOrBlock target,
                       Block blockToGenerate, Predicate<BlockState> blockExpectedBelow) {
        this.sourceFluid = sourceFluid;
        this.target = target;
        this.blockToGenerate = blockToGenerate;
        this.blockExpectedBelow = blockExpectedBelow;
        Interaction.ALL.add(this);
    }

    public Interaction(Fluid sourceFluid, Fluid targetFluid, Block blockToGenerate,
                       @Nullable Block blockExpectedBelow) {
        this(fluid -> fluid.isEquivalentTo(sourceFluid),
            FluidOrBlock.from(targetFluid), blockToGenerate,
            block -> blockExpectedBelow == null || block.matchesBlock(blockExpectedBelow));
    }

    public Interaction(Fluid sourceFluid, Block targetBlock, Block blockToGenerate,
                       @Nullable Block blockExpectedBelow) {
        this(fluid -> fluid.isEquivalentTo(sourceFluid),
            FluidOrBlock.from(targetBlock), blockToGenerate,
            block -> blockExpectedBelow == null || block.matchesBlock(blockExpectedBelow));
    }

    public boolean matchWorldAt(World worldIn, BlockPos pos, Fluid thisFluid, UponGeneration uponGeneration) {
        if (this.sourceFluid.test(thisFluid)) {
            boolean belowFlag = this.blockExpectedBelow.test(worldIn.getBlockState(pos.down()));
            for (Direction direction : Direction.values()) {
                if (direction != Direction.DOWN) {
                    BlockPos neighbourPos = pos.offset(direction);
                    if (belowFlag && this.target.check(
                        worldIn.getFluidState(neighbourPos),
                        worldIn.getBlockState(neighbourPos))) {
                        uponGeneration.uponGeneration(this.blockToGenerate);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public interface UponGeneration {
        void uponGeneration(Block blockToGenerate);
    }
}
