package com.github.ruifengx.skylandutils.fluid;

import com.github.ruifengx.skylandutils.util.BlockUtil;
import com.github.ruifengx.skylandutils.util.TranslationUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface Ingredient {
    Ingredient WHATEVER = new Whatever();

    static Ingredient fromId(IngredientCreator plain, IngredientCreator tag, String name) {
        ResourceLocation loc = name.startsWith("#")
            ? ResourceLocation.tryParse(name.substring(1))
            : ResourceLocation.tryParse(name);
        if (loc == null) throw new JsonSyntaxException(MessageFormat.format("Invalid id: ''{}''", name));
        return name.startsWith("#") ? tag.create(loc) : plain.create(loc);
    }
    static Ingredient fluidFromId(String name) { return fromId(Fluid::new, FluidTag::new, name); }
    static Ingredient blockFromId(String name) { return fromId(Block::new, BlockTag::new, name); }

    static Ingredient fromElement(JsonElement json) {
        if (json.isJsonObject())
            return Ingredient.fromObject(json.getAsJsonObject());
        else if (json.isJsonPrimitive())
            return Ingredient.fluidFromId(json.getAsString());
        else throw new JsonSyntaxException("Invalid JSON for fluid-interaction ingredients");
    }

    static Ingredient fromObject(JsonObject obj) {
        if (obj.has("fluid"))
            return fluidFromId(obj.get("fluid").getAsString());
        else if (obj.has("block"))
            return blockFromId(obj.get("block").getAsString());
        else throw new JsonSyntaxException("Field 'fluid' or 'block' required for ingredients");
    }

    interface IngredientCreator {
        Ingredient create(ResourceLocation loc);
    }

    class FlatStructure {
        public boolean isWhatever;
        public boolean isFluid;
        public boolean isTag;
        public String asString;
        public static FlatStructure whatever() {
            return new FlatStructure(true, false, false, "WHATEVER");
        }
        public static FlatStructure normal(boolean isFluid, boolean isTag, String asString) {
            return new FlatStructure(false, isFluid, isTag, asString);
        }
        private FlatStructure(boolean isWhatever, boolean isFluid, boolean isTag, String asString) {
            this.isWhatever = isWhatever;
            this.isFluid = isFluid;
            this.isTag = isTag;
            this.asString = asString;
        }

        public static FlatStructure readBuffer(PacketBuffer buffer) {
            boolean isWhatever = buffer.readBoolean();
            if (isWhatever) return whatever();
            return normal(buffer.readBoolean(), buffer.readBoolean(), buffer.readUtf());
        }

        public void writeBuffer(PacketBuffer buffer) {
            if (this.isWhatever) buffer.writeBoolean(this.isWhatever);
            else {
                buffer.writeBoolean(this.isWhatever);
                buffer.writeBoolean(this.isFluid);
                buffer.writeBoolean(this.isTag);
                buffer.writeUtf(this.asString);
            }
        }

        public Ingredient recover() {
            if (this.isWhatever) return WHATEVER;
            ResourceLocation loc = new ResourceLocation(this.asString);
            if (this.isFluid) return this.isTag ? new FluidTag(loc) : new Fluid(loc);
            else return this.isTag ? new BlockTag(loc) : new Block(loc);
        }
    }

    boolean match(Supplier<net.minecraft.fluid.Fluid> fluid, Supplier<net.minecraft.block.Block> block);
    default List<FluidStack> getAsFluids() { throw new RuntimeException("cannot get as fluids"); }
    default List<ItemStack> getAsBlocks() { throw new RuntimeException("cannot get as blocks"); }
    FlatStructure make_flat();
    default @Nullable ITextComponent getDisplayName() { return null; }

    static <T> ITag.INamedTag<T> getTagByName(List<? extends ITag.INamedTag<T>> tags, ResourceLocation name) {
        Optional<? extends ITag.INamedTag<T>> optTag = tags.stream()
            .filter((tag) -> tag.getName().equals(name)).findFirst();
        return optTag.orElseThrow(() -> new JsonSyntaxException(
            MessageFormat.format("Invalid tag name: ''{0}''", name.toString())));
    }

    class Whatever implements Ingredient {
        @Override
        public boolean match(Supplier<net.minecraft.fluid.Fluid> fluid,
                             Supplier<net.minecraft.block.Block> block) { return true; }
        @Override public FlatStructure make_flat() { return FlatStructure.whatever(); }
    }

    class Fluid implements Ingredient {
        private final net.minecraft.fluid.Fluid fluid;
        public Fluid(net.minecraft.fluid.Fluid fluid) { this.fluid = fluid; }
        public Fluid(ResourceLocation fluid) { this(ForgeRegistries.FLUIDS.getValue(fluid)); }
        @Override public FlatStructure make_flat() {
            return FlatStructure.normal(true, false, fluid.getRegistryName().toString());
        }
        private List<FluidStack> fluidStacks = null;
        @Override public List<FluidStack> getAsFluids() {
            if (this.fluidStacks == null) {
                this.fluidStacks = new ArrayList<>();
                this.fluidStacks.add(new FluidStack(this.fluid, 1000));
            }
            return this.fluidStacks;
        }
        @Override public boolean match(Supplier<net.minecraft.fluid.Fluid> fluid,
                                       Supplier<net.minecraft.block.Block> block) {
            return fluid.get() == this.fluid;
        }
    }

    class FluidTag implements Ingredient {
        private final ITag.INamedTag<net.minecraft.fluid.Fluid> fluidTag;
        public FluidTag(ITag.INamedTag<net.minecraft.fluid.Fluid> fluidTag) { this.fluidTag = fluidTag; }
        public FluidTag(ResourceLocation fluidTag) {
            this(Ingredient.getTagByName(FluidTags.getWrappers(), fluidTag));
        }
        @Override public FlatStructure make_flat() {
            return FlatStructure.normal(true, true, fluidTag.getName().toString());
        }
        private List<FluidStack> fluidStacks = null;
        @Override public List<FluidStack> getAsFluids() {
            if (this.fluidStacks == null) {
                this.fluidStacks = new ArrayList<>();
                for (net.minecraft.fluid.Fluid fluid : this.fluidTag.getValues())
                    this.fluidStacks.add(new FluidStack(fluid, 1000));
            }
            return this.fluidStacks;
        }
        @Override public boolean match(Supplier<net.minecraft.fluid.Fluid> fluid,
                                       Supplier<net.minecraft.block.Block> block) {
            return fluid.get().is(this.fluidTag);
        }
        @Override
        public ITextComponent getDisplayName() {
            return TranslationUtil.getTagDescription("fluid", this.fluidTag.getName());
        }
    }

    class Block implements Ingredient {
        private final net.minecraft.block.Block block;
        public Block(net.minecraft.block.Block block) { this.block = block; }
        public Block(ResourceLocation block) { this(ForgeRegistries.BLOCKS.getValue(block)); }
        @Override public FlatStructure make_flat() {
            return FlatStructure.normal(false, false, block.getRegistryName().toString());
        }
        private List<ItemStack> blocks = null;
        @Override public List<ItemStack> getAsBlocks() {
            if (this.blocks == null) {
                this.blocks = new ArrayList<>();
                this.blocks.add(new ItemStack(BlockUtil.forceAsItem(this.block)));
            }
            return this.blocks;
        }
        @Override public boolean match(Supplier<net.minecraft.fluid.Fluid> fluid,
                                       Supplier<net.minecraft.block.Block> block) {
            return block.get() == this.block;
        }
    }

    class BlockTag implements Ingredient {
        private final ITag.INamedTag<net.minecraft.block.Block> blockTag;
        public BlockTag(ITag.INamedTag<net.minecraft.block.Block> blockTag) { this.blockTag = blockTag; }
        public BlockTag(ResourceLocation blockTag) {
            this(Ingredient.getTagByName(BlockTags.getWrappers(), blockTag));
        }
        @Override public FlatStructure make_flat() {
            return FlatStructure.normal(false, true, blockTag.getName().toString());
        }
        private List<ItemStack> blocks = null;
        @Override public List<ItemStack> getAsBlocks() {
            if (this.blocks == null) {
                this.blocks = new ArrayList<>();
                for (net.minecraft.block.Block block : this.blockTag.getValues())
                    this.blocks.add(new ItemStack(BlockUtil.forceAsItem(block)));
            }
            return this.blocks;
        }
        @Override
        public ITextComponent getDisplayName() {
            return TranslationUtil.getTagDescription("block", this.blockTag.getName());
        }
        @Override public boolean match(Supplier<net.minecraft.fluid.Fluid> fluid,
                                       Supplier<net.minecraft.block.Block> block) {
            return block.get().is(this.blockTag);
        }
    }
}
