package dev.mattidragon.polydexbridge.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record BridgeRecipe(Identifier id,
                           String group,
                           List<BridgeCategory> categories,
                           ItemStack typeIcon,
                           ItemStack entryIcon,
                           @Nullable Component texture,
                           List<List<BridgeStack>> ingredients,
                           List<Slot> icons,
                           List<Slot> inputs,
                           List<Slot> outputs) {
    public static final StreamCodec<RegistryFriendlyByteBuf, BridgeRecipe> CODEC = StreamCodec.of((buf, recipe) -> {
        Identifier.STREAM_CODEC.encode(buf, recipe.id);
        ByteBufCodecs.STRING_UTF8.encode(buf, recipe.group);
        BridgeCategory.CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.categories);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.typeIcon);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.entryIcon);
        ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC).encode(buf, Optional.ofNullable(recipe.texture));
        BridgeStack.CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()).encode(buf, recipe.ingredients);
        Slot.CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.icons);
        Slot.CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.inputs);
        Slot.CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.outputs);
    }, buf -> new BridgeRecipe(
            Identifier.STREAM_CODEC.decode(buf),
            ByteBufCodecs.STRING_UTF8.decode(buf),
            BridgeCategory.CODEC.apply(ByteBufCodecs.list()).decode(buf),
            ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
            ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC).decode(buf).orElse(null),
            BridgeStack.CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()).decode(buf),
            Slot.CODEC.apply(ByteBufCodecs.list()).decode(buf),
            Slot.CODEC.apply(ByteBufCodecs.list()).decode(buf),
            Slot.CODEC.apply(ByteBufCodecs.list()).decode(buf)
    ));
}
