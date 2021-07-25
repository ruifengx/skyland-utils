package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.SkylandUtils;
import com.github.ruifengx.skylandutils.util.BlockUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Interaction implements IRecipe<IInventory> {
    private final ResourceLocation id;
    public static final IRecipeType<Interaction> type
        = IRecipeType.register(SkylandUtils.MODID + ":fluid_interation");

    public static List<Interaction> getAll(World world) {
        return world.getRecipeManager().getAllRecipesFor(type);
    }

    public static class Serializer
        extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<Interaction> {
        public static final Serializer INSTANCE;

        static {
            INSTANCE = new Serializer();
            INSTANCE.setRegistryName(SkylandUtils.MODID, "fluid_interation");
        }

        @Override public Interaction fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient consume = Ingredient.fluidFromId(json.get("consume").getAsString());
            List<Ingredient> env = new ArrayList<>();
            for (JsonElement envEntry : json.getAsJsonArray("env"))
                env.add(Ingredient.fromElement(envEntry));
            JsonElement jsonGenerate = json.get("generate");
            if (!jsonGenerate.isJsonPrimitive())
                throw new JsonSyntaxException("Fluid interaction requires a 'generate' field.");
            Block generate = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonGenerate.getAsString()));
            if (generate == null) throw new JsonSyntaxException("Invalid 'generate' field.");
            Ingredient below = !json.has("below") ? Ingredient.WHATEVER
                : Ingredient.blockFromId(json.get("below").getAsString());
            return new Interaction(recipeId, consume, env, generate, below);
        }

        @Nullable @Override public Interaction fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient consume = Ingredient.FlatStructure.readBuffer(buffer).recover();
            List<Ingredient> env = new ArrayList<>();
            int n = buffer.readVarInt();
            for (int i = 0; i < n; ++i)
                env.add(Ingredient.FlatStructure.readBuffer(buffer).recover());
            Block generate = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(buffer.readUtf()));
            Ingredient below = Ingredient.FlatStructure.readBuffer(buffer).recover();
            return new Interaction(recipeId, consume, env, generate, below);
        }

        @Override public void toNetwork(PacketBuffer buffer, Interaction recipe) {
            recipe.sourceFluid.make_flat().writeBuffer(buffer);
            buffer.writeVarInt(recipe.target.size());
            for (Ingredient target : recipe.target) target.make_flat().writeBuffer(buffer);
            buffer.writeUtf(recipe.blockToGenerate.getRegistryName().toString());
            recipe.blockExpectedBelow.make_flat().writeBuffer(buffer);
        }
    }

    @Override public boolean matches(IInventory inv, World worldIn) { return false; }
    @Override public ItemStack assemble(IInventory inv) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem() { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return this.id; }
    @Override public IRecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }
    @Override public IRecipeType<?> getType() { return type; }
    @Override public boolean isSpecial() { return true; }

    private final Ingredient sourceFluid;
    private final List<Ingredient> target;
    private final Block blockToGenerate;
    private final Ingredient blockExpectedBelow;

    public Interaction(ResourceLocation id, Ingredient sourceFluid, List<Ingredient> target,
                       Block blockToGenerate, Ingredient blockExpectedBelow) {
        this.id = id;
        this.sourceFluid = sourceFluid;
        this.target = target;
        this.blockToGenerate = blockToGenerate;
        this.blockExpectedBelow = blockExpectedBelow;
        if (target.size() > 5)
            SkylandUtils.LOGGER.warn(MessageFormat.format(
                "Fluid interation ''{}'' has {} env (more than 5), making it impossible to trigger.",
                id.toString(), target.size()));
    }

    static final Supplier<Fluid> INVALID_FLUID = () -> { throw new RuntimeException("unreachable"); };
    static final Supplier<Block> INVALID_BLOCK = () -> { throw new RuntimeException("unreachable"); };

    private List<Ingredient> targetFluids;
    private List<Ingredient> targetBlocks;
    public Ingredient getSourceFluid() { return this.sourceFluid; }
    public List<Ingredient> getTargetFluids() {
        if (this.targetFluids == null)
            this.targetFluids = this.target.stream()
                .filter(ingredient -> ingredient instanceof Ingredient.Fluid
                    || ingredient instanceof Ingredient.FluidTag)
                .collect(Collectors.toList());
        return this.targetFluids;
    }
    public List<Ingredient> getTargetBlocks() {
        if (this.targetBlocks == null)
            this.targetBlocks = this.target.stream()
                .filter(ingredient -> ingredient instanceof Ingredient.Block
                    || ingredient instanceof Ingredient.BlockTag)
                .collect(Collectors.toList());
        return this.targetBlocks;
    }
    public Ingredient getTargetFluid(int n) { return this.getTargetFluids().get(n); }
    public Ingredient getTargetBlock(int n) { return this.getTargetBlocks().get(n); }
    public Ingredient getBlockExpectedBelow() { return this.blockExpectedBelow; }
    public Block getBlockToGenerate() { return this.blockToGenerate; }
    public boolean consumesSource() {
        return this.sourceFluid.getAsFluids().stream().allMatch(fluid ->
            ((FlowingFluid) fluid.getFluid()).getSource() == fluid.getFluid());
    }
    public boolean mightConsumeSource() {
        return this.sourceFluid.getAsFluids().stream().anyMatch(fluid ->
            ((FlowingFluid) fluid.getFluid()).getSource() == fluid.getFluid());
    }

    public List<FluidStack> getDisplaySourceFluid() { return this.sourceFluid.getAsFluids(); }
    @Nullable List<List<FluidStack>> displayFluidInputs = null;
    public List<List<FluidStack>> getDisplayFluidInputs() {
        if (this.displayFluidInputs == null) {
            this.displayFluidInputs = new ArrayList<>();
            this.displayFluidInputs.add(this.sourceFluid.getAsFluids());
            this.displayFluidInputs.addAll(getDisplayEnvFluids());
        }
        return this.displayFluidInputs;
    }
    @Nullable List<List<ItemStack>> displayBlockInputs = null;
    public List<List<ItemStack>> getDisplayBlockInputs() {
        if (this.displayBlockInputs == null) {
            if (this.blockExpectedBelow instanceof Ingredient.Whatever)
                this.displayBlockInputs = getDisplayEnvBlocks();
            else {
                this.displayBlockInputs = new ArrayList<>();
                this.displayBlockInputs.add(this.blockExpectedBelow.getAsBlocks());
                this.displayBlockInputs.addAll(getDisplayEnvBlocks());
            }
        }
        return this.displayBlockInputs;
    }
    @Nullable List<List<FluidStack>> displayEnvFluids = null;
    @Nullable List<List<ItemStack>> displayEnvBlocks = null;
    private void updateDisplayEnv() {
        this.displayEnvFluids = new ArrayList<>();
        this.displayEnvBlocks = new ArrayList<>();
        for (Ingredient target : this.target) {
            if (target instanceof Ingredient.Whatever) continue;
            if (target instanceof Ingredient.Fluid || target instanceof Ingredient.FluidTag)
                this.displayEnvFluids.add(target.getAsFluids());
            else
                this.displayEnvBlocks.add(target.getAsBlocks());
        }
    }
    public List<List<FluidStack>> getDisplayEnvFluids() {
        if (this.displayEnvFluids == null) updateDisplayEnv();
        return this.displayEnvFluids;
    }
    public List<List<ItemStack>> getDisplayEnvBlocks() {
        if (this.displayEnvBlocks == null) updateDisplayEnv();
        return this.displayEnvBlocks;
    }
    public List<ItemStack> getDisplayBlockBelow() {
        if (this.blockExpectedBelow instanceof Ingredient.Whatever) return new ArrayList<>();
        return this.blockExpectedBelow.getAsBlocks();
    }
    public List<ItemStack> getDisplayOutput() {
        List<ItemStack> output = new ArrayList<>();
        output.add(new ItemStack(BlockUtil.forceAsItem(this.blockToGenerate)));
        return output;
    }

    public boolean matchWorldAt(World worldIn, BlockPos pos, Fluid thisFluid, UponGeneration uponGeneration) {
        if (this.sourceFluid.match(() -> thisFluid, INVALID_BLOCK)) {
            boolean belowFlag = this.blockExpectedBelow.match(INVALID_FLUID,
                () -> worldIn.getBlockState(pos.below()).getBlock());
            int directionFlag = 0;
            target_loop:
            for (Ingredient target : this.target) {
                for (Direction direction : Direction.values()) {
                    final int bit = 1 << direction.get3DDataValue();
                    if ((directionFlag & bit) == 0) {
                        if (direction != Direction.DOWN) {
                            BlockPos neighbourPos = pos.relative(direction);
                            if (belowFlag && target.match(
                                () -> worldIn.getFluidState(neighbourPos).getType(),
                                () -> worldIn.getBlockState(neighbourPos).getBlock())) {
                                directionFlag |= bit;
                                continue target_loop;
                            }
                        }
                    }
                }
                return false;
            }
            uponGeneration.uponGeneration(this.blockToGenerate);
            return true;
        }
        return false;
    }

    public interface UponGeneration {
        void uponGeneration(Block blockToGenerate);
    }
}
