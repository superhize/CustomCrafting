package me.wolfyscript.customcrafting.utils.recipe_item.target;

import me.wolfyscript.customcrafting.utils.recipe_item.Result;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class SlotTarget extends ResultTarget {

    private int slot;

    public SlotTarget(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public Optional<Result<EmptyTarget>> get(ItemStack[] ingredients) {
        if (ingredients != null && slot > -1 && slot < ingredients.length) {
            return check(ingredients[slot]);
        }
        return Optional.empty();
    }
}
