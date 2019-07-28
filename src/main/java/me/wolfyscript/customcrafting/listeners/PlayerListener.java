package me.wolfyscript.customcrafting.listeners;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.PlayerCache;
import me.wolfyscript.customcrafting.recipes.furnace.CustomFurnaceRecipe;
import me.wolfyscript.customcrafting.utils.ChatUtils;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import me.wolfyscript.utilities.api.utils.chat.ClickData;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.BaseComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecutterInventory;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.material.Openable;
import org.yaml.snakeyaml.constructor.BaseConstructor;

import java.util.List;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        WolfyUtilities api = CustomCrafting.getApi();
        if(!CustomCrafting.isLoaded()){
            api.sendPlayerMessage(player, "$msg.player.error.loading.msg$");
            api.sendPlayerMessage(player, "$msg.player.error.loading.msg1$");
            api.sendPlayerMessage(player, "$msg.player.error.loading.msg2$");
            api.sendPlayerMessage(player, "$msg.player.error.loading.msg3$");
            api.sendActionMessage(player,
                    new ClickData("$msg.player.error.loading.msg4$", null, new me.wolfyscript.utilities.api.utils.chat.ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/wiki/buildtools/")),
                    new ClickData("$msg.player.error.loading.msg5$", null, new me.wolfyscript.utilities.api.utils.chat.ClickEvent(ClickEvent.Action.OPEN_URL, "https://papermc.io/downloads")));
            api.sendPlayerMessage(player, "");
            api.sendPlayerMessage(player, "$msg.player.error.loading.msg6$");
        }

        for(CustomFurnaceRecipe customFurnaceRecipe : CustomCrafting.getRecipeHandler().getFurnaceRecipes()){
            player.undiscoverRecipe(new NamespacedKey(customFurnaceRecipe.getId().split(":")[0], customFurnaceRecipe.getId().split(":")[1]));
        }
        if(!CustomCrafting.hasPlayerCache(player)){
            CustomCrafting.getApi().sendConsoleMessage("Initializing new cache for "+player.getDisplayName());
            CustomCrafting.renewPlayerCache(player);
        }
        InventoryAPI inventoryAPI = CustomCrafting.getApi().getInventoryAPI();
        if((player.isOp() || player.hasPermission("customcrafting.*") || player.hasPermission("customcrafting.update_check"))) {
            if (CustomCrafting.isOutdated()) {
                api.sendPlayerMessage(player, "$msg.player.outdated.msg$");
                api.sendActionMessage(player, new ClickData("$msg.player.outdated.msg2$", null), new ClickData("$msg.player.outdated.link$", null, new me.wolfyscript.utilities.api.utils.chat.ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/55883/")));
            } else {
                CustomCrafting.checkUpdate(player);
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event){
        if(event.hasItem()){
            ItemStack itemStack = event.getItem();
            if(itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()){
                List<String> lore = itemStack.getItemMeta().getLore();
                String lastLine = WolfyUtilities.unhideString(lore.get(lore.size()-1));
                if(lastLine.equals("cc_knowledgebook")){
                    Player p = event.getPlayer();
                    event.setUseItemInHand(Event.Result.DENY);
                    if(event.hasBlock()){
                        if(event.getClickedBlock().getType().isInteractable()){
                            return;
                        }
                    }
                    if (ChatUtils.checkPerm(event.getPlayer(), "customcrafting.item.knowledge_book")) {
                        if (CustomCrafting.getApi().getInventoryAPI().hasGuiHandler(p)) {
                            CustomCrafting.getApi().getInventoryAPI().getGuiHandler(p).changeToInv("recipe_book");
                        }else{
                            CustomCrafting.getApi().getInventoryAPI().openGui(p, "recipe_book");
                        }
                    }
                }
            }
        }
    }

}