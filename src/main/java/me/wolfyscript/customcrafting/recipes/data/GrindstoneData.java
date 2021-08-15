package me.wolfyscript.customcrafting.recipes.data;

import me.wolfyscript.customcrafting.recipes.CustomRecipeGrindstone;
import me.wolfyscript.customcrafting.utils.recipe_item.Result;
import me.wolfyscript.utilities.api.inventory.custom_items.CustomItem;

import java.util.Optional;

public class GrindstoneData {

    private final CustomRecipeGrindstone recipe;
    private final Result result;
    private final CustomItem inputTop;
    private final CustomItem inputBottom;
    private final boolean validItem;

    public GrindstoneData(CustomRecipeGrindstone recipe, Result result, boolean validItem, CustomItem inputTop, CustomItem inputBottom) {
        this.recipe = recipe;
        this.result = result;
        this.validItem = validItem;
        this.inputTop = inputTop;
        this.inputBottom = inputBottom;
    }

    public CustomItem getInputBottom() {
        return inputBottom;
    }

    public CustomItem getInputTop() {
        return inputTop;
    }

    public CustomRecipeGrindstone getRecipe() {
        return recipe;
    }

    public Optional<Result> getResult() {
        return Optional.ofNullable(result);
    }

    public boolean isValidItem() {
        return validItem;
    }
}