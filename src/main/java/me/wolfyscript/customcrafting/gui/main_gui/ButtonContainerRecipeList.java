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

package me.wolfyscript.customcrafting.gui.main_gui;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.Setting;
import me.wolfyscript.customcrafting.gui.recipe_creator.ClusterRecipeCreator;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.chat.ClickData;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiHandler;
import me.wolfyscript.utilities.api.inventory.gui.GuiWindow;
import me.wolfyscript.utilities.api.inventory.gui.button.Button;
import me.wolfyscript.utilities.api.nms.inventory.GUIInventory;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.Pair;
import me.wolfyscript.utilities.util.inventory.ItemUtils;
import me.wolfyscript.utilities.util.inventory.item_builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.io.IOException;
import java.util.HashMap;

class ButtonContainerRecipeList extends Button<CCCache> {

    private static final String KEY = "recipe_list.container_";

    static String key(int slot) {
        return KEY + slot;
    }

    private final CustomCrafting customCrafting;
    private final HashMap<GuiHandler<CCCache>, Recipe> recipes = new HashMap<>();
    private final HashMap<GuiHandler<CCCache>, CustomRecipe<?>> customRecipes = new HashMap<>();

    private final WolfyUtilities api;

    ButtonContainerRecipeList(int slot, CustomCrafting customCrafting) {
        super(key(slot), null);
        this.customCrafting = customCrafting;
        this.api = CustomCrafting.inst().getApi();
    }

    @Override
    public void init(GuiWindow guiWindow) {
        //NOT NEEDED
    }

    @Override
    public void init(GuiCluster<CCCache> guiCluster) {

    }

    @Override
    public void postExecute(GuiHandler<CCCache> guiHandler, Player player, GUIInventory<CCCache> inventory, ItemStack itemStack, int slot, InventoryInteractEvent event) throws IOException {

    }

    @Override
    public void preRender(GuiHandler<CCCache> guiHandler, Player player, GUIInventory<CCCache> inventory, ItemStack itemStack, int slot, boolean help) {

    }

    @Override
    public boolean execute(GuiHandler<CCCache> guiHandler, Player player, GUIInventory<CCCache> inventory, int slot, InventoryInteractEvent event) {
        CCCache cache = guiHandler.getCustomCache();
        if (event instanceof InventoryClickEvent clickEvent) {
            CustomRecipe<?> customRecipe = getCustomRecipe(guiHandler);
            if (clickEvent.isShiftClick() && customRecipe != null) {
                if (clickEvent.isLeftClick()) {
                    cache.setSetting(Setting.RECIPE_CREATOR);
                    cache.getRecipeCreatorCache().setRecipeType(customRecipe.getRecipeType());
                    try {
                        cache.getRecipeCreatorCache().loadRecipeIntoCache(customRecipe);
                        Bukkit.getScheduler().runTaskLater(customCrafting, () -> guiHandler.openWindow(new NamespacedKey(ClusterRecipeCreator.KEY, cache.getRecipeCreatorCache().getRecipeType().getCreatorID())), 1);
                    } catch (IllegalArgumentException ex) {
                        api.getChat().sendKey(player, ClusterMain.RECIPE_LIST, "invalid_recipe", new Pair<>("%recipe_type%", cache.getRecipeCreatorCache().getRecipeType().name()));
                    }
                } else {
                    api.getChat().sendKey(player, ClusterMain.RECIPE_LIST, "delete.confirm", new Pair<>("%recipe%", customRecipe.getNamespacedKey().toString()));
                    api.getChat().sendActionMessage(player, new ClickData("$inventories.none.recipe_list.messages.delete.confirmed$", (wolfyUtilities, player1) -> {
                        guiHandler.openCluster();
                        Bukkit.getScheduler().runTaskAsynchronously(customCrafting, () -> customRecipe.delete(player1));
                    }), new ClickData("$inventories.none.recipe_list.messages.delete.declined$", (wolfyUtilities, player2) -> guiHandler.openCluster()));
                }
            } else {
                if (customRecipe != null) {
                    customCrafting.getDisableRecipesHandler().toggleRecipe(customRecipe);
                } else {
                    customCrafting.getDisableRecipesHandler().toggleBukkitRecipe(((Keyed) getRecipe(guiHandler)).getKey());
                }
            }
        }
        return true;
    }

    @Override
    public void render(GuiHandler<CCCache> guiHandler, Player player, GUIInventory<CCCache> guiInventory, Inventory inventory, ItemStack itemStack, int slot, boolean help) {
        if (getCustomRecipe(guiHandler) != null) {
            CustomRecipe<?> recipe = getCustomRecipe(guiHandler);
            if (recipe != null) {
                var itemB = new ItemBuilder(recipe.getResult().getItemStack().clone());
                if (recipe.getResult().isEmpty()) {
                    itemB.setType(Material.STONE).addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 0).addItemFlags(ItemFlag.HIDE_ENCHANTS).setDisplayName("§r§7" + recipe.getNamespacedKey());
                }
                itemB.addLoreLine("§8" + recipe.getNamespacedKey());
                if (recipe.isDisabled()) {
                    itemB.addLoreLine(api.getLanguageAPI().replaceColoredKeys("$inventories.none.recipe_list.items.lores.disabled$"));
                } else {
                    itemB.addLoreLine(api.getLanguageAPI().replaceColoredKeys("$inventories.none.recipe_list.items.lores.enabled$"));
                }
                itemB.addLoreLine("");
                itemB.addLoreLine("§8" + recipe.getRecipeType().name());
                itemB.addLoreLine(api.getLanguageAPI().replaceColoredKeys("$inventories.none.recipe_list.items.lores.edit$"));
                itemB.addLoreLine(api.getLanguageAPI().replaceColoredKeys("$inventories.none.recipe_list.items.lores.delete$"));
                inventory.setItem(slot, itemB.create());
            }
        } else {
            Recipe recipe = getRecipe(guiHandler);
            if (recipe != null) {
                ItemBuilder itemB;
                if (ItemUtils.isAirOrNull(recipe.getResult())) {
                    itemB = new ItemBuilder(Material.STONE);
                    itemB.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 0).addItemFlags(ItemFlag.HIDE_ENCHANTS).setDisplayName("§r§7" + ((Keyed) recipe).getKey());
                } else {
                    itemB = new ItemBuilder(recipe.getResult());
                }
                itemB.addLoreLine("§8" + ((Keyed) recipe).getKey());
                if (customCrafting.getDisableRecipesHandler().isBukkitRecipeDisabled(((Keyed) recipe).getKey())) {
                    itemB.addLoreLine(api.getLanguageAPI().replaceColoredKeys("$inventories.none.recipe_list.items.lores.disabled$"));
                } else {
                    itemB.addLoreLine(api.getLanguageAPI().replaceColoredKeys("$inventories.none.recipe_list.items.lores.enabled$"));
                }
                inventory.setItem(slot, itemB.create());
            }
        }
    }

    public CustomRecipe<?> getCustomRecipe(GuiHandler<CCCache> guiHandler) {
        return customRecipes.getOrDefault(guiHandler, null);
    }

    public void setCustomRecipe(GuiHandler<CCCache> guiHandler, CustomRecipe<?> recipe) {
        customRecipes.put(guiHandler, recipe);
    }

    public Recipe getRecipe(GuiHandler<CCCache> guiHandler) {
        return recipes.getOrDefault(guiHandler, null);
    }

    public void setRecipe(GuiHandler<CCCache> guiHandler, Recipe recipe) {
        recipes.put(guiHandler, recipe);
    }
}
