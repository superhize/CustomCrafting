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

package me.wolfyscript.customcrafting.handlers;

import com.google.common.collect.Streams;
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.configs.MainConfig;
import me.wolfyscript.customcrafting.configs.recipebook.Categories;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.world.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataHandler {

    public static final File DATA_FOLDER = new File(CustomCrafting.inst().getDataFolder() + File.separator + "data");
    private final CustomCrafting customCrafting;
    private Categories categories;
    private List<Recipe> minecraftRecipes = new ArrayList<>();

    private final MainConfig config;
    private final WolfyUtilities api;
    private final DatabaseLoader databaseLoader;
    private final LocalStorageLoader localStorageLoader;
    private final ExtensionPackLoader extensionPackLoader;
    private final List<ResourceLoader> loaders = new ArrayList<>();
    private SaveDestination saveDestination = SaveDestination.LOCAL;

    public DataHandler(CustomCrafting customCrafting) {
        this.api = WolfyUtilities.get(customCrafting);
        this.config = customCrafting.getConfigHandler().getConfig();
        this.customCrafting = customCrafting;
        initCategories();

        if (customCrafting.getConfigHandler().getConfig().isDatabaseEnabled()) {
            //Currently, there is only support for SQL. MongoDB is planned!
            this.databaseLoader = new SQLDatabaseLoader(customCrafting);
            this.databaseLoader.setPriority(2);
            if (config.isLocalStorageEnabled()) {
                this.localStorageLoader = new LocalStorageLoader(customCrafting);
                this.localStorageLoader.setPriority(config.isLocalStorageBeforeDatabase() ? 3 : 1);
            } else {
                this.localStorageLoader = null;
            }
        } else {
            this.databaseLoader = null;
            this.localStorageLoader = new LocalStorageLoader(customCrafting);
        }
        this.extensionPackLoader = null; //No extension pack implementation yet. TODO

        initLoaders();
    }

    private void initLoaders() {
        loaders.add(localStorageLoader);
        if (databaseLoader != null) {
            loaders.add(databaseLoader);
        }
        if (extensionPackLoader != null) {
            loaders.add(extensionPackLoader);
        }
        loaders.sort(ResourceLoader::compareTo);
    }

    public void initCategories() {
        this.categories = customCrafting.getConfigHandler().getRecipeBookConfig().getCategories();
    }

    public DatabaseLoader getDatabaseLoader() {
        return databaseLoader;
    }

    public LocalStorageLoader getLocalStorageLoader() {
        return localStorageLoader;
    }

    public SaveDestination getSaveDestination() {
        return saveDestination;
    }

    public void setSaveDestination(SaveDestination saveDestination) {
        this.saveDestination = saveDestination;
    }

    public void load() {
        api.getConsole().info("$msg.startup.recipes.title$");
        var lastBukkitVersion = config.getInt("data.bukkit_version");
        var lastVersion = config.getInt("data.version");
        boolean upgrade = lastBukkitVersion < CustomCrafting.BUKKIT_VERSION || lastVersion < CustomCrafting.CONFIG_VERSION;
        for (ResourceLoader loader : loaders) {
            loader.load(upgrade);
        }
        if (upgrade) {
            config.set("data.version", CustomCrafting.CONFIG_VERSION);
            config.set("data.bukkit_version", CustomCrafting.BUKKIT_VERSION);
            config.save();
        }
    }

    public void loadRecipesAndItems() {
        load();
        categories.index();
        WorldUtils.getWorldCustomItemStore().initiateMissingBlockEffects();
    }

    public ResourceLoader getActiveLoader() {
        if (saveDestination == SaveDestination.LOCAL) {
            return localStorageLoader;
        } else {
            return databaseLoader;
        }
    }

    public List<Recipe> getMinecraftRecipes() {
        if (minecraftRecipes.isEmpty()) {
            minecraftRecipes = Streams.stream(Bukkit.recipeIterator()).filter(recipe -> recipe instanceof Keyed keyed && keyed.getKey().getNamespace().equals("minecraft")).sorted(Comparator.comparing(recipe -> ((Keyed) recipe).getKey().toString())).toList();
        }
        return minecraftRecipes;
    }

    public List<String> getBukkitNamespacedKeys() {
        return getMinecraftRecipes().stream().filter(Keyed.class::isInstance).map(recipe -> NamespacedKey.fromBukkit(((Keyed) recipe).getKey()).toString()).collect(Collectors.toList());
    }

    public Categories getCategories() {
        return categories;
    }
}
