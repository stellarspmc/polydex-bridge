package dev.mattidragon.polydexbridge;

import dev.mattidragon.polydexbridge.data.BridgeRecipe;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record PolydexRecipesPacket(List<BridgeRecipe> recipes) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PolydexRecipesPacket> CODEC
            = StreamCodec.composite(BridgeRecipe.CODEC.apply(ByteBufCodecs.list()), PolydexRecipesPacket::recipes, PolydexRecipesPacket::new);
    public static final Type<PolydexRecipesPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(PolydexBridge.MOD_ID, "sync_recipes"));
    
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
    }
    
    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
