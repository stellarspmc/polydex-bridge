package dev.mattidragon.polydexbridge;

import dev.mattidragon.polydexbridge.data.BridgeRecipe;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.List;

public class PolydexBridgeClient implements ClientModInitializer {
	public static final List<BridgeRecipe> RECIPES = new ArrayList<>();
	public static Runnable onPolyRecipes = () -> {};
	public static boolean waitForPoly = false;
	
	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> disconnect());
		ClientConfigurationConnectionEvents.INIT.register(((_, _) -> disconnect()));
		
		ClientConfigurationNetworking.registerGlobalReceiver(BridgeEnablePacket.ID, (_, _) -> waitForPoly = true);
		ClientPlayNetworking.registerGlobalReceiver(PolydexRecipesPacket.ID, (packet, _) -> {
			RECIPES.clear();
			RECIPES.addAll(packet.recipes());
			onPolyRecipes.run();
		});
	}
	
	private void disconnect() {
		RECIPES.clear();
		waitForPoly = false;
	}
}