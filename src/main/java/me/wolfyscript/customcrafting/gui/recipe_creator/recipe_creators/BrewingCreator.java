package me.wolfyscript.customcrafting.gui.recipe_creator.recipe_creators;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.TestCache;
import me.wolfyscript.customcrafting.gui.ExtendedGuiWindow;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.BrewingContainerButton;
import me.wolfyscript.customcrafting.recipes.RecipePriority;
import me.wolfyscript.customcrafting.recipes.types.brewing.BrewingConfig;
import me.wolfyscript.customcrafting.recipes.types.brewing.BrewingRecipe;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.inventory.GuiHandler;
import me.wolfyscript.utilities.api.inventory.GuiUpdateEvent;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import me.wolfyscript.utilities.api.inventory.button.ButtonActionRender;
import me.wolfyscript.utilities.api.inventory.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.button.buttons.ActionButton;
import me.wolfyscript.utilities.api.inventory.button.buttons.ChatInputButton;
import me.wolfyscript.utilities.api.inventory.button.buttons.DummyButton;
import me.wolfyscript.utilities.api.inventory.button.buttons.ToggleButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BrewingCreator extends ExtendedGuiWindow {

    public BrewingCreator(InventoryAPI inventoryAPI, CustomCrafting customCrafting) {
        super("brewing_stand", inventoryAPI, 45, customCrafting);
    }

    @Override
    public void onInit() {
        registerButton(new ActionButton("back", new ButtonState("none", "back", WolfyUtilities.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0="), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            guiHandler.openCluster("none");
            return true;
        })));

        registerButton(new ActionButton("save", new ButtonState("recipe_creator", "save", Material.WRITABLE_BOOK, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            TestCache cache = ((TestCache) guiHandler.getCustomCache());
            if (validToSave(cache)) {
                openChat("recipe_creator", "save.input", guiHandler, (guiHandler1, player1, s, args) -> {
                    TestCache cache1 = ((TestCache) guiHandler1.getCustomCache());
                    BrewingConfig config = cache1.getBrewingConfig();
                    if (args.length > 1) {
                        if (!config.saveConfig(args[0], args[1], player1)) {
                            return true;
                        }
                        try {
                            Bukkit.getScheduler().runTaskLater(CustomCrafting.getInst(), () -> {
                                CustomCrafting.getRecipeHandler().injectRecipe(new BrewingRecipe(config));
                                api.sendPlayerMessage(player, "recipe_creator", "loading.success");
                            }, 1);
                            if (CustomCrafting.getConfigHandler().getConfig().isResetCreatorAfterSave()) {
                                cache.resetBrewingConfig();
                            }
                        } catch (Exception ex) {
                            api.sendPlayerMessage(player, "recipe_creator", "error_loading", new String[]{"%REC%", config.getId()});
                            ex.printStackTrace();
                            return false;
                        }
                        Bukkit.getScheduler().runTaskLater(CustomCrafting.getInst(), () -> guiHandler.openCluster("none"), 1);
                    }
                    return false;
                });
            } else {
                api.sendPlayerMessage(player, "recipe_creator", "save.empty");
            }
            return false;
        })));

        registerButton(new ToggleButton("hidden", new ButtonState("recipe_creator", "hidden.enabled", WolfyUtilities.getSkullViaURL("ce9d49dd09ecee2a4996965514d6d301bf12870c688acb5999b6658e1dfdff85"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().setHidden(false);
            return true;
        }), new ButtonState("recipe_creator", "hidden.disabled", WolfyUtilities.getSkullViaURL("85e5bf255d5d7e521474318050ad304ab95b01a4af0bae15e5cd9c1993abcc98"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().setHidden(true);
            return true;
        })));

        registerButton(new ToggleButton("exact_meta", new ButtonState("recipe_creator", "exact_meta.enabled", Material.GREEN_CONCRETE, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().setExactMeta(false);
            return true;
        }), new ButtonState("recipe_creator", "exact_meta.disabled", Material.RED_CONCRETE, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().setExactMeta(true);
            return true;
        })));
        registerButton(new ActionButton("priority", new ButtonState("recipe_creator", "priority", WolfyUtilities.getSkullViaURL("b8ea57c7551c6ab33b8fed354b43df523f1e357c4b4f551143c34ddeac5b6c8d"), new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int i, InventoryClickEvent inventoryClickEvent) {
                RecipePriority priority = ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().getPriority();
                int order;
                order = priority.getOrder();
                if (order < 2) {
                    order++;
                } else {
                    order = -2;
                }
                ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().setPriority(RecipePriority.getByOrder(order));
                return true;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int slot, boolean help) {
                RecipePriority priority = ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().getPriority();
                if (priority != null) {
                    hashMap.put("%PRI%", priority.name());
                }
                return itemStack;
            }
        })));
        registerButton(new DummyButton("brewing_stand", new ButtonState("brewing_stand", Material.BREWING_STAND)));
        registerButton(new ChatInputButton("brewTime", new ButtonState("brewTime", Material.CLOCK, (hashMap, guiHandler, player, itemStack, slot, help) -> {
            hashMap.put("%time%", ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().getBrewTime());
            return itemStack;
        }), (guiHandler, player, s, args) -> {
            int time;
            try {
                time = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                api.sendPlayerMessage(player, "recipe_creator", "valid_number");
                return true;
            }
            ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().setBrewTime(time <= 400 ? time : 400);
            return false;
        }));
        registerButton(new ChatInputButton("fuelCost", new ButtonState("fuelCost", Material.BLAZE_POWDER, (hashMap, guiHandler, player, itemStack, slot, help) -> {
            hashMap.put("%cost%", ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().getFuelCost());
            return itemStack;
        }), (guiHandler, player, s, args) -> {
            int cost;
            try {
                cost = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                api.sendPlayerMessage(player, "recipe_creator", "valid_number");
                return true;
            }
            ((TestCache) guiHandler.getCustomCache()).getBrewingConfig().setFuelCost(cost);
            return false;
        }));
        registerButton(new BrewingContainerButton(0));
        registerButton(new BrewingContainerButton(1));

        registerButton(new ActionButton("potion_duration", new ButtonState("potion_duration", Material.CLOCK, new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int slot, InventoryClickEvent event) {
                TestCache cache = ((TestCache) guiHandler.getCustomCache());
                BrewingConfig config = cache.getBrewingConfig();
                if (event.getClick().isRightClick()) {
                    //Change Mode
                    config.setDurationChange(0);
                    return true;
                } else {
                    //Change Value
                    openChat("potion_duration", guiHandler, (guiHandler1, player1, s, strings) -> {
                        try {
                            int value = Integer.parseInt(s);
                            config.setDurationChange(value);
                        } catch (NumberFormatException ex) {
                            api.sendPlayerMessage(player1, "recipe_creator", "valid_number");
                        }
                        return false;
                    });
                }
                return true;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int slot, boolean b) {
                TestCache cache = ((TestCache) guiHandler.getCustomCache());
                hashMap.put("%value%", cache.getBrewingConfig().getDurationChange());
                return itemStack;
            }
        })));

        registerButton(new ActionButton("potion_amplifier", new ButtonState("potion_amplifier", Material.IRON_SWORD, new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int slot, InventoryClickEvent event) {
                TestCache cache = ((TestCache) guiHandler.getCustomCache());
                BrewingConfig config = cache.getBrewingConfig();
                if (event.getClick().isRightClick()) {
                    //Change Mode
                    config.setDurationChange(0);
                    return true;
                } else {
                    //Change Value
                    openChat("potion_amplifier", guiHandler, (guiHandler1, player1, s, strings) -> {
                        try {
                            int value = Integer.parseInt(s);
                            config.setAmplifierChange(value);
                        } catch (NumberFormatException ex) {
                            api.sendPlayerMessage(player1, "recipe_creator", "valid_number");
                        }
                        return false;
                    });
                }
                return true;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int slot, boolean b) {
                TestCache cache = ((TestCache) guiHandler.getCustomCache());
                hashMap.put("%value%", cache.getBrewingConfig().getAmplifierChange());
                return itemStack;
            }
        })));

    }

    @EventHandler
    public void onUpdate(GuiUpdateEvent event) {
        if (event.verify(this)) {
            event.setButton(0, "back");
            TestCache cache = (TestCache) event.getGuiHandler().getCustomCache();
            BrewingConfig brewingConfig = cache.getBrewingConfig();
            ((ToggleButton) event.getGuiWindow().getButton("hidden")).setState(event.getGuiHandler(), brewingConfig.isHidden());

            event.setButton(1, "hidden");
            event.setButton(3, "recipe_creator", "conditions");
            event.setButton(5, "priority");
            event.setButton(7, "exact_meta");

            event.setButton(13, "brewing.container_0");

            event.setButton(22, "brewing_stand");
            event.setButton(21, "brewTime");

            event.setButton(23, "fuelCost");
            event.setButton(31, "brewing.container_1");

            event.setButton(33, "potion_duration");
            event.setButton(34, "potion_amplifier");

            event.setButton(44, "save");
        }
    }

    private boolean validToSave(TestCache cache) {
        BrewingConfig config = cache.getBrewingConfig();
        return config.getIngredient() != null && !config.getIngredient().isEmpty();
    }
}