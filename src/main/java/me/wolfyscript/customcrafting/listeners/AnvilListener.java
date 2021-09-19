package me.wolfyscript.customcrafting.listeners;

import me.wolfyscript.customcrafting.CCRegistry;
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import me.wolfyscript.customcrafting.recipes.CustomRecipeAnvil;
import me.wolfyscript.customcrafting.recipes.RecipeType;
import me.wolfyscript.customcrafting.recipes.data.AnvilData;
import me.wolfyscript.customcrafting.recipes.data.IngredientData;
import me.wolfyscript.customcrafting.recipes.items.Result;
import me.wolfyscript.utilities.api.inventory.custom_items.CustomItem;
import me.wolfyscript.utilities.util.inventory.InventoryUtils;
import me.wolfyscript.utilities.util.inventory.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.Repairable;

import java.util.*;

public class AnvilListener implements Listener {

    private static final HashMap<UUID, AnvilData> preCraftedRecipes = new HashMap<>();

    private final CustomCrafting customCrafting;

    public AnvilListener(CustomCrafting customCrafting) {
        this.customCrafting = customCrafting;
    }

    @EventHandler
    public void onCheck(PrepareAnvilEvent event) {
        var player = (Player) event.getView().getPlayer();
        AnvilInventory inventory = event.getInventory();
        preCraftedRecipes.remove(player.getUniqueId());
        ItemStack inputLeft = inventory.getItem(0);
        ItemStack inputRight = inventory.getItem(1);
        if (ItemUtils.isAirOrNull(inputLeft) && ItemUtils.isAirOrNull(inputRight)) {
            event.setResult(null);
            return;
        }
        List<CustomRecipeAnvil> recipes = CCRegistry.RECIPES.getAvailable(RecipeType.ANVIL, player);
        recipes.sort(Comparator.comparing(CustomRecipe::getPriority));
        for (CustomRecipeAnvil recipe : recipes) {
            Optional<CustomItem> finalInputLeft = Optional.empty();
            Optional<CustomItem> finalInputRight = Optional.empty();
            if ((recipe.hasInputLeft() && (inputLeft == null || (finalInputLeft = recipe.getInputLeft().check(inputLeft, recipe.isExactMeta())).isEmpty())) || (recipe.hasInputRight() && (inputRight == null || (finalInputRight = recipe.getInputRight().check(inputRight, recipe.isExactMeta())).isEmpty()))) {
                continue;
            }
            //Recipe is valid at this point!
            final CustomItem result;
            AnvilData anvilData = new AnvilData(recipe, Map.of(0, new IngredientData(0, recipe.getInputLeft(), finalInputLeft.orElse(null), inputLeft), 1, new IngredientData(1, recipe.getInputRight(), finalInputRight.orElse(null), inputRight)));
            //Set the result depending on what is configured!
            if (recipe.getMode().equals(CustomRecipeAnvil.Mode.RESULT)) {
                //Recipe has a plain result set that we can use.
                result = recipe.getResult().getItem(player).orElse(new CustomItem(Material.AIR));
                anvilData.setUsedResult(true);
            } else {
                //Either none or durability mode is set.
                if (ItemUtils.isAirOrNull(event.getResult())) {
                    result = new CustomItem(inputLeft).clone();
                    if (!recipe.isBlockRename() && inventory.getRenameText() != null && !inventory.getRenameText().isEmpty()) {
                        result.setDisplayName(inventory.getRenameText());
                    }
                } else {
                    result = new CustomItem(event.getResult());
                    ItemStack resultStack = result.getItemStack();
                    if (result.hasItemMeta()) {
                        //Further recipe options to block features.
                        if (recipe.isBlockEnchant() && resultStack.hasItemMeta() && result.getItemMeta().hasEnchants()) {
                            //Block Enchants
                            for (Enchantment enchantment : resultStack.getEnchantments().keySet()) {
                                result.removeEnchantment(enchantment);
                            }
                            if (inputLeft != null) {
                                for (Map.Entry<Enchantment, Integer> entry : inputLeft.getEnchantments().entrySet()) {
                                    result.addUnsafeEnchantment(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                        if (recipe.isBlockRename()) {
                            //Block Renaming
                            if (inputLeft != null && inputLeft.hasItemMeta() && inputLeft.getItemMeta().hasDisplayName()) {
                                result.setDisplayName(inputLeft.getItemMeta().getDisplayName());
                            } else {
                                result.setDisplayName(null);
                            }
                        }
                        if (recipe.isBlockRepair() && result.getItemMeta() instanceof Damageable resultDamageable) {
                            //Block Repairing
                            if (inputLeft != null && inputLeft.hasItemMeta() && inputLeft.getItemMeta() instanceof Damageable damageable) {
                                resultDamageable.setDamage(damageable.getDamage());
                            }
                            result.setItemMeta(resultDamageable);
                        }
                    }
                }
                if (recipe.getMode().equals(CustomRecipeAnvil.Mode.DURABILITY)) {
                    //Durability mode is set.
                    if (result.hasCustomDurability()) {
                        result.setCustomDamage(Math.max(0, result.getCustomDamage() - recipe.getDurability()));
                    } else if (result.getItemMeta() instanceof Damageable damageable) {
                        damageable.setDamage(damageable.getDamage() - recipe.getDurability());
                        result.setItemMeta(damageable);
                    }
                }
            }

            int repairCost = Math.max(1, recipe.getRepairCost());
            if (inputLeft != null) {
                var inputMeta = inputLeft.getItemMeta();
                //Configure the Repair cost
                if (inputMeta instanceof Repairable repairable) {
                    int itemRepairCost = repairable.getRepairCost();
                    if (recipe.getRepairCostMode().equals(CustomRecipeAnvil.RepairCostMode.ADD)) {
                        repairCost = repairCost + itemRepairCost;
                    } else if (recipe.getRepairCostMode().equals(CustomRecipeAnvil.RepairCostMode.MULTIPLY)) {
                        repairCost = recipe.getRepairCost() * (itemRepairCost > 0 ? itemRepairCost : 1);
                    }
                }
                //Apply the repair cost to the result.
                if (recipe.isApplyRepairCost()) {
                    var itemMeta = result.getItemMeta();
                    if (itemMeta instanceof Repairable repairable) {
                        repairable.setRepairCost(repairCost);
                        result.setItemMeta(itemMeta);
                    }
                }
            }

            //Save current active recipe to consume correct item inputs!
            preCraftedRecipes.put(player.getUniqueId(), anvilData);
            final ItemStack finalResult = result.getItemStack();

            inventory.setRepairCost(repairCost);
            event.setResult(repairCost > 0 ? finalResult : null);
            player.updateInventory();
            int finalRepairCost = repairCost;
            Bukkit.getScheduler().runTask(customCrafting, () -> {
                if (inventory.getRepairCost() == 0) {
                    inventory.setRepairCost(finalRepairCost);
                }
                player.updateInventory();
            });
            break;
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() instanceof AnvilInventory inventory) {
            var player = (Player) event.getWhoClicked();
            if (event.getSlot() == 2 && !ItemUtils.isAirOrNull(event.getCurrentItem()) && preCraftedRecipes.get(player.getUniqueId()) != null) {
                event.setCancelled(true);
                var anvilData = preCraftedRecipes.get(player.getUniqueId());
                if (inventory.getRepairCost() > 0 && player.getLevel() >= inventory.getRepairCost()) {
                    ItemStack result = event.getCurrentItem();
                    ItemStack cursor = event.getCursor();
                    if (event.isShiftClick()) {
                        if (InventoryUtils.hasInventorySpace(player, result)) {
                            player.getInventory().addItem(result);
                        } else {
                            return;
                        }
                    } else if (ItemUtils.isAirOrNull(cursor) || (result.isSimilar(cursor) && cursor.getAmount() + result.getAmount() <= cursor.getMaxStackSize())) {
                        if (ItemUtils.isAirOrNull(cursor)) {
                            event.setCursor(result);
                        } else {
                            cursor.setAmount(cursor.getAmount() + result.getAmount());
                        }
                    } else {
                        return;
                    }
                    if (anvilData.isUsedResult()) {
                        Result recipeResult = anvilData.getResult();
                        recipeResult.executeExtensions(inventory.getLocation() != null ? inventory.getLocation() : player.getLocation(), inventory.getLocation() != null, player);
                        recipeResult.removeCachedItem(player);
                    }
                    preCraftedRecipes.remove(player.getUniqueId());

                    if (inventory.getLocation() != null && inventory.getLocation().getWorld() != null) {
                        //Play sound & TODO: damage the Anvil Block!
                        var location = inventory.getLocation();
                        location.getWorld().playEffect(location, Effect.ANVIL_USE, 0);
                    }

                    event.setCurrentItem(null);
                    player.updateInventory();

                    CustomItem inputLeft = anvilData.getLeftIngredient().customItem();
                    CustomItem inputRight = anvilData.getRightIngredient().customItem();

                    if (inputLeft != null && inventory.getItem(0) != null) {
                        ItemStack itemLeft = anvilData.getLeftIngredient().itemStack().clone();
                        inputLeft.remove(itemLeft, itemLeft.getAmount(), inventory);
                        inventory.setItem(0, itemLeft);
                    } else {
                        inventory.setItem(0, null);
                    }
                    if (inputRight != null && inventory.getItem(1) != null) {
                        ItemStack itemRight = anvilData.getRightIngredient().itemStack().clone();
                        inputRight.remove(itemRight, 1, inventory);
                        inventory.setItem(1, itemRight);
                    } else {
                        inventory.setItem(1, null);
                    }
                }
            }
        }
    }
}
