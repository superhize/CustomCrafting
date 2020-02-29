package me.wolfyscript.customcrafting.recipes.types.elite_workbench;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.Conditions;
import me.wolfyscript.customcrafting.recipes.RecipePriority;
import me.wolfyscript.customcrafting.recipes.types.CraftingRecipe;
import me.wolfyscript.customcrafting.recipes.types.RecipeType;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.custom_items.CustomItem;
import me.wolfyscript.utilities.api.inventory.GuiUpdateEvent;
import me.wolfyscript.utilities.api.inventory.GuiWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class EliteCraftingRecipe implements CraftingRecipe<EliteCraftConfig> {

    private boolean exactMeta, hidden;
    private RecipePriority priority;
    private Conditions conditions;

    private EliteCraftConfig config;
    private String id;
    private String group;
    private List<CustomItem> result;
    private Map<Character, List<CustomItem>> ingredients;
    private WolfyUtilities api;
    protected int requiredGridSize;

    public EliteCraftingRecipe(EliteCraftConfig config) {
        this.result = config.getResult();
        this.id = config.getId();
        this.config = config;
        this.ingredients = config.getIngredients();
        this.group = config.getGroup();
        this.priority = config.getPriority();
        this.api = CustomCrafting.getApi();
        this.exactMeta = config.isExactMeta();
        this.conditions = config.getConditions();
        this.hidden = config.isHidden();

        this.requiredGridSize = 6;
        if (config.isShapeless()){
            if(ingredients.size() <= 9){
                requiredGridSize = 3;
            }else if (ingredients.size() <= 16){
                requiredGridSize = 4;
            }else if (ingredients.size() <= 25){
                requiredGridSize = 5;
            }else if (ingredients.size() <= 36){
                requiredGridSize = 6;
            }
        }else{
            config.getShape();
        }
    }

    @Override
    public void setIngredients(Map<Character, List<CustomItem>> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public Map<Character, List<CustomItem>> getIngredients() {
        return ingredients;
    }

    @Override
    public List<CustomItem> getIngredients(int slot) {
        return getIngredients().getOrDefault(LETTERS[slot], new ArrayList<>());
    }

    @Override
    public CustomItem getIngredient(int slot) {
        List<CustomItem> list = getIngredients(slot);
        return list.size() > 0 ? list.get(0) : null;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public void setResult(List<CustomItem> result) {
        this.result = result;
    }

    @Override
    public CustomItem getCustomResult() {
        return getCustomResults().get(0);
    }

    @Override
    public List<CustomItem> getCustomResults() {
        return result;
    }

    public EliteCraftConfig getConfig() {
        return config;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public RecipePriority getPriority() {
        return priority;
    }

    @Override
    public boolean isExactMeta() {
        return exactMeta;
    }

    @Override
    public Conditions getConditions() {
        return conditions;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public RecipeType getRecipeType() {
        return RecipeType.ELITE_WORKBENCH;
    }

    @Override
    public void renderMenu(GuiWindow guiWindow, GuiUpdateEvent event) {
        event.setButton(6, "back");
        if (!getIngredients().isEmpty()) {
            event.setButton(24, "recipe_book", isShapeless() ? "workbench.shapeless_on" : "workbench.shapeless_off");
            int gridSize = 6;
            int startSlot = 0;
            int invSlot;
            for (int i = 0; i < gridSize * gridSize; i++) {
                invSlot = startSlot + i + (i / gridSize) * 3;
                event.setButton(invSlot, "recipe_book", "ingredient.container_" + invSlot);
            }
            event.setButton(25, "recipe_book", "ingredient.container_25");
        }
    }
}
