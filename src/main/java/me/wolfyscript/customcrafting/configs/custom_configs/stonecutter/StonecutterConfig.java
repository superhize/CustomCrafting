package me.wolfyscript.customcrafting.configs.custom_configs.stonecutter;

import me.wolfyscript.customcrafting.configs.custom_configs.CustomConfig;
import me.wolfyscript.customcrafting.items.CustomItem;
import me.wolfyscript.utilities.api.config.ConfigAPI;

public class StonecutterConfig extends CustomConfig {

    public StonecutterConfig(ConfigAPI configAPI, String folder, String name, String fileType) {
        super(configAPI, folder, "stonecutter", name, "stonecutter", fileType);
    }

    public StonecutterConfig(ConfigAPI configAPI, String folder, String name) {
        this(configAPI, folder, name, "yml");
    }

    public void setSource(CustomItem source) {
        saveCustomItem("source", source);
    }

    public CustomItem getSource() {
        return getCustomItem("source");
    }

    public void setResult(CustomItem result) {
        saveCustomItem("result", result);
    }

    public CustomItem getResult() {
        return getCustomItem("result");
    }
}