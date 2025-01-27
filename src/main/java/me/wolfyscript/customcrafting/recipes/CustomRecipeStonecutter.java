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
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.main_gui.ClusterMain;
import me.wolfyscript.customcrafting.gui.recipebook.ButtonContainerIngredient;
import me.wolfyscript.customcrafting.gui.recipebook.ClusterRecipeBook;
import me.wolfyscript.customcrafting.recipes.items.Ingredient;
import me.wolfyscript.customcrafting.recipes.items.Result;
import me.wolfyscript.customcrafting.utils.ItemLoader;
import me.wolfyscript.utilities.api.inventory.custom_items.references.APIReference;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiHandler;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.gui.GuiWindow;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.core.JsonGenerator;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.JsonNode;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.SerializerProvider;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.json.jackson.JacksonUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CustomRecipeStonecutter extends CustomRecipe<CustomRecipeStonecutter> implements ICustomVanillaRecipe<StonecuttingRecipe> {

    private static final String KEY_SOURCE = "source";

    private Ingredient source;

    public CustomRecipeStonecutter(NamespacedKey namespacedKey, JsonNode node) {
        super(namespacedKey, node);
        if (node.has(KEY_RESULT)) {
            //Some old config format, which saved the item directly as a reference
            setResult(node.path(KEY_RESULT).has("custom_amount") ? new Result(JacksonUtil.getObjectMapper().convertValue(node.path(KEY_RESULT), APIReference.class)) : ItemLoader.loadResult(node.path(KEY_RESULT)));
        }
        setSource(ItemLoader.loadIngredient(node.path(KEY_SOURCE)));
    }

    public CustomRecipeStonecutter(NamespacedKey key) {
        super(key);
        this.result = new Result();
        this.source = new Ingredient();
    }

    public CustomRecipeStonecutter(CustomRecipeStonecutter customRecipeStonecutter) {
        super(customRecipeStonecutter);
        this.result = customRecipeStonecutter.getResult();
        this.source = customRecipeStonecutter.getSource();
    }

    public Ingredient getSource() {
        return source;
    }

    public void setSource(@NotNull Ingredient source) {
        Preconditions.checkArgument(!source.isEmpty(), "Invalid source! Recipe must have non-air source!");
        this.source = source;
    }

    @Override
    public void writeToJson(JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        super.writeToJson(gen, serializerProvider);
        gen.writeObjectField(KEY_RESULT, this.result);
        gen.writeObjectField(KEY_SOURCE, this.source);
    }

    @Override
    public RecipeType<CustomRecipeStonecutter> getRecipeType() {
        return RecipeType.STONECUTTER;
    }

    @Override
    public Ingredient getIngredient(int slot) {
        return this.source;
    }

    @Override
    public CustomRecipeStonecutter clone() {
        return new CustomRecipeStonecutter(this);
    }

    @Override
    public void prepareMenu(GuiHandler<CCCache> guiHandler, GuiCluster<CCCache> cluster) {
        ((ButtonContainerIngredient) cluster.getButton(ButtonContainerIngredient.key(20))).setVariants(guiHandler, getSource());
        ((ButtonContainerIngredient) cluster.getButton(ButtonContainerIngredient.key(24))).setVariants(guiHandler, getResult());
    }

    @Override
    public void renderMenu(GuiWindow<CCCache> guiWindow, GuiUpdate<CCCache> event) {
        NamespacedKey glass = ClusterMain.GLASS_GREEN;
        event.setButton(20, new NamespacedKey(ClusterRecipeBook.KEY, ButtonContainerIngredient.key(20)));
        event.setButton(24, new NamespacedKey(ClusterRecipeBook.KEY, ButtonContainerIngredient.key(24)));
        event.setButton(29, glass);
        event.setButton(30, glass);
        event.setButton(31, ClusterRecipeBook.STONECUTTER);
        event.setButton(32, glass);
        event.setButton(33, glass);

        ItemStack whiteGlass = event.getInventory().getItem(53);
        if (whiteGlass != null) {
            var itemMeta = whiteGlass.getItemMeta();
            itemMeta.setCustomModelData(9007);
            whiteGlass.setItemMeta(itemMeta);
            event.setItem(53, whiteGlass);
        }
    }

    @Override
    public StonecuttingRecipe getVanillaRecipe() {
        if (!getResult().isEmpty() && !getSource().isEmpty()) {
            RecipeChoice choice = isExactMeta() ? new RecipeChoice.ExactChoice(getSource().getBukkitChoices()) : new RecipeChoice.MaterialChoice(getSource().getBukkitChoices().stream().map(ItemStack::getType).toList());
            return new StonecuttingRecipe(getNamespacedKey().toBukkit(CustomCrafting.inst()), getResult().getChoices().get(0).create(), choice);
        }
        return null;
    }

    @Override
    public boolean isVisibleVanillaBook() {
        return vanillaBook;
    }

    @Override
    public void setVisibleVanillaBook(boolean vanillaBook) {
        this.vanillaBook = vanillaBook;
    }
}
