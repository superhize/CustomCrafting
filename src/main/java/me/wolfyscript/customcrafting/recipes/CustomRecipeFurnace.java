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

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.JsonNode;
import me.wolfyscript.utilities.util.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;

public class CustomRecipeFurnace extends CustomRecipeCooking<CustomRecipeFurnace, FurnaceRecipe> {

    public CustomRecipeFurnace(NamespacedKey namespacedKey, JsonNode node) {
        super(namespacedKey, node);
    }

    public CustomRecipeFurnace(NamespacedKey key) {
        super(key);
    }

    public CustomRecipeFurnace(CustomRecipeFurnace customRecipeFurnace) {
        super(customRecipeFurnace);
    }

    @Override
    public FurnaceRecipe getVanillaRecipe() {
        if (!getSource().isEmpty()) {
            return new FurnaceRecipe(getNamespacedKey().toBukkit(CustomCrafting.inst()), getResult().getItemStack(), getRecipeChoice(), getExp(), getCookingTime());
        }
        return null;
    }

    @Override
    public RecipeType<CustomRecipeFurnace> getRecipeType() {
        return RecipeType.FURNACE;
    }

    @Override
    public CustomRecipeFurnace clone() {
        return new CustomRecipeFurnace(this);
    }

    @Override
    public boolean validType(Material material) {
        return material.equals(Material.FURNACE);
    }
}
