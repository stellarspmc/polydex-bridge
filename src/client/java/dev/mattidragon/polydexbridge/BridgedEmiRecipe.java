package dev.mattidragon.polydexbridge;

import com.google.common.collect.Iterables;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.mattidragon.polydexbridge.data.BridgeRecipe;
import dev.mattidragon.polydexbridge.data.BridgeStack;
import dev.mattidragon.polydexbridge.data.Slot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BridgedEmiRecipe implements EmiRecipe {
    private static final Style TEXTURE_STYLE = Style.EMPTY.withFont(FontDescription.DEFAULT).withColor(ChatFormatting.WHITE); // not sure
    private final EmiRecipeCategory category;
    private final BridgeRecipe recipe;

    public BridgedEmiRecipe(EmiRecipeCategory category, BridgeRecipe recipe) {
        this.category = category;
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return recipe.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return recipe.inputs()
                .stream()
                .map(Slot::stacks)
                .map(BridgedEmiRecipe::convertIngredient)
                .toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return recipe.outputs()
                .stream()
                .map(Slot::stacks)
                .map(BridgedEmiRecipe::convertIngredient)
                .map(EmiIngredient::getEmiStacks)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public int getDisplayWidth() {
        return 18 * 9;
    }

    @Override
    public int getDisplayHeight() {
        return 18 * 5;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (Slot slot : Iterables.concat(recipe.inputs(), recipe.outputs(), recipe.icons())) {
            widgets.addTexture(EmiTexture.SLOT, slot.x() * 18, slot.y() * 18);
        }

        if (recipe.texture() != null) {
            widgets.addText(createTextureText(),
                    0,
                    -11,
                    0xffffffff,
                    false);
        }
        
        for (Slot input : recipe.inputs()) {
            widgets.add(createSlotWidget(input));
        }
        for (Slot output : recipe.outputs()) {
            widgets.add(createSlotWidget(output)).recipeContext(this);
        }
        for (Slot icon : recipe.icons()) {
            widgets.add(createIconWidget(icon));
        }
    }

    private MutableComponent createTextureText() {
        // We add the recipe texture as a child so that it can override the font
        // This is needed because other mods adding polydex integration use their own fonts
        assert recipe.texture() != null;
        return Component.literal("").setStyle(TEXTURE_STYLE).append(recipe.texture());
    }

    private static SlotWidget createSlotWidget(Slot input) {
        return new SlotWidget(convertIngredient(input.stacks()), input.x() * 18, input.y() * 18) {

            @Override
            public void extractRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, final float a) {
                var matrices = graphics.pose();
                matrices.pushMatrix();
                super.extractRenderState(graphics, mouseX, mouseY, a);
                matrices.popMatrix();
            }
        }.drawBack(false);
    }

    private static Widget createIconWidget(Slot slot) {
        var bounds = new Bounds(slot.x() * 18, slot.y() * 18, 18, 18);
        return new Widget() {
            @Override
            public Bounds getBounds() {
                return bounds;
            }

            @Override
            public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
                var stacks = slot.stacks();
                var item = (int) (System.currentTimeMillis() / 1000 % stacks.size());
                var current = stacks.get(item);

                var matrices = guiGraphicsExtractor.pose();
                matrices.pushMatrix();// Move matrix in 2D space (X, Y)
                matrices.translate(bounds.x() + 1, bounds.y() + 1);

                EmiStack.of(current.stack()).render(guiGraphicsExtractor, 0, 0, v);
                matrices.popMatrix();
            }

            @Override
            public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                var stacks = slot.stacks();
                var item = (int) (System.currentTimeMillis() / 1000 % stacks.size());
                var current = stacks.get(item);
                return getTooltipComponentListFromItem(current.stack());
            }

            public List<ClientTooltipComponent> getTooltipComponentListFromItem(ItemStack stack) {
                var list = Screen.getTooltipFromItem(Minecraft.getInstance(), stack)
                        .stream()
                        .map(EmiPort::ordered)
                        .map(ClientTooltipComponent::create)
                        .collect(Collectors.toCollection(ArrayList::new));
                stack.getTooltipImage().ifPresent(data -> {
                    try {
                        list.add(ClientTooltipComponent.create(data));
                    } catch (Throwable e) {
                        PolydexBridge.LOGGER.error("Error while getting tooltip data", e);
                    }
                });
                return list;
            }
        };
    }
    
    private static EmiIngredient convertIngredient(List<BridgeStack> stacks) {
        return EmiIngredient.of(stacks.stream().map(stack -> EmiStack.of(stack.stack()).setChance(stack.chance())).toList());
    }
}
