package me.wolfyscript.customcrafting.listeners;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.Conditions;
import me.wolfyscript.customcrafting.recipes.types.grindstone.GrindstoneData;
import me.wolfyscript.customcrafting.recipes.types.grindstone.GrindstoneRecipe;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.custom_items.CustomItem;
import me.wolfyscript.utilities.api.utils.ItemUtils;
import me.wolfyscript.utilities.api.utils.RandomCollection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class GrindStoneListener implements Listener {

    private static HashMap<UUID, GrindstoneData> preCraftedRecipes = new HashMap<>();
    private static HashMap<UUID, HashMap<String, CustomItem>> precraftedItems = new HashMap<>();
    private WolfyUtilities api;

    public GrindStoneListener(WolfyUtilities api) {
        this.api = api;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (WolfyUtilities.hasVillagePillageUpdate()) {
            if (!event.getClickedInventory().getType().equals(InventoryType.GRINDSTONE)) return;
            Player player = (Player) event.getWhoClicked();
            InventoryAction action = event.getAction();
            Inventory inventory = event.getClickedInventory();
            if (event.getSlot() == 2 && !ItemUtils.isAirOrNull(inventory.getItem(2)) && (action.toString().startsWith("PICKUP_") || action.equals(InventoryAction.COLLECT_TO_CURSOR) || action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))) {
                //Take out item!
                if (preCraftedRecipes.get(player.getUniqueId()) != null) {
                    //Custom Recipe
                    GrindstoneData grindstoneData = preCraftedRecipes.get(player.getUniqueId());
                    CustomItem inputTop = grindstoneData.getInputTop();
                    CustomItem inputBottom = grindstoneData.getInputBottom();
                    if (inputTop != null) {
                        inputTop.consumeItem(inventory.getItem(0), 1, inventory);
                    }
                    if (inputBottom != null) {
                        inputBottom.consumeItem(inventory.getItem(1), 1, inventory);
                    }
                } else {
                    //Vanilla Recipe
                }
            } else {
                //Place in items and click empty result slot
                if (event.getSlot() != 2) {
                    final ItemStack cursor = event.getCursor(); //And the item in the cursor
                    final ItemStack currentItem = event.getCurrentItem(); //We want to get the item in the slot
                    event.setCancelled(true);
                    boolean validItem = false;
                    if (event.getSlot() == 0) {
                        for (GrindstoneRecipe grindstoneRecipe : CustomCrafting.getRecipeHandler().getGrindstoneRecipes()) {
                            CustomItem finalInputTop = null;
                            if (grindstoneRecipe.getInputTop() != null && !grindstoneRecipe.getInputTop().isEmpty()) {
                                if (ItemUtils.isAirOrNull(cursor)) {
                                    continue;
                                }
                                for (CustomItem customItem : grindstoneRecipe.getInputTop()) {
                                    if (customItem.isSimilar(cursor, grindstoneRecipe.isExactMeta())) {
                                        finalInputTop = customItem.clone();
                                        break;
                                    }
                                }
                                if (finalInputTop == null) {
                                    continue;
                                }
                            } else if (!ItemUtils.isAirOrNull(cursor)) {
                                continue;
                            }
                            validItem = true;
                            break;
                        }
                    } else {
                        for (GrindstoneRecipe grindstoneRecipe : CustomCrafting.getRecipeHandler().getGrindstoneRecipes()) {
                            CustomItem finalInputBottom = null;
                            if (grindstoneRecipe.getInputBottom() != null && !grindstoneRecipe.getInputBottom().isEmpty()) {
                                if (ItemUtils.isAirOrNull(cursor)) {
                                    continue;
                                }
                                for (CustomItem customItem : grindstoneRecipe.getInputBottom()) {
                                    if (customItem.isSimilar(cursor, grindstoneRecipe.isExactMeta())) {
                                        finalInputBottom = customItem.clone();
                                        break;
                                    }
                                }
                                if (finalInputBottom == null) {
                                    continue;
                                }
                            } else if (!ItemUtils.isAirOrNull(cursor)) {
                                continue;
                            }
                            if (!grindstoneRecipe.getConditions().checkConditions(grindstoneRecipe, new Conditions.Data(player, player.getTargetBlock(null, 5), event.getView()))) {
                                continue;
                            }
                            validItem = true;
                            break;
                        }
                    }
                    if (!validItem) {
                        if (ItemUtils.isAirOrNull(cursor) || allowedInGrindstone(cursor.getType())) {
                            event.setCancelled(false);
                        }
                        return;
                    }
                    //Check the opposite item depending on which slot was clicked
                    preCraftedRecipes.put(player.getUniqueId(), null);
                    GrindstoneRecipe foundRecipe = null;
                    CustomItem finalInputTop = null;
                    CustomItem finalInputBottom = null;
                    for (GrindstoneRecipe grindstoneRecipe : CustomCrafting.getRecipeHandler().getGrindstoneRecipes()) {
                        if (event.getSlot() == 0) {
                            ItemStack input = inventory.getItem(1);
                            if (grindstoneRecipe.getInputBottom() != null && !grindstoneRecipe.getInputBottom().isEmpty()) {
                                if (ItemUtils.isAirOrNull(input)) {
                                    continue;
                                }
                                for (CustomItem customItem : grindstoneRecipe.getInputBottom()) {
                                    if (customItem.isSimilar(input, grindstoneRecipe.isExactMeta())) {
                                        finalInputBottom = customItem.clone();
                                        break;
                                    }
                                }
                                if (finalInputBottom == null) {
                                    continue;
                                }
                            } else if (!ItemUtils.isAirOrNull(input)) {
                                continue;
                            }
                        } else {
                            ItemStack input = inventory.getItem(0);
                            if (grindstoneRecipe.getInputTop() != null && !grindstoneRecipe.getInputTop().isEmpty()) {
                                if (ItemUtils.isAirOrNull(input)) {
                                    continue;
                                }
                                for (CustomItem customItem : grindstoneRecipe.getInputTop()) {
                                    if (customItem.isSimilar(input, grindstoneRecipe.isExactMeta())) {
                                        finalInputTop = customItem.clone();
                                        break;
                                    }
                                }
                                if (finalInputTop == null) {
                                    continue;
                                }
                            } else if (!ItemUtils.isAirOrNull(input)) {
                                continue;
                            }
                        }
                        if (!grindstoneRecipe.getConditions().checkConditions(grindstoneRecipe, new Conditions.Data(player, player.getTargetBlock(null, 5), event.getView()))) {
                            continue;
                        }
                        foundRecipe = grindstoneRecipe;
                        break;
                    }
                    if (foundRecipe == null) {
                        if (ItemUtils.isAirOrNull(cursor) || allowedInGrindstone(cursor.getType())) {
                            event.setCancelled(false);
                        }
                        return; //Returns and uses Vanilla recipe instead
                    }
                    RandomCollection<CustomItem> items = new RandomCollection<>();
                    for (CustomItem customItem : foundRecipe.getCustomResults()) {
                        if (!customItem.hasPermission() || player.hasPermission(customItem.getPermission())) {
                            items.add(customItem.getRarityPercentage(), customItem.clone());
                        }
                    }
                    HashMap<String, CustomItem> precraftedItem = precraftedItems.getOrDefault(player, new HashMap<>());
                    CustomItem result = new CustomItem(Material.AIR);
                    if (precraftedItem.get(foundRecipe.getId()) == null) {
                        if (!items.isEmpty()) {
                            result = items.next();
                            precraftedItem.put(foundRecipe.getId(), result);
                            precraftedItems.put(player.getUniqueId(), precraftedItem);
                        }
                    } else {
                        result = precraftedItem.get(foundRecipe.getId());
                    }
                    preCraftedRecipes.put(player.getUniqueId(), new GrindstoneData(foundRecipe, finalInputTop, finalInputBottom));
                    inventory.setItem(2, result.getRealItem());
                    Bukkit.getScheduler().runTask(CustomCrafting.getInst(), () -> {
                        //Place items when the recipe is valid
                        if (event.getClickedInventory() == null) return;
                        if (event.getClickedInventory().getType() != InventoryType.GRINDSTONE) return;
                        if (event.isRightClick()) {
                            //Dropping one item or pick up half
                            if (event.getAction().equals(InventoryAction.PICKUP_HALF) || event.getAction().equals(InventoryAction.PICKUP_SOME)) {
                                return;
                            }
                            //Dropping one item
                            if (ItemUtils.isAirOrNull(currentItem)) {
                                cursor.setAmount(cursor.getAmount() - 1);
                                ItemStack itemStack = new ItemStack(cursor);
                                Bukkit.getScheduler().runTaskLater(CustomCrafting.getInst(), () -> {
                                    itemStack.setAmount(1);
                                    inventory.setItem(event.getSlot(), itemStack);
                                    event.getWhoClicked().setItemOnCursor(cursor);
                                }, 1);
                            } else if (currentItem.isSimilar(cursor) || cursor.isSimilar(currentItem)) {
                                event.setCancelled(false);
                                if (currentItem.getAmount() < currentItem.getMaxStackSize()) {
                                    if (cursor.getAmount() > 0) {
                                        event.setCancelled(true);
                                        currentItem.setAmount(currentItem.getAmount() + 1);
                                        cursor.setAmount(cursor.getAmount() - 1);
                                        player.updateInventory();
                                    }
                                }
                            }
                        } else {
                            //Placing an item
                            if (ItemUtils.isAirOrNull(event.getCursor())) {
                                return; //Make sure cursor contains item
                            }
                            if (!ItemUtils.isAirOrNull(currentItem)) {
                                if (currentItem.isSimilar(cursor) || cursor.isSimilar(currentItem)) {
                                    int possibleAmount = currentItem.getMaxStackSize() - currentItem.getAmount();
                                    currentItem.setAmount(currentItem.getAmount() + (cursor.getAmount() < possibleAmount ? cursor.getAmount() : possibleAmount));
                                    cursor.setAmount(cursor.getAmount() - possibleAmount);
                                } else {
                                    if (!ItemUtils.isAirOrNull(cursor)) {
                                        ItemStack itemStack = new ItemStack(cursor);
                                        event.getView().setCursor(currentItem);
                                        inventory.setItem(event.getSlot(), itemStack);
                                    }
                                }
                            } else if (!event.getAction().equals(InventoryAction.PICKUP_ALL)) {
                                event.setCancelled(false);
                                ItemStack itemStack = new ItemStack(cursor);
                                Bukkit.getScheduler().runTask(CustomCrafting.getInst(), () -> {
                                    inventory.setItem(event.getSlot(), itemStack);
                                    event.getView().setCursor(new ItemStack(Material.AIR));
                                });
                            }
                            player.updateInventory();//Update the inventory
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory() == null) return;
        if (WolfyUtilities.hasVillagePillageUpdate()) {
            if (!event.getInventory().getType().equals(InventoryType.GRINDSTONE)) return;
            if (event.getInventorySlots().isEmpty()) return;
            event.setCancelled(true);

            //TODO: DRAG ITEMS INTO GRINDSTONE!

            /* Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getInventory();

            //Place in items and click empty result slot

            final ItemStack cursor = event.getCursor(); //And the item in the cursor
            final ItemStack currentItem = event.getCurrentItem(); //We want to get the item in the slot
            event.setCancelled(true);
            boolean validItem = false;
            if (event.getSlot() == 0) {
                for (GrindstoneRecipe grindstoneRecipe : CustomCrafting.getRecipeHandler().getGrindstoneRecipes()) {
                    CustomItem finalInputTop = null;
                    if (grindstoneRecipe.getInputTop() != null && !grindstoneRecipe.getInputTop().isEmpty()) {
                        if (ItemUtils.isAirOrNull(cursor)) {
                            continue;
                        }
                        for (CustomItem customItem : grindstoneRecipe.getInputTop()) {
                            if (customItem.isSimilar(cursor, grindstoneRecipe.isExactMeta())) {
                                finalInputTop = customItem.clone();
                                break;
                            }
                        }
                        if (finalInputTop == null) {
                            continue;
                        }
                    } else if (!ItemUtils.isAirOrNull(cursor)) {
                        continue;
                    }
                    validItem = true;
                    break;
                }
            } else {
                for (GrindstoneRecipe grindstoneRecipe : CustomCrafting.getRecipeHandler().getGrindstoneRecipes()) {
                    CustomItem finalInputBottom = null;
                    if (grindstoneRecipe.getInputBottom() != null && !grindstoneRecipe.getInputBottom().isEmpty()) {
                        if (ItemUtils.isAirOrNull(cursor)) {
                            continue;
                        }
                        for (CustomItem customItem : grindstoneRecipe.getInputBottom()) {
                            if (customItem.isSimilar(cursor, grindstoneRecipe.isExactMeta())) {
                                finalInputBottom = customItem.clone();
                                break;
                            }
                        }
                        if (finalInputBottom == null) {
                            continue;
                        }
                    } else if (!ItemUtils.isAirOrNull(cursor)) {
                        continue;
                    }
                    if (!grindstoneRecipe.getConditions().checkConditions(grindstoneRecipe, new Conditions.Data(player, player.getTargetBlock(null, 5), event.getView()))) {
                        continue;
                    }
                    validItem = true;
                    break;
                }
            }
            System.out.println("Activated After item: " + validItem);
            if (!validItem) {
                if (ItemUtils.isAirOrNull(cursor) || allowedInGrindstone(cursor.getType())) {
                    event.setCancelled(false);
                }
                return;
            }
            //Check the opposite item depending on which slot was clicked
            preCraftedRecipes.put(player.getUniqueId(), null);
            GrindstoneRecipe foundRecipe = null;
            CustomItem finalInputTop = null;
            CustomItem finalInputBottom = null;
            for (GrindstoneRecipe grindstoneRecipe : CustomCrafting.getRecipeHandler().getGrindstoneRecipes()) {
                if (event.getSlot() == 0) {
                    ItemStack input = inventory.getItem(1);
                    if (grindstoneRecipe.getInputBottom() != null && !grindstoneRecipe.getInputBottom().isEmpty()) {
                        if (ItemUtils.isAirOrNull(input)) {
                            continue;
                        }
                        for (CustomItem customItem : grindstoneRecipe.getInputBottom()) {
                            if (customItem.isSimilar(input, grindstoneRecipe.isExactMeta())) {
                                finalInputBottom = customItem.clone();
                                break;
                            }
                        }
                        if (finalInputBottom == null) {
                            continue;
                        }
                    } else if (!ItemUtils.isAirOrNull(input)) {
                        continue;
                    }
                } else {
                    ItemStack input = inventory.getItem(0);
                    if (grindstoneRecipe.getInputTop() != null && !grindstoneRecipe.getInputTop().isEmpty()) {
                        if (ItemUtils.isAirOrNull(input)) {
                            continue;
                        }
                        for (CustomItem customItem : grindstoneRecipe.getInputTop()) {
                            if (customItem.isSimilar(input, grindstoneRecipe.isExactMeta())) {
                                finalInputTop = customItem.clone();
                                break;
                            }
                        }
                        if (finalInputTop == null) {
                            continue;
                        }
                    } else if (!ItemUtils.isAirOrNull(input)) {
                        continue;
                    }
                }
                if (!grindstoneRecipe.getConditions().checkConditions(grindstoneRecipe, new Conditions.Data(player, player.getTargetBlock(null, 5), event.getView()))) {
                    continue;
                }
                foundRecipe = grindstoneRecipe;
                break;
            }
            System.out.println("Activated After recipe: " + foundRecipe);
            if (foundRecipe == null) {
                if (ItemUtils.isAirOrNull(cursor) || allowedInGrindstone(cursor.getType())) {
                    event.setCancelled(false);
                }
                return; //Returns and uses Vanilla recipe instead
            }
            RandomCollection<CustomItem> items = new RandomCollection<>();
            for (CustomItem customItem : foundRecipe.getCustomResults()) {
                if (!customItem.hasPermission() || player.hasPermission(customItem.getPermission())) {
                    items.add(customItem.getRarityPercentage(), customItem.clone());
                }
            }
            HashMap<String, CustomItem> precraftedItem = precraftedItems.getOrDefault(player, new HashMap<>());
            CustomItem result = new CustomItem(Material.AIR);
            if (precraftedItem.get(foundRecipe.getId()) == null) {
                if (!items.isEmpty()) {
                    result = items.next();
                    precraftedItem.put(foundRecipe.getId(), result);
                    precraftedItems.put(player.getUniqueId(), precraftedItem);
                }
            } else {
                result = precraftedItem.get(foundRecipe.getId());
            }
            preCraftedRecipes.put(player.getUniqueId(), new GrindstoneData(foundRecipe, finalInputTop, finalInputBottom));
            inventory.setItem(2, result.getRealItem());
             */
        }
    }

    private boolean isTool(Material material) {
        switch (material) {
            case WOODEN_AXE:
            case WOODEN_HOE:
            case WOODEN_SWORD:
            case WOODEN_SHOVEL:
            case WOODEN_PICKAXE:
            case GOLDEN_AXE:
            case GOLDEN_HOE:
            case GOLDEN_SWORD:
            case GOLDEN_SHOVEL:
            case GOLDEN_PICKAXE:
            case STONE_AXE:
            case STONE_HOE:
            case STONE_SWORD:
            case STONE_SHOVEL:
            case STONE_PICKAXE:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_SWORD:
            case IRON_SHOVEL:
            case IRON_PICKAXE:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_SWORD:
            case DIAMOND_SHOVEL:
            case DIAMOND_PICKAXE:
                return true;
            default:
                return false;
        }
    }

    private boolean allowedInGrindstone(Material material) {
        if (isTool(material)) return true;
        if (WolfyUtilities.hasVillagePillageUpdate() && material.equals(Material.CROSSBOW)) return true;
        switch (material) {
            case LEATHER_BOOTS:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case LEATHER_CHESTPLATE:
            case IRON_BOOTS:
            case IRON_HELMET:
            case IRON_LEGGINGS:
            case IRON_CHESTPLATE:
            case GOLDEN_BOOTS:
            case GOLDEN_HELMET:
            case GOLDEN_LEGGINGS:
            case GOLDEN_CHESTPLATE:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
            case DIAMOND_CHESTPLATE:
            case BOW:
            case CROSSBOW:
            case TRIDENT:
            case SHIELD:
            case TURTLE_HELMET:
            case ELYTRA:
            case CARROT_ON_A_STICK:
            case FISHING_ROD:
            case SHEARS:
            case FLINT_AND_STEEL:
                return true;
            default:
                return false;
        }
    }

}
