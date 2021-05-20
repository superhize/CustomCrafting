package me.wolfyscript.customcrafting.utils.recipe_item.target;

import me.wolfyscript.customcrafting.utils.recipe_item.Result;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class FixedTarget extends ResultTarget {

    @Override
    public Optional<Result<EmptyTarget>> get(ItemStack[] ingredients) {
        return ingredients == null ? Optional.empty() : check(ingredients[0]);
    }
}
