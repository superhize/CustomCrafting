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

package me.wolfyscript.customcrafting.utils.cooking;

import me.wolfyscript.customcrafting.CCRegistry;
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.CustomRecipeBlasting;
import me.wolfyscript.customcrafting.recipes.CustomRecipeCooking;
import me.wolfyscript.customcrafting.recipes.CustomRecipeFurnace;
import me.wolfyscript.customcrafting.recipes.CustomRecipeSmoking;
import me.wolfyscript.customcrafting.recipes.conditions.Conditions;
import me.wolfyscript.customcrafting.recipes.data.*;
import me.wolfyscript.customcrafting.recipes.items.Result;
import me.wolfyscript.utilities.api.inventory.custom_items.CustomItem;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.Pair;
import me.wolfyscript.utilities.util.inventory.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public abstract class SmeltAPIAdapter {

    protected final CustomCrafting customCrafting;
    protected final CookingManager manager;

    protected SmeltAPIAdapter(CustomCrafting customCrafting, CookingManager manager) {
        this.customCrafting = customCrafting;
        this.manager = manager;
    }

    public abstract Pair<CookingRecipeData<?>, Boolean> process(FurnaceSmeltEvent blockEvent, Block block, Furnace furnace);

    protected Pair<CookingRecipeData<?>, Boolean> processRecipe(ItemStack source, NamespacedKey recipeKey, Block block) {
        if (CCRegistry.RECIPES.get(recipeKey) instanceof CustomRecipeCooking<?, ?> cookingRecipe && cookingRecipe.validType(block.getType())) {
            Optional<CustomItem> customSource = cookingRecipe.getSource().check(source, cookingRecipe.isExactMeta());
            if (customSource.isPresent()) {
                if (cookingRecipe.checkConditions(new Conditions.Data(null, block, null))) {
                    var data = new IngredientData(0, cookingRecipe.getSource(), customSource.get(), source);
                    return new Pair<>(switch (cookingRecipe.getRecipeType().getType()) {
                        case FURNACE -> new FurnaceRecipeData((CustomRecipeFurnace) cookingRecipe, data);
                        case SMOKER -> new SmokerRecipeData((CustomRecipeSmoking) cookingRecipe, data);
                        case BLAST_FURNACE -> new BlastingRecipeData((CustomRecipeBlasting) cookingRecipe, data);
                        default -> null;
                    }, true);
                }
            }
        }
        return new Pair<>(null, true);
    }

    /**
     * Applies the result to the furnace and clears the cached data afterwards.<br>
     * <p>
     * If there is no actively cached data for the specified block it won't do anything!
     *
     * @param event The event to set result for.
     */
    public void applyResult(FurnaceSmeltEvent event) {
        var block = event.getBlock();
        if (manager.cachedRecipeData.get(block) != null) {
            var data = manager.cachedRecipeData.get(block).getKey();
            var result = data.getResult();
            var inventory = ((Furnace) event.getBlock().getState()).getInventory();
            var currentResultItem = inventory.getResult();

            Bukkit.getScheduler().runTaskLater(customCrafting, () -> manager.clearCache(block), 1); //Clearing the cached data after 1 tick (event should be done).
            ItemStack itemResult = result.getItem(data, null, block);
            if (currentResultItem != null) {
                event.setCancelled(true);
                if (itemResult.isSimilar(currentResultItem)) {
                    int nextAmount = currentResultItem.getAmount() + itemResult.getAmount();
                    if (nextAmount <= currentResultItem.getMaxStackSize() && !ItemUtils.isAirOrNull(inventory.getSmelting())) {
                        inventory.getSmelting().setAmount(inventory.getSmelting().getAmount() - 1);
                        currentResultItem.setAmount(nextAmount);
                        result.executeExtensions(block.getLocation(), true, null);
                        result.removeCachedItem(block);
                    }
                }
            } else {
                event.setResult(itemResult);
                result.executeExtensions(block.getLocation(), true, null);
                result.removeCachedItem(block);
            }
        }
    }

}
