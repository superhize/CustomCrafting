/*
 *       ____ _  _ ____ ___ ____ _  _ ____ ____ ____ ____ ___ _ _  _ ____
 *       |    |  | [__   |  |  | |\/| |    |__/ |__| |___  |  | |\ | | __
 *       |___ |__| ___]  |  |__| |  | |___ |  \ |  | |     |  | | \| |__]
 *
 *       CustomCrafting Recipe creation and management tool for Minecraft
 *                      Copyright (C) 2021  WolfyScript
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.wolfyscript.customcrafting.recipes;

import com.google.common.base.Preconditions;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.main_gui.ClusterMain;
import me.wolfyscript.customcrafting.gui.recipebook.ButtonContainerIngredient;
import me.wolfyscript.customcrafting.recipes.items.Ingredient;
import me.wolfyscript.customcrafting.recipes.items.Result;
import me.wolfyscript.customcrafting.utils.ItemLoader;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiHandler;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.gui.GuiWindow;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.core.JsonGenerator;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.JsonNode;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.SerializerProvider;
import me.wolfyscript.utilities.util.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CustomRecipeGrindstone extends CustomRecipe<CustomRecipeGrindstone> {

    private Ingredient inputTop;
    private Ingredient inputBottom;
    private int xp;

    public CustomRecipeGrindstone(NamespacedKey namespacedKey, JsonNode node) {
        super(namespacedKey, node);
        this.xp = node.path("exp").intValue();
        this.inputTop = ItemLoader.loadIngredient(node.path("input_top"));
        this.inputBottom = ItemLoader.loadIngredient(node.path("input_bottom"));
    }

    public CustomRecipeGrindstone(NamespacedKey key) {
        super(key);
        this.result = new Result();
        this.inputTop = new Ingredient();
        this.inputBottom = new Ingredient();
        this.xp = 0;
    }

    public CustomRecipeGrindstone(CustomRecipeGrindstone customRecipeGrindstone) {
        super(customRecipeGrindstone);
        this.inputBottom = customRecipeGrindstone.getInputBottom();
        this.inputTop = customRecipeGrindstone.getInputTop();
        this.xp = customRecipeGrindstone.getXp();
    }

    @Override
    public RecipeType<CustomRecipeGrindstone> getRecipeType() {
        return RecipeType.GRINDSTONE;
    }

    @Override
    public Ingredient getIngredient(int slot) {
        return slot == 0 ? getInputTop() : getInputBottom();
    }

    public Ingredient getInputTop() {
        return inputTop;
    }

    public void setInputTop(@NotNull Ingredient inputTop) {
        Preconditions.checkArgument(!inputTop.isEmpty() || !inputBottom.isEmpty(), "Recipe must have at least one non-air top or bottom ingredient!");
        this.inputTop = inputTop;
    }

    public Ingredient getInputBottom() {
        return inputBottom;
    }

    public void setInputBottom(@NotNull Ingredient inputBottom) {
        Preconditions.checkArgument(!inputBottom.isEmpty() || !inputTop.isEmpty(), "Recipe must have at least one non-air top or bottom ingredient!");
        this.inputBottom = inputBottom;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    @Override
    public CustomRecipeGrindstone clone() {
        return new CustomRecipeGrindstone(this);
    }

    @Override
    public void writeToJson(JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        super.writeToJson(gen, serializerProvider);
        gen.writeNumberField("exp", xp);
        gen.writeObjectField("result", result);
        gen.writeObjectField("input_top", getInputTop());
        gen.writeObjectField("input_bottom", getInputBottom());
    }

    @Override
    public void prepareMenu(GuiHandler<CCCache> guiHandler, GuiCluster<CCCache> cluster) {
        ((ButtonContainerIngredient) cluster.getButton(ButtonContainerIngredient.key(11))).setVariants(guiHandler, getInputTop());
        ((ButtonContainerIngredient) cluster.getButton(ButtonContainerIngredient.key(29))).setVariants(guiHandler, getInputBottom());
        ((ButtonContainerIngredient) cluster.getButton(ButtonContainerIngredient.key(24))).setVariants(guiHandler, getResult());
    }

    @Override
    public void renderMenu(GuiWindow<CCCache> guiWindow, GuiUpdate<CCCache> event) {
        event.setButton(11, ButtonContainerIngredient.namespacedKey(11));
        event.setButton(12, ClusterMain.GLASS_GREEN);
        event.setButton(21, ClusterMain.GLASS_GREEN);
        event.setButton(22, new NamespacedKey("recipe_book", "grindstone"));
        event.setButton(23, ClusterMain.GLASS_GREEN);
        event.setButton(24, ButtonContainerIngredient.namespacedKey(24));
        event.setButton(29, ButtonContainerIngredient.namespacedKey(29));
        event.setButton(30, ClusterMain.GLASS_GREEN);
    }
}
