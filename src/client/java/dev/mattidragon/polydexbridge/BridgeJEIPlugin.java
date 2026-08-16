package dev.mattidragon.polydexbridge;

import dev.mattidragon.polydexbridge.data.BridgeCategory;
import dev.mattidragon.polydexbridge.data.BridgeRecipe;
import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    public void registerCategories(@NonNull IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        Map<Identifier, TempCategory> categories = new HashMap<>();

        for (CategoryCache.SavedCategory saved : CategoryCache.load()) {
            Identifier id = Identifier.tryParse(saved.id());
            if (id == null) continue;

            Component name = Component.literal(saved.jsonName());
            categories.put(id, new TempCategory(name, ItemStack.EMPTY));
        }

        for (BridgeRecipe recipe : PolydexBridgeClient.RECIPES) {
            for (BridgeCategory category : recipe.categories()) {
                TempCategory existing = categories.get(category.id());
                ItemStack icon = !recipe.typeIcon().isEmpty() ? recipe.typeIcon() : (existing != null ? existing.icon() : ItemStack.EMPTY);
                categories.put(category.id(), new TempCategory(category.name(), icon));
            }
        }

        for (Map.Entry<Identifier, TempCategory> entry : categories.entrySet()) {
            TempCategory data = entry.getValue();
            IRecipeType<BridgeRecipe> type = BridgeCategoryRegistry.getOrCreateType(entry.getKey());
            IDrawable iconDrawable = guiHelper.createDrawableItemStack(data.icon());
            registration.addRecipeCategories(new DynamicBridgeCategory(type, data.name(), iconDrawable));
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
                grouped.computeIfAbsent(type, _ -> new ArrayList<>()).add(recipe);
            }
        }

        grouped.forEach((type, list) -> jeiRuntime.getRecipeManager().addRecipes(type, list));
    }

    private record TempCategory(Component name, ItemStack icon) {}
}
