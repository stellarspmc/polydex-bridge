package dev.mattidragon.polydexbridge.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record BridgeCategory(Identifier id, Component name) {
    public static final StreamCodec<RegistryFriendlyByteBuf, BridgeCategory> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, BridgeCategory::id,
            ComponentSerialization.STREAM_CODEC, BridgeCategory::name,
            BridgeCategory::new
    );

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BridgeCategory that && this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
