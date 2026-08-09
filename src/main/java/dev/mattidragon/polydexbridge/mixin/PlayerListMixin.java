package dev.mattidragon.polydexbridge.mixin;

import dev.mattidragon.polydexbridge.PolydexBridge;
import eu.pb4.polydex.api.v1.recipe.PolydexPageUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundUpdateRecipesPacket;<init>(Ljava/util/Map;Lnet/minecraft/world/item/crafting/SelectableRecipe$SingleInputSet;)V"))
    private void syncPolyRecipesOnJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        if (PolydexPageUtils.isReady()) {
            ServerPlayNetworking.send(player, PolydexBridge.createPacket(player));
        }
    }
}
