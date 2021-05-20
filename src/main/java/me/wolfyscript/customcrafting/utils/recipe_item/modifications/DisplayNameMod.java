package me.wolfyscript.customcrafting.utils.recipe_item.modifications;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DisplayNameMod extends Modification {

    //Overwrite the results Display name if it exists! if false only applies name to result if it has none.
    private final boolean overwrite;
    //Allows to reset the Display name if the ingredient has no display name.
    private final boolean allowNulls;

    public DisplayNameMod() {
        super();
        this.overwrite = false;
        this.allowNulls = false;
    }

    public DisplayNameMod(DisplayNameMod displayNameMod) {
        super(displayNameMod);
        this.overwrite = displayNameMod.overwrite;
        this.allowNulls = displayNameMod.allowNulls;
    }

    @Override
    public ItemStack apply(ItemStack[] ingredients, ItemStack result) {
        if (result.hasItemMeta()) {
            ItemMeta resultMeta = result.getItemMeta();
            for (int slot : slots) {
                if (slot < ingredients.length) {
                    ItemStack item = ingredients[slot];
                    if (item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        if ((allowNulls || meta.hasDisplayName()) && (overwrite || !resultMeta.hasDisplayName())) {
                            resultMeta.setDisplayName(meta.getDisplayName());
                            result.setItemMeta(resultMeta);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Modification clone() {
        return new DisplayNameMod(this);
    }
}
