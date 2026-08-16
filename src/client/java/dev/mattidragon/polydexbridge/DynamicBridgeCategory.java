package dev.mattidragon.polydexbridge;

import dev.mattidragon.polydexbridge.data.BridgeRecipe;
import dev.mattidragon.polydexbridge.data.BridgeStack;
import dev.mattidragon.polydexbridge.data.Slot;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class DynamicBridgeCategory implements IRecipeCategory<BridgeRecipe> {
    private final IRecipeType<BridgeRecipe> recipeType;
    private final Component title;
    private final IDrawable icon;

    public DynamicBridgeCategory(IRecipeType<BridgeRecipe> recipeType, Component title, IDrawable icon) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
    }

    @Override public @NonNull IRecipeType<BridgeRecipe> getRecipeType() { return recipeType; }
    @Override public @NonNull Component getTitle() { return title; }
    @Override public int getWidth() { return 18 * 9; }
    @Override public int getHeight() { return 18 * 5; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NonNull IRecipeLayoutBuilder builder, BridgeRecipe recipe, @NonNull IFocusGroup focuses) {
        for (Slot input : recipe.inputs()) builder.addSlot(RecipeIngredientRole.INPUT, input.x() * 18, input.y() * 18).addIngredients(VanillaTypes.ITEM_STACK, input.stacks().stream().map(BridgeStack::stack).toList());
        for (Slot output : recipe.outputs()) builder.addSlot(RecipeIngredientRole.OUTPUT, output.x() * 18, output.y() * 18).addIngredients(VanillaTypes.ITEM_STACK, output.stacks().stream().map(BridgeStack::stack).toList());
    }
}