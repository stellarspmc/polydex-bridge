package dev.mattidragon.polydexbridge;

import dev.mattidragon.polydexbridge.data.BridgeStack;
import dev.mattidragon.polydexbridge.data.Slot;
import eu.pb4.polydex.api.v1.recipe.PageBuilder;
import eu.pb4.polydex.api.v1.recipe.PolydexIngredient;
import eu.pb4.polydex.api.v1.recipe.PolydexStack;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PageConverter implements PageBuilder {
    private final ServerPlayer player;
    public final List<Slot> icons = new ArrayList<>();
    public final List<Slot> inputs = new ArrayList<>();
    public final List<Slot> outputs = new ArrayList<>();

    public PageConverter(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void set(int x, int y, ItemStack stack) {
        icons.add(new Slot(x, y, new BridgeStack(stack)));
    }

    @Override
    public void set(int x, int y, ItemStack... stacks) {
        icons.add(new Slot(x, y, Arrays.stream(stacks).map(BridgeStack::new).toList()));
    }

    @Override
    public void set(int x, int y, SlotDisplay slotDisplay) {
        icons.add(new Slot(x, y, convertSlotDisplay(slotDisplay, player)));
    }

    @Override
    public void setOutput(int x, int y, ItemStack... stacks) {
        outputs.add(new Slot(x, y, Arrays.stream(stacks).map(BridgeStack::new).toList()));
    }

    @Override
    public void setOutput(int x, int y, PolydexStack<?>... stacks) {
        outputs.add(new Slot(x, y, Arrays.stream(stacks).map(stack -> new BridgeStack(stack.toItemStack(player), stack.chance())).toList()));
    }

    @Override
    public void setOutput(int x, int y, SlotDisplay slotDisplay) {
        outputs.add(new Slot(x, y, convertSlotDisplay(slotDisplay, player)));
    }

    @Override
    public void setIngredient(int x, int y, ItemStack... stacks) {
        inputs.add(new Slot(x, y, Arrays.stream(stacks).map(BridgeStack::new).toList()));
    }

    @Override
    public void setIngredient(int x, int y, Ingredient ingredient) {
        List<BridgeStack> stacks = ingredient.items()
                .map(ItemStack::new)
                .map(BridgeStack::new)
                .toList();
        inputs.add(new Slot(x, y, stacks));
    }

    @Override
    public void setIngredient(int x, int y, Optional<Ingredient> optional) {
        if (optional.isPresent()) {
            setIngredient(x, y, optional.get());
        } else {
            setEmpty(x, y);
        }
    }

    @Override
    public void setIngredient(int x, int y, SlotDisplay slotDisplay) {
        inputs.add(new Slot(x, y, convertSlotDisplay(slotDisplay, player)));
    }

    @Override
    public void setIngredient(int x, int y, PolydexIngredient<?> ingredient) {
        inputs.add(new Slot(x, y, convertIngredient(ingredient, player)));
    }

    @Override
    public void setIngredient(int x, int y, PolydexIngredient<?> ingredient, Consumer<GuiElementBuilder> consumer) {
        inputs.add(new Slot(x, y, ingredient.asStacks()
                .stream()
                .map(stack -> {
                    var builder = GuiElementBuilder.from(stack.toItemStack(player));
                    consumer.accept(builder);
                    return new BridgeStack(builder.asStack(), ingredient.chance());
                })
                .toList()));
    }

    @Override
    public void setEmpty(int x, int y) {
        inputs.add(new Slot(x, y, List.of()));
    }

    @Override
    public boolean hasTextures() {
        return true;
    }

    public static List<BridgeStack> convertIngredient(PolydexIngredient<?> ingredient, ServerPlayer player) {
        return ingredient.asStacks().stream().map(stack -> new BridgeStack(stack.toItemStack(player), ingredient.chance())).toList();
    }

    public static List<BridgeStack> convertSlotDisplay(SlotDisplay display, ServerPlayer player) {
        if (display == null) return List.of();
        ContextMap context = SlotDisplayContext.fromLevel(player.level());
        return display.resolveForStacks(context)
                .stream()
                .map(BridgeStack::new)
                .toList();
    }
}