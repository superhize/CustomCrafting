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

package me.wolfyscript.customcrafting.commands.cc_subcommands;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.commands.AbstractSubCommand;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.recipebook.ClusterRecipeBook;
import me.wolfyscript.customcrafting.gui.recipebook.MenuRecipeBook;
import me.wolfyscript.customcrafting.utils.ChatUtils;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.inventory.gui.InventoryAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReloadSubCommand extends AbstractSubCommand {

    public ReloadSubCommand(CustomCrafting customCrafting) {
        super("reload", new ArrayList<>(), customCrafting);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String var3, @NotNull String[] var4) {
        WolfyUtilities api = customCrafting.getApi();
        if (sender instanceof Player p && ChatUtils.checkPerm(p, "customcrafting.cmd.reload")) {
            InventoryAPI<CCCache> invAPI = api.getInventoryAPI(CCCache.class);
            api.getChat().sendMessage(p, "&eReloading Config!");
            customCrafting.getConfigHandler().getConfig().load();
            api.getChat().sendMessage(p, "  - &aComplete");
            api.getChat().sendMessage(p, "&eReloading Languages!");
            customCrafting.getApi().getLanguageAPI().unregisterLanguages();
            customCrafting.getConfigHandler().loadLang();
            api.getChat().sendMessage(p, "  - &aComplete");
            api.getChat().sendMessage(p, "&eReloading Recipes/Items!");
            customCrafting.getConfigHandler().loadRecipeBookConfig();
            var dataHandler = customCrafting.getDataHandler();
            dataHandler.initCategories();
            dataHandler.load();
            dataHandler.getCategories().index();
            api.getChat().sendMessage(p, "  - &aComplete");
            api.getChat().sendMessage(p, "&eReloading GUIs");
            ((MenuRecipeBook) invAPI.getGuiWindow(ClusterRecipeBook.RECIPE_BOOK)).reset();
            invAPI.reset();
            api.getChat().sendMessage(p, "  - &aComplete");
        }
        return true;
    }

    @Override
    protected @Nullable List<String> onTabComplete(@NotNull CommandSender var1, @NotNull String var3, @NotNull String[] var4) {
        return null;
    }
}
