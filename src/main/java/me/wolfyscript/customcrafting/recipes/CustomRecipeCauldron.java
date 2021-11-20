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

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.common.collect.Streams;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.gui.recipebook.ButtonContainerIngredient;
import me.wolfyscript.customcrafting.recipes.conditions.Condition;
import me.wolfyscript.customcrafting.recipes.conditions.PermissionCondition;
import me.wolfyscript.customcrafting.recipes.data.CauldronData;
import me.wolfyscript.customcrafting.recipes.data.CraftingData;
import me.wolfyscript.customcrafting.recipes.data.IngredientData;
import me.wolfyscript.customcrafting.recipes.items.Ingredient;
import me.wolfyscript.customcrafting.recipes.items.Result;
import me.wolfyscript.customcrafting.utils.ItemLoader;
import me.wolfyscript.utilities.api.inventory.custom_items.CustomItem;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiHandler;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.gui.GuiWindow;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.core.JsonGenerator;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.JsonNode;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.SerializerProvider;
import me.wolfyscript.utilities.util.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomRecipeCauldron extends CustomRecipe<CustomRecipeCauldron> {

    private static final int MAX_INGREDIENTS = 6;
    private static final String MAX_INGREDIENTS_ERR = "Recipe cannot have more than " + MAX_INGREDIENTS + " ingredients!";

    private int cookingTime;
    private int waterLevel;
    private float xp;
    private CustomItem handItem;
    private List<Ingredient> ingredients;
    private int nonEmptyIngredientSize;
    private boolean hasAllowedEmptyIngredient;
    private boolean dropItems;
    private boolean needsFire;
    private boolean needsWater;

    public CustomRecipeCauldron(NamespacedKey namespacedKey, JsonNode node) {
        super(namespacedKey, node);
        this.xp = node.path("exp").floatValue();
        this.cookingTime = node.path("cookingTime").asInt(60);
        this.waterLevel = node.path("waterLevel").asInt(1);
        this.needsWater = node.path("water").asBoolean(true);
        this.needsFire = node.path("fire").asBoolean(true);
        {
            JsonNode dropNode = node.path("dropItems");
            this.dropItems = dropNode.path("enabled").asBoolean();
            this.handItem = ItemLoader.load(dropNode.path("handItem"));
        }
        JsonNode ingredientsNode = node.path("ingredients");
        if (ingredientsNode.isObject()) {
            //Convert the old single Ingredient to the new multi Ingredient system.
            Ingredient ingredient = ItemLoader.loadIngredient(node.path("ingredients"));
            setIngredients(ingredient.getItems().stream().map(Ingredient::new).toList());
            if(!ingredient.getTags().isEmpty()) {
                addIngredients(ingredient.getTags().stream().map(Ingredient::new).collect(Collectors.toList()));
            }
        } else {
            Preconditions.checkArgument(ingredientsNode.isArray(), "Error reading ingredients! Ingredient node must be an Array!");
            setIngredients(Streams.stream(ingredientsNode.elements()).map(ItemLoader::loadIngredient).toList());
        }

    }

    public CustomRecipeCauldron(NamespacedKey key) {
        super(key);
        this.result = new Result();
        this.ingredients = List.of();
        this.dropItems = true;
        this.xp = 0;
        this.cookingTime = 80;
        this.needsFire = false;
        this.waterLevel = 0;
        this.needsWater = true;
        this.handItem = new CustomItem(Material.AIR);
    }

    public CustomRecipeCauldron(CustomRecipeCauldron customRecipeCauldron) {
        super(customRecipeCauldron);
        this.result = customRecipeCauldron.getResult();
        setIngredients(customRecipeCauldron.getIngredients());
        this.dropItems = customRecipeCauldron.dropItems();
        this.xp = customRecipeCauldron.getXp();
        this.cookingTime = customRecipeCauldron.getCookingTime();
        this.needsFire = customRecipeCauldron.needsFire();
        this.waterLevel = customRecipeCauldron.getWaterLevel();
        this.needsWater = customRecipeCauldron.needsWater();
        this.handItem = customRecipeCauldron.getHandItem();
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public boolean needsFire() {
        return needsFire;
    }

    public void setNeedsFire(boolean needsFire) {
        this.needsFire = needsFire;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    public boolean needsWater() {
        return needsWater;
    }

    public void setNeedsWater(boolean needsWater) {
        this.needsWater = needsWater;
    }

    public float getXp() {
        return xp;
    }

    public void setXp(float xp) {
        this.xp = xp;
    }

    public boolean dropItems() {
        return dropItems;
    }

    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    public List<Item> checkRecipe(List<Item> items) {
        Map<Integer, IngredientData> dataMap = new HashMap<>();
        Deque<Ingredient> queue = Queues.newArrayDeque(this.ingredients);
        List<Ingredient> selectedIngreds = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) { //First we go through all the items in the grid.
            var recipeSlot = checkIngredient(queue, dataMap, items.get(i).getItemStack()); //Get the slot of the ingredient or -1 if non is found.
            if (recipeSlot == null) {
                if (i == 0 || selectedIngreds.isEmpty()) { //We can directly end the check if it fails for the first slot.
                    return null;
                }
                if (selectedIngreds.size() > i) {
                    //Add the previous selected recipe slot back into the queue.
                    queue.addLast(selectedIngreds.get(i));
                    selectedIngreds.remove(i);
                }
                //Go back one inventory slot
                i -= 2;
                continue;
            } else if (selectedIngreds.size() > i) {
                //Add the previous selected recipe slot back into the queue, so we don't miss it.
                queue.addLast(selectedIngreds.get(i));
                selectedIngreds.remove(i);
            }
            //Add the newly found slot to the used slots.
            selectedIngreds.add(recipeSlot);
        }
        if (queue.isEmpty() || (hasAllowedEmptyIngredient && (items.size() == ingredients.size() - queue.size()) && queue.stream().allMatch(Ingredient::isAllowEmpty)) ) {
            //The empty ingredients can be very tricky in shapeless recipes and shouldn't be used... but might as well implement it anyway.
            return new CauldronData(this, dataMap);
        }
        return null;
    }

    protected Ingredient checkIngredient(Deque<Ingredient> deque, Map<Integer, IngredientData> dataMap, ItemStack item) {
        int size = deque.size();
        for (int qj = 0; qj < size; qj++) {
            var ingredient = deque.removeFirst(); //Take the first key out of the queue.
            Optional<CustomItem> validItem = ingredient.check(item, isExactMeta());
            if (validItem.isPresent()) {
                var key = this.ingredients.indexOf(ingredient);
                dataMap.put(key, new IngredientData(key, ingredient, validItem.get(), item));
                return ingredient;
            }
            //Check failed. Let's add the key back into the queue. (To the end, so we don't check it again and again...)
            deque.addLast(ingredient);
        }
        return null;
    }

    public CustomItem getHandItem() {
        return handItem;
    }

    public void setHandItem(CustomItem handItem) {
        this.handItem = handItem;
    }

    @Override
    public RecipeType<CustomRecipeCauldron> getRecipeType() {
        return RecipeType.CAULDRON;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public Ingredient getIngredient(int slot) {
        return this.ingredients.get(slot);
    }

    public void addIngredients(Ingredient... ingredients) {
        addIngredients(Arrays.asList(ingredients));
    }

    public void addIngredients(List<Ingredient> ingredients) {
        Preconditions.checkArgument(this.ingredients.size() + ingredients.size() <= MAX_INGREDIENTS, MAX_INGREDIENTS_ERR);
        List<Ingredient> currentIngredients = new ArrayList<>(this.ingredients);
        currentIngredients.addAll(ingredients);
        setIngredients(currentIngredients);
    }

    public void addIngredient(int count, Ingredient ingredient) {
        Preconditions.checkArgument(ingredients.size() + count <= MAX_INGREDIENTS, MAX_INGREDIENTS_ERR);
        List<Ingredient> currentIngredients = new ArrayList<>(this.ingredients);
        for (int i = 0; i < count; i++) {
            currentIngredients.add(ingredient);
        }
        setIngredients(currentIngredients);
    }

    public void setIngredients(List<Ingredient> ingredients) {
        setIngredients(ingredients.stream());
    }

    public void setIngredients(Stream<Ingredient> ingredients) {
        List<Ingredient> ingredientsNew = ingredients.filter(ingredient -> ingredient != null && !ingredient.isEmpty()).toList();
        Preconditions.checkArgument(!ingredientsNew.isEmpty(), "Invalid ingredients! Recipe requires non-air ingredients!");
        this.ingredients = ingredientsNew;
        this.nonEmptyIngredientSize = (int) this.ingredients.stream().filter(ingredient -> !ingredient.isAllowEmpty()).count();
        this.hasAllowedEmptyIngredient = this.nonEmptyIngredientSize != this.ingredients.size();
    }

    /**
     * @deprecated The recipe can have multiple Ingredients now!
     * @return Always returns the first Ingredient in the List.
     */
    @Deprecated(forRemoval = true)
    public Ingredient getIngredient() {
        return getIngredient(0);
    }

    /**
     * @deprecated Redirects to {@link #addIngredients(Ingredient...)}
     */
    @Deprecated(forRemoval = true)
    public void setIngredient(Ingredient ingredient) {
        addIngredients(ingredient);
    }

    /**
     * @deprecated Redirects to {@link #addIngredients(Ingredient...)}
     */
    @Deprecated(forRemoval = true)
    private void setIngredient(int slot, Ingredient ingredient) {
        addIngredients(ingredient);
    }

    @Override
    public CustomRecipeCauldron clone() {
        return new CustomRecipeCauldron(this);
    }

    @Override
    public void writeToJson(JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        super.writeToJson(gen, serializerProvider);
        gen.writeObjectFieldStart("dropItems");
        gen.writeBooleanField("enabled", dropItems);
        gen.writeObjectField("handItem", handItem == null ? null : handItem.getApiReference());
        gen.writeEndObject();
        gen.writeNumberField("exp", xp);
        gen.writeNumberField("cookingTime", cookingTime);
        gen.writeNumberField("waterLevel", waterLevel);
        gen.writeBooleanField("water", needsWater);
        gen.writeBooleanField("fire", needsFire);
        gen.writeObjectField("result", this.result);
        gen.writeObjectField("ingredients", ingredients);
    }

    @Override
    public void prepareMenu(GuiHandler<CCCache> guiHandler, GuiCluster<CCCache> cluster) {
        Ingredient ingredients = getIngredient();
        int invSlot;
        for (int i = 0; i < 6; i++) {
            invSlot = 10 + i + (i / 3) * 6;
            if (i < ingredients.size()) {
                ((ButtonContainerIngredient) cluster.getButton(ButtonContainerIngredient.key(invSlot))).setVariants(guiHandler, Collections.singletonList(ingredients.getChoices().get(i)));
            } else {
                ((ButtonContainerIngredient) cluster.getButton(ButtonContainerIngredient.key(invSlot))).setVariants(guiHandler, Collections.singletonList(new CustomItem(Material.AIR)));
            }
        }
        ((ButtonContainerIngredient) cluster.getButton(ButtonContainerIngredient.key(25))).setVariants(guiHandler, getResult());
    }

    @Override
    public void renderMenu(GuiWindow<CCCache> guiWindow, GuiUpdate<CCCache> event) {
        int invSlot;
        for (int i = 0; i < 6; i++) {
            invSlot = 10 + i + (i / 3) * 6;
            event.setButton(invSlot, ButtonContainerIngredient.namespacedKey(invSlot));
        }
        List<Condition<?>> conditions = getConditions().getValues().stream().filter(condition -> !condition.getNamespacedKey().equals(PermissionCondition.KEY)).toList();
        int startSlot = 9 / (conditions.size() + 1);
        int slot = 0;
        for (Condition<?> condition : conditions) {
            event.setButton(36 + startSlot + slot, new NamespacedKey("recipe_book", "conditions." + condition.getId()));
            slot += 2;
        }
        event.setButton(23, new NamespacedKey("recipe_book", needsWater() ? "cauldron.water.enabled" : "cauldron.water.disabled"));
        event.setButton(32, new NamespacedKey("recipe_book", needsFire() ? "cauldron.fire.enabled" : "cauldron.fire.disabled"));
        event.setButton(25, ButtonContainerIngredient.namespacedKey(25));
    }
}
