package me.wolfyscript.customcrafting.utils.recipe_item.modifications;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LoreMod extends Modification {

    public LoreMod() {
        super();
    }

    public LoreMod(LoreMod loreMod) {
        super(loreMod);
    }

    @Override
    public ItemStack apply(ItemStack[] ingredients, ItemStack result) {
        if (result.hasItemMeta()) {
            ItemMeta resultMeta = result.getItemMeta();
            assert resultMeta != null;
            for (int slot : slots) {
                if (slot < ingredients.length) {
                    ItemStack item = ingredients[slot];
                    if (item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        if (meta.hasLore()) {
                            List<String> lore = resultMeta.hasLore() ? resultMeta.getLore() : new ArrayList<>();
                            lore.addAll(meta.getLore());
                            resultMeta.setLore(lore);
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
        return new LoreMod(this);
    }
}
