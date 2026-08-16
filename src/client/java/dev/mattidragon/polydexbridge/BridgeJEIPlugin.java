package dev.mattidragon.polydexbridge;

import dev.mattidragon.polydexbridge.data.BridgeCategory;
import dev.mattidragon.polydexbridge.data.BridgeRecipe;
import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.NonNull;

import java.util.*;

@JeiPlugin
public class BridgeJEIPlugin implements IModPlugin {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(PolydexBridge.MOD_ID, "jei_plugin");
    private static IJeiRuntime jeiRuntime;

    @Override
    public @NonNull Identifier getPluginUid() { return ID; }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Items.PAPER, (stack, _) -> {
            Identifier polymerId = PolymerItemUtils.getPolymerIdentifier(stack);
            return polymerId != null ? polymerId.toString() : "";
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        Set<BridgeCategory> categoriesToRegister = new HashSet<>();
        for (BridgeRecipe recipe : PolydexBridgeClient.RECIPES) categoriesToRegister.addAll(recipe.categories());

        for (BridgeCategory category : categoriesToRegister) {
            IRecipeType<BridgeRecipe> type = BridgeCategoryRegistry.getOrCreateType(category);

            ItemStack iconStack = PolydexBridgeClient.RECIPES.stream()
                    .filter(r -> r.categories().contains(category))
                    .map(BridgeRecipe::typeIcon)
                    .findFirst()
                    .orElse(ItemStack.EMPTY);

            registration.addRecipeCategories(new DynamicBridgeCategory(type, category.name(), registration.getJeiHelpers().getGuiHelper().createDrawableItemStack(iconStack)));
        }
    }

    @Override
    public void onRuntimeAvailable(@NonNull IJeiRuntime runtime) {
        jeiRuntime = runtime;
        if (!PolydexBridgeClient.RECIPES.isEmpty()) injectServerRecipes(PolydexBridgeClient.RECIPES);
        PolymerClientUtils.ON_SEARCH_REBUILD.register(() -> {
            if (jeiRuntime != null) refreshJeiIngredients(jeiRuntime.getIngredientManager());
        });
    }

    private static void refreshJeiIngredients(IIngredientManager ingredientManager) {
        List<ItemStack> polymerItems = new ArrayList<>();
        PolymerClientUtils.provideCreativeModePolymerItems(polymerItems::add);
        if (!polymerItems.isEmpty()) ingredientManager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, polymerItems);
    }

    public static void injectServerRecipes(List<BridgeRecipe> recipes) {
        if (jeiRuntime == null) return;

        Map<IRecipeType<BridgeRecipe>, List<BridgeRecipe>> grouped = new HashMap<>();

        for (BridgeRecipe recipe : recipes) {
            for (BridgeCategory category : recipe.categories()) {
                IRecipeType<BridgeRecipe> type = BridgeCategoryRegistry.getOrCreateType(category);
                grouped.computeIfAbsent(type, k -> new ArrayList<>()).add(recipe);
            }
        }

        grouped.forEach((type, list) -> jeiRuntime.getRecipeManager().addRecipes(type, list));
    }
}
