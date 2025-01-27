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

package me.wolfyscript.customcrafting.gui.main_gui;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.data.patreon.Patron;
import me.wolfyscript.customcrafting.gui.CCWindow;
import me.wolfyscript.utilities.api.chat.ClickData;
import me.wolfyscript.utilities.api.chat.ClickEvent;
import me.wolfyscript.utilities.api.chat.HoverEvent;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.gui.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.gui.button.buttons.ActionButton;
import me.wolfyscript.utilities.util.inventory.PlayerHeadUtils;

import java.util.List;

public class MenuPatrons extends CCWindow {

    public MenuPatrons(GuiCluster<CCCache> guiCluster, CustomCrafting customCrafting) {
        super(guiCluster, "patrons_menu", 54, customCrafting);
    }

    @Override
    public void onInit() {
        registerButton(new ActionButton<>("back", new ButtonState<>(ClusterMain.BACK, PlayerHeadUtils.getViaURL("864f779a8e3ffa231143fa69b96b14ee35c16d669e19c75fd1a7da4bf306c"), (cache, guiHandler, player, inventory, slot, event) -> {
            guiHandler.openPreviousWindow();
            return true;
        })));
        registerButton(new ActionButton<>("patreon", PlayerHeadUtils.getViaURL("5693b66a595f78af3f51f4efa4c13375b1b958e6f4c507a47c4fe565cc275"), (cache, guiHandler, player, inventory, slot, event) -> {
            api.getBookUtil().openBook(player, false,
                    new ClickData[]{
                            new ClickData(
                                    "&8[&cBecome a Patron&8]\n\n\n\n\n",
                                    null,
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, "§7Goto WolfyScript's Patreon"),
                                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/wolfyscript")
                            ),
                            new ClickData(
                                    "\n&8[&cBack&8]\n",
                                    null,
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, "§7Back to Patreon Menu"),
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cc")),
                    });
            return true;
        }));
    }

    @Override
    public void onUpdateAsync(GuiUpdate<CCCache> event) {
        super.onUpdateAsync(event);
        event.setButton(0, "back");
        List<Patron> patronList = customCrafting.getPatreon().getPatronList();

        for (int i = 0, j = 9; i < patronList.size() && j < getSize(); i++, j += 1) {
            event.setItem(j, patronList.get(i).getHead());
        }
    }

}
