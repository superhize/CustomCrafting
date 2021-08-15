package me.wolfyscript.customcrafting.data.cache.recipe_creator;

import me.wolfyscript.customcrafting.recipes.*;
import me.wolfyscript.customcrafting.recipes.settings.CraftingRecipeSettings;
import me.wolfyscript.customcrafting.utils.recipe_item.Ingredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class RecipeCacheCraftingAbstract<S extends CraftingRecipeSettings<S>> extends RecipeCache<CraftingRecipe<?, S>> {

    protected boolean shapeless;
    protected Map<Integer, Ingredient> ingredients;
    private S settings;
    private boolean mirrorHorizontal;
    private boolean mirrorVertical;
    private boolean mirrorRotation;

    protected RecipeCacheCraftingAbstract() {
        super();
        this.shapeless = false;
        this.ingredients = new HashMap<>();
    }

    protected RecipeCacheCraftingAbstract(CraftingRecipe<?, S> recipe) {
        super(recipe);
        this.settings = recipe.getSettings().clone();
        this.shapeless = RecipeType.WORKBENCH_SHAPELESS.isInstance(recipe) || RecipeType.ELITE_WORKBENCH_SHAPELESS.isInstance(recipe);
        if (recipe instanceof AbstractRecipeShaped<?, ?> shaped) {
            this.mirrorHorizontal = shaped.mirrorHorizontal();
            this.mirrorVertical = shaped.mirrorVertical();
            this.mirrorRotation = shaped.mirrorRotation();
            this.ingredients = shaped.getMappedIngredients().entrySet().stream().collect(Collectors.toMap(entry -> ICraftingRecipe.LETTERS.indexOf(entry.getKey()), Map.Entry::getValue));
        } else if (recipe instanceof AbstractRecipeShapeless<?, ?> shapeless) {
            AtomicInteger index = new AtomicInteger();
            this.ingredients = shapeless.getIngredients().stream().collect(Collectors.toMap(ingredient -> index.getAndIncrement(), ingredient -> ingredient));
        }
    }

    @Override
    public void setIngredient(int slot, Ingredient ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            this.ingredients.remove(slot);
        } else {
            ingredients.buildChoices();
            this.ingredients.put(slot, ingredients);
        }
    }

    @Override
    public Ingredient getIngredient(int slot) {
        return ingredients.get(slot);
    }

    public Map<Integer, Ingredient> getIngredients() {
        return ingredients;
    }

    public S getSettings() {
        return settings;
    }

    public void setSettings(S settings) {
        this.settings = settings;
    }

    public boolean isShapeless() {
        return shapeless;
    }

    public void setShapeless(boolean shapeless) {
        this.shapeless = shapeless;
    }

    public boolean isMirrorHorizontal() {
        return mirrorHorizontal;
    }

    public void setMirrorHorizontal(boolean mirrorHorizontal) {
        this.mirrorHorizontal = mirrorHorizontal;
    }

    public boolean isMirrorVertical() {
        return mirrorVertical;
    }

    public void setMirrorVertical(boolean mirrorVertical) {
        this.mirrorVertical = mirrorVertical;
    }

    public boolean isMirrorRotation() {
        return mirrorRotation;
    }

    public void setMirrorRotation(boolean mirrorRotation) {
        this.mirrorRotation = mirrorRotation;
    }

    @Override
    protected CraftingRecipe<?, S> create(CraftingRecipe<?, S> recipe) {
        CraftingRecipe<?, S> craftingRecipe = super.create(recipe);
        if (craftingRecipe instanceof AbstractRecipeShapeless<?, ?> shapeless) {
            shapeless.setIngredients(ingredients.values().stream());
        } else if (craftingRecipe instanceof AbstractRecipeShaped<?, ?> shaped) {
            shaped.setMirrorHorizontal(isMirrorHorizontal());
            shaped.setMirrorVertical(isMirrorVertical());
            shaped.setMirrorRotation(isMirrorRotation());
            Map<Character, Ingredient> ingredientMap = ingredients.entrySet().stream().collect(Collectors.toMap(entry -> ICraftingRecipe.LETTERS.charAt(entry.getKey()), Map.Entry::getValue));
            shaped.generateMissingShape(List.copyOf(ingredientMap.keySet()));
            shaped.setIngredients(ingredientMap);
        }
        return craftingRecipe;
    }
}