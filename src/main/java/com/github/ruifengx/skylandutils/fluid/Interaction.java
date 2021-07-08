package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.block.SkylandBlocks;
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
        FluidOrBlock.exact(TinkerFluids.blazingBlood.getFlowing()),
        FluidOrBlock.from(TinkerFluids.blood.get()),
        Blocks.NETHERRACK, FluidOrBlock.whatever()
    );
    public static final Interaction blazingBlockGen = new Interaction(
        FluidOrBlock.exact(TinkerFluids.blazingBlood.getStill()),
        FluidOrBlock.from(TinkerFluids.blood.get()),
        SkylandBlocks.BLAZE_BLOCK.get(), FluidOrBlock.whatever()
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
