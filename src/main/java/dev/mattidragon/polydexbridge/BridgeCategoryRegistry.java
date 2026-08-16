package dev.mattidragon.polydexbridge;

import dev.mattidragon.polydexbridge.data.BridgeCategory;
import dev.mattidragon.polydexbridge.data.BridgeRecipe;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class BridgeCategoryRegistry {
    private static final Map<Identifier, IRecipeType<BridgeRecipe>> TYPES = new HashMap<>();

    public static IRecipeType<BridgeRecipe> getOrCreateType(Identifier id) {
        return TYPES.computeIfAbsent(id, i ->
                IRecipeType.create(i.getNamespace(), i.getPath(), BridgeRecipe.class)
        );
    }

    public static IRecipeType<BridgeRecipe> getOrCreateType(BridgeCategory category) {
        return getOrCreateType(category.id());
    }
}
