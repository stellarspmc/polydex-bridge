package dev.mattidragon.polydexbridge.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.List;

public record Slot(int x, int y, List<BridgeStack> stacks) {
    public static final StreamCodec<RegistryFriendlyByteBuf, Slot> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Slot::x,
            ByteBufCodecs.VAR_INT, Slot::y,
            BridgeStack.CODEC.apply(ByteBufCodecs.list()), Slot::stacks,
            Slot::new
    );

    public Slot(int x, int y, BridgeStack... stacks) {
        this(x, y, Arrays.asList(stacks));
    }
}
