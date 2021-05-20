package me.wolfyscript.customcrafting.utils.recipe_item.modifications;

import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.annotation.JsonAutoDetect;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.annotation.JsonSubTypes;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bukkit.inventory.ItemStack;

/**
 * Modifications allow to modify the result depending on the ingredients.
 * This makes it possible to modify the NBT of the result, completely replace the result ItemStack, etc.
 * <p>
 * They can be chained together in different ways to further change the way the result is manipulated.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EnchantMod.class, name = "enchant"),
        @JsonSubTypes.Type(value = DisplayNameMod.class, name = "display_name"),
        @JsonSubTypes.Type(value = LoreMod.class, name = "lore"),
        @JsonSubTypes.Type(value = CustomMod.class, name = "custom")
})
public abstract class Modification {

    protected final int[] slots;

    protected Modification() {
        this.slots = new int[]{0};
    }

    protected Modification(int[] slots) {
        this.slots = slots;
    }

    protected Modification(Modification modification) {
        this.slots = modification.slots;
    }

    public int[] getSlots() {
        return slots;
    }

    /**
     * Apply changes to the Result {@link ItemStack} depending on the Ingredients.
     *
     * @param ingredients The ingredients of the recipe.
     * @param result      The result of the recipe that was selected (After the random selection of there are multiple results).
     * @return The modified result {@link ItemStack}
     */
    public abstract ItemStack apply(ItemStack[] ingredients, ItemStack result);

    /**
     * This method clones this modification.
     * It must call the copy constructor of the extending class and call the super copy constructor.
     *
     * @return A deep copy of this modification.
     */
    public abstract Modification clone();

}
