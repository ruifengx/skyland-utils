package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.SkylandUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Interaction implements IRecipe<IInventory> {
    private final ResourceLocation id;
    public static final IRecipeType<Interaction> type
        = IRecipeType.register(SkylandUtils.MODID + ":fluid-interation");

    public static List<Interaction> getAll(World world) {
        return world.getRecipeManager().getRecipesForType(type);
    }

    public static class Serializer
        extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<Interaction> {
        public static final Serializer INSTANCE;

        static {
            INSTANCE = new Serializer();
            INSTANCE.setRegistryName("skyland-utils", "fluid-interation");
        }

        @Override public Interaction read(ResourceLocation recipeId, JsonObject json) {
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
                : Ingredient.fromElement(json.get("below"));
            return new Interaction(recipeId, consume, env, generate, below);
        }

        @Nullable @Override public Interaction read(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient consume = Ingredient.FlatStructure.readBuffer(buffer).recover();
            List<Ingredient> env = new ArrayList<>();
            int n = buffer.readVarInt();
            for (int i = 0; i < n; ++i)
                env.add(Ingredient.FlatStructure.readBuffer(buffer).recover());
            Block generate = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(buffer.readString()));
            Ingredient below = Ingredient.FlatStructure.readBuffer(buffer).recover();
            return new Interaction(recipeId, consume, env, generate, below);
        }

        @Override public void write(PacketBuffer buffer, Interaction recipe) {
            recipe.sourceFluid.make_flat().writeBuffer(buffer);
            buffer.writeVarInt(recipe.target.size());
            for (Ingredient target : recipe.target) target.make_flat().writeBuffer(buffer);
            buffer.writeString(recipe.blockToGenerate.getRegistryName().toString());
            recipe.blockExpectedBelow.make_flat().writeBuffer(buffer);
        }
    }

    @Override public boolean matches(IInventory inv, World worldIn) { return false; }
    @Override public ItemStack getCraftingResult(IInventory inv) { return ItemStack.EMPTY; }
    @Override public boolean canFit(int width, int height) { return true; }
    @Override public ItemStack getRecipeOutput() { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return this.id; }
    @Override public IRecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }
    @Override public IRecipeType<?> getType() { return type; }
    @Override public boolean isDynamic() { return true; }

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

    public boolean matchWorldAt(World worldIn, BlockPos pos, Fluid thisFluid, UponGeneration uponGeneration) {
        if (this.sourceFluid.match(() -> thisFluid, INVALID_BLOCK)) {
            boolean belowFlag = this.blockExpectedBelow.match(INVALID_FLUID,
                () -> worldIn.getBlockState(pos.down()).getBlock());
            int directionFlag = 0;
            target_loop:
            for (Ingredient target : this.target) {
                for (Direction direction : Direction.values()) {
                    final int bit = 1 << direction.getIndex();
                    if ((directionFlag & bit) == 0) {
                        if (direction != Direction.DOWN) {
                            BlockPos neighbourPos = pos.offset(direction);
                            if (belowFlag && target.match(
                                () -> worldIn.getFluidState(neighbourPos).getFluid(),
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
