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

package me.wolfyscript.customcrafting.gui.recipe_creator;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.CCWindow;
import me.wolfyscript.customcrafting.gui.main_gui.ClusterMain;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.gui.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.gui.button.buttons.ActionButton;
import me.wolfyscript.utilities.api.inventory.gui.button.buttons.ToggleButton;
import me.wolfyscript.utilities.util.inventory.PlayerHeadUtils;
import org.bukkit.Material;

public class MenuIngredient extends CCWindow {

    static final String KEY = "ingredient";
    private static final String REPLACE_WITH_REMAINS = "replace_with_remains";

    public MenuIngredient(GuiCluster<CCCache> cluster, CustomCrafting customCrafting) {
        super(cluster, KEY, 54, customCrafting);
        setForceSyncUpdate(true);
    }

    @Override
    public void onInit() {
        for (int i = 0; i < 36; i++) {
            registerButton(new ButtonContainerItemIngredient(i));
        }
        registerButton(new ActionButton<>("back", new ButtonState<>(ClusterMain.BACK, PlayerHeadUtils.getViaURL("864f779a8e3ffa231143fa69b96b14ee35c16d669e19c75fd1a7da4bf306c"), (cache, guiHandler, player, inventory, slot, event) -> {
            var creatorCache = cache.getRecipeCreatorCache();
            creatorCache.getRecipeCache().setIngredient(creatorCache.getIngredientCache().getSlot(), creatorCache.getIngredientCache().getIngredient());
            guiHandler.openPreviousWindow();
            return true;
        })));
        registerButton(new ActionButton<>("tags", new ButtonState<>(ClusterRecipeCreator.TAGS, Material.NAME_TAG, (cache, guiHandler, player, guiInventory, i, inventoryInteractEvent) -> {
            cache.getRecipeCreatorCache().getTagSettingsCache().setRecipeItemStack(cache.getRecipeCreatorCache().getIngredientCache().getIngredient());
            guiHandler.openWindow("tag_settings");
            return true;
        })));
        registerButton(new ToggleButton<>(REPLACE_WITH_REMAINS, (cache, guiHandler, player, guiInventory, i) -> cache.getRecipeCreatorCache().getIngredientCache().getIngredient().isReplaceWithRemains(), new ButtonState<>(REPLACE_WITH_REMAINS + ".enabled", Material.BUCKET, (cache, guiHandler, player, guiInventory, i, inventoryInteractEvent) -> {
            cache.getRecipeCreatorCache().getIngredientCache().getIngredient().setReplaceWithRemains(false);
            return true;
        }), new ButtonState<>(REPLACE_WITH_REMAINS + ".disabled", Material.BUCKET, (cache, guiHandler, player, guiInventory, i, inventoryInteractEvent) -> {
            cache.getRecipeCreatorCache().getIngredientCache().getIngredient().setReplaceWithRemains(true);
            return true;
        })));
    }

    @Override
    public void onUpdateAsync(GuiUpdate<CCCache> update) {
        super.onUpdateAsync(update);
        update.setButton(0, "back");
        for (int i = 0; i < 36; i++) {
            update.setButton(9 + i, "item_container_" + i);
        }
        update.setButton(48, "tags");
        update.setButton(50, REPLACE_WITH_REMAINS);
    }
}
