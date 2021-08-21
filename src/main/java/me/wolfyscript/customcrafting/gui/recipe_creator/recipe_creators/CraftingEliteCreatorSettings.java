package me.wolfyscript.customcrafting.gui.recipe_creator.recipe_creators;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.MainCluster;
import me.wolfyscript.customcrafting.gui.RecipeCreatorCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;

public class CraftingEliteCreatorSettings extends RecipeCreator {

    public static final String KEY = "elite_crafting_settings";

    public CraftingEliteCreatorSettings(GuiCluster<CCCache> cluster, CustomCrafting customCrafting) {
        super(cluster, "elite_crafting_settings", 27, customCrafting);
    }

    @Override
    public void onUpdateAsync(GuiUpdate<CCCache> update) {
        super.onUpdateAsync(update);
        update.setButton(0, MainCluster.BACK);
        update.setButton(9, RecipeCreatorCluster.HIDDEN);
        update.setButton(11, RecipeCreatorCluster.GROUP);
        update.setButton(13, RecipeCreatorCluster.CONDITIONS);
        update.setButton(15, RecipeCreatorCluster.EXACT_META);
        update.setButton(17, RecipeCreatorCluster.PRIORITY);
    }

    @Override
    public boolean validToSave(CCCache cache) {
        var workbench = cache.getRecipeCreatorCache().getEliteCraftingCache();
        return workbench.getIngredients() != null && !workbench.getResult().isEmpty();
    }
}