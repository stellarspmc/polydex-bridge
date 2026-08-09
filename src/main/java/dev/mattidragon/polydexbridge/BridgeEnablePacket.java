package dev.mattidragon.polydexbridge;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public enum BridgeEnablePacket implements CustomPacketPayload {
    INSTANCE;
    
    public static final Type<BridgeEnablePacket> ID = new Type<>(Identifier.fromNamespaceAndPath(PolydexBridge.MOD_ID, "enable"));
    public static final StreamCodec<FriendlyByteBuf, BridgeEnablePacket> CODEC = StreamCodec.unit(INSTANCE);

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ID, CODEC);
    }
    
    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
