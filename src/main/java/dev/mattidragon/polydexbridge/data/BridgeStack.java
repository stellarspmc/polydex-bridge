package dev.mattidragon.polydexbridge.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record BridgeStack(ItemStack stack, float chance) {
    public static final StreamCodec<RegistryFriendlyByteBuf, BridgeStack> CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, BridgeStack::stack,
            ByteBufCodecs.FLOAT, BridgeStack::chance,
            BridgeStack::new);

    public BridgeStack(ItemStack stack) {
        this(stack, 1);
    }
}
