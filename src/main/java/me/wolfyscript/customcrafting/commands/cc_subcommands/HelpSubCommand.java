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

package me.wolfyscript.customcrafting.commands.cc_subcommands;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.commands.AbstractSubCommand;
import me.wolfyscript.customcrafting.utils.ChatUtils;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.chat.ClickData;
import me.wolfyscript.utilities.api.chat.ClickEvent;
import me.wolfyscript.utilities.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HelpSubCommand extends AbstractSubCommand {

    public HelpSubCommand(CustomCrafting customCrafting) {
        super("help", new ArrayList<>(), customCrafting);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String var3, @NotNull String[] var4) {
        if (sender instanceof Player p && ChatUtils.checkPerm(p, "customcrafting.cmd.help")) {
            printHelp(p);
        }
        return true;
    }

    @Override
    protected @Nullable List<String> onTabComplete(@NotNull CommandSender var1, @NotNull String var3, @NotNull String[] var4) {
        return null;
    }

    public void printHelp(Player p) {
        WolfyUtilities api = customCrafting.getApi();
        var chat = api.getChat();
        chat.sendMessage(p, "———————— &3&lCustomCrafting &7————————");
        chat.sendMessage(p, "");
        List<String> help = api.getLanguageAPI().replaceKey("commands.help");
        for (String line : help) {
            chat.sendMessage(p, line);
        }
        chat.sendActionMessage(p, new ClickData("&ehttps://github.com/WolfyScript/CustomCrafting-Wiki/wiki", null, new HoverEvent(HoverEvent.Action.SHOW_TEXT, "Go to Wiki"), new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/WolfyScript/CustomCrafting-Wiki/wiki")));
        chat.sendMessage(p, "—————————————————————————");
    }
}
