/*
 *       ____ _  _ ____ ___ ____ _  _ ____ ____ ____ ____ ___ _ _  _ ____
 *       |    |  | [__   |  |  | |\/| |    |__/ |__| |___  |  | |\ | | __
 *       |___ |__| ___]  |  |__| |  | |___ |  \ |  | |     |  | | \| |__]
 *
 *       CustomCrafting Recipe creation and management tool for Minecraft
 *                      Copyright (C) 2021  WolfyScript
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.wolfyscript.customcrafting.utils.other_plugins;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.wrappers.MinecraftKey;
import me.wolfyscript.customcrafting.CCRegistry;
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import me.wolfyscript.customcrafting.recipes.ICustomVanillaRecipe;
import me.wolfyscript.customcrafting.utils.NamespacedKeyUtils;
import me.wolfyscript.utilities.util.NamespacedKey;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProtocolLib {

    private final CustomCrafting plugin;
    private final ProtocolManager protocolManager;

    public ProtocolLib(CustomCrafting plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        init();
    }

    private void init() {
        registerServerSide();
    }

    private void registerServerSide() {
        Function<MinecraftKey, Boolean> recipeFilter = minecraftKey -> {
            if (minecraftKey.getPrefix().equals(NamespacedKeyUtils.NAMESPACE)) {
                CustomRecipe<?> recipe = CCRegistry.RECIPES.get(NamespacedKeyUtils.toInternal(NamespacedKey.of(minecraftKey.getFullKey())));
                if (recipe instanceof ICustomVanillaRecipe<?> vanillaRecipe && vanillaRecipe.isVisibleVanillaBook()) {
                    return !recipe.isHidden() && !recipe.isDisabled();
                }
                return false;
            }
            return true;
        };
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGH, PacketType.Play.Server.RECIPES) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                List<MinecraftKey> recipes = null;
                List<MinecraftKey> highlightedRecipes = null;
                try {
                    recipes = (List<MinecraftKey>) FieldUtils.readField(packet.getHandle(), "b", true);
                    highlightedRecipes = (List<MinecraftKey>) FieldUtils.readField(packet.getHandle(), "c", true);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if(recipes != null) {
                    try {
                        FieldUtils.writeField(packet.getHandle(), "b", recipes.stream().filter(recipeFilter::apply).collect(Collectors.toList()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                if(highlightedRecipes != null) {
                    try {
                        FieldUtils.writeField(packet.getHandle(), "c", highlightedRecipes.stream().filter(recipeFilter::apply).collect(Collectors.toList()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

}