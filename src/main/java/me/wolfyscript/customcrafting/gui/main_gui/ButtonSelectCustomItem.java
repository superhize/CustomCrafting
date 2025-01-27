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
import me.wolfyscript.customcrafting.data.cache.items.ItemsButtonAction;
import me.wolfyscript.customcrafting.gui.Setting;
import me.wolfyscript.customcrafting.gui.item_creator.ClusterItemCreator;
import me.wolfyscript.customcrafting.gui.recipe_creator.ClusterRecipeCreator;
import me.wolfyscript.customcrafting.utils.ChatUtils;
import me.wolfyscript.customcrafting.utils.ItemLoader;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.chat.ClickData;
import me.wolfyscript.utilities.api.inventory.gui.GuiWindow;
import me.wolfyscript.utilities.api.inventory.gui.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.gui.button.buttons.ActionButton;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.Pair;
import me.wolfyscript.utilities.util.Registry;
import me.wolfyscript.utilities.util.chat.ChatColor;
import me.wolfyscript.utilities.util.inventory.InventoryUtils;
import me.wolfyscript.utilities.util.inventory.ItemUtils;
import me.wolfyscript.utilities.util.inventory.item_builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

class ButtonSelectCustomItem extends ActionButton<CCCache> {

    ButtonSelectCustomItem(CustomCrafting customCrafting, NamespacedKey namespacedKey) {
        super("item_" + namespacedKey.toString("__"), new ButtonState<>("custom_item_error", Material.STONE, (ItemsButtonAction) (cache, items, guiHandler, player, inventory, i, event) -> {
            if (!Registry.CUSTOM_ITEMS.has(namespacedKey) || ItemUtils.isAirOrNull(Registry.CUSTOM_ITEMS.get(namespacedKey))) {
                return true;
            }
            WolfyUtilities api = customCrafting.getApi();
            var customItem = Registry.CUSTOM_ITEMS.get(namespacedKey);
            if (event instanceof InventoryClickEvent clickEvent) {
                if (clickEvent.isRightClick()) {
                    if (clickEvent.isShiftClick()) {
                        api.getChat().sendKey(player, ClusterMain.ITEM_LIST, "delete.confirm", new Pair<>("%item%", customItem.getNamespacedKey().toString()));
                        api.getChat().sendActionMessage(player, new ClickData("$inventories.none.item_list.messages.delete.confirmed$", (wolfyUtilities, player1) -> {
                            guiHandler.openCluster();
                            Bukkit.getScheduler().runTaskAsynchronously(customCrafting, () -> ItemLoader.deleteItem(namespacedKey, player));
                        }), new ClickData("$inventories.none.item_list.messages.delete.declined$", (wolfyUtilities, player2) -> guiHandler.openCluster()));
                    } else if (customItem != null) {
                        items.setItem(items.isRecipeItem(), customItem.clone());
                        api.getInventoryAPI().getGuiWindow(ClusterRecipeCreator.ITEM_EDITOR).sendMessage(player, "item_editable");
                        guiHandler.openWindow(ClusterItemCreator.MAIN_MENU);
                    }
                } else if (clickEvent.isLeftClick()) {
                    if (cache.getSetting().equals(Setting.RECIPE_CREATOR)) {
                        cache.applyItem(customItem);
                        api.getInventoryAPI().getGuiWindow(ClusterRecipeCreator.ITEM_EDITOR).sendMessage(player, "item_applied");
                        List<? extends GuiWindow<?>> history = guiHandler.getClusterHistory().get(guiHandler.getCluster());
                        history.remove(history.size() - 1);
                        guiHandler.openCluster(ClusterRecipeCreator.KEY);
                    } else if (ChatUtils.checkPerm(player, "customcrafting.cmd.give")) {
                        var itemStack = customItem.create();
                        int amount = clickEvent.isShiftClick() ? itemStack.getMaxStackSize() : 1;
                        itemStack.setAmount(amount);
                        if (InventoryUtils.hasInventorySpace(player, itemStack)) {
                            player.getInventory().addItem(itemStack);
                        } else {
                            player.getLocation().getWorld().dropItem(player.getLocation(), itemStack);
                        }
                        if (clickEvent.isShiftClick()) {
                            api.getChat().sendMessage(player, "$commands.give.success_amount$", new Pair<>("%PLAYER%", player.getDisplayName()), new Pair<>("%ITEM%", namespacedKey.toString()), new Pair<>("%AMOUNT%", String.valueOf(amount)));
                        } else {
                            api.getChat().sendMessage(player, "$commands.give.success$", new Pair<>("%PLAYER%", player.getDisplayName()), new Pair<>("%ITEM%", namespacedKey.toString()));
                        }
                    }
                }
            }
            return true;
        }, (hashMap, cache, guiHandler, player, inventory, itemStack, slot, help) -> {
            var customItem = Registry.CUSTOM_ITEMS.get(namespacedKey);
            if (!ItemUtils.isAirOrNull(customItem)) {
                var itemB = new ItemBuilder(customItem.create());
                itemB.addLoreLine("");
                itemB.addLoreLine("§8" + namespacedKey);
                CustomCrafting.inst().getApi().getLanguageAPI().replaceKey("inventories.none.item_list.items.custom_item.lore").forEach(s -> itemB.addLoreLine(ChatColor.convert(s)));
                return itemB.create();
            }
            var itemB = new ItemBuilder(itemStack);
            itemB.addLoreLine("");
            itemB.addLoreLine("§8" + namespacedKey);
            itemB.addLoreLine("§c");
            return itemB.create();
        }));
    }

}
