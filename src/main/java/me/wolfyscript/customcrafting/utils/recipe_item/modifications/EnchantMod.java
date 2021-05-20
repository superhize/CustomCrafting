package me.wolfyscript.customcrafting.utils.recipe_item.modifications;

import org.bukkit.inventory.ItemStack;

public class EnchantMod extends Modification {

    public EnchantMod() {
        super();
    }

    public EnchantMod(EnchantMod enchantMod) {
        super(enchantMod);
    }

    @Override
    public ItemStack apply(ItemStack[] ingredients, ItemStack result) {
        for (int slot : slots) {
            if (slot < ingredients.length) {
                ItemStack item = ingredients[slot];
                result.addUnsafeEnchantments(item.getEnchantments());
            }
        }
        return result;
    }

    @Override
    public Modification clone() {
        return new EnchantMod(this);
    }
}
