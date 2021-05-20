package me.wolfyscript.customcrafting.utils.recipe_item.modifications;

import me.wolfyscript.customcrafting.Registry;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.annotation.JsonAutoDetect;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.annotation.JsonCreator;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.annotation.JsonProperty;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.JsonNode;
import me.wolfyscript.utilities.util.Keyed;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.json.jackson.JacksonUtil;
import org.bukkit.inventory.ItemStack;

public class CustomMod extends Modification {

    private final Settings settings;

    public CustomMod() {
        super();
        this.settings = null;
    }

    @JsonCreator
    public CustomMod(@JsonProperty("settings") JsonNode settingsNode, @JsonProperty("slots") int[] slots) {
        super(slots);
        NamespacedKey key = NamespacedKey.of(settingsNode.path("key").asText());
        Settings.Provider<?> provider = Registry.RESULT_MODIFICATIONS.get(key);
        if (provider != null) {
            this.settings = provider.parse(settingsNode);
        } else {
            this.settings = null;
        }
    }

    public CustomMod(Settings settings, int[] slots) {
        super(slots);
        this.settings = settings;
    }

    public CustomMod(CustomMod customMod) {
        super(customMod);
        this.settings = customMod.settings.clone();
    }

    @JsonProperty("settings")
    public Settings getSettings() {
        return settings;
    }

    @Override
    public ItemStack apply(ItemStack[] ingredients, ItemStack result) {
        if (settings != null) {
            return settings.apply(ingredients, result);
        }
        return result;
    }

    @Override
    public CustomMod clone() {
        return new CustomMod(this);
    }

    /**
     * Allows to be extended and registered into {@link Registry#RESULT_MODIFICATIONS} for custom result modifications!
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public abstract static class Settings {

        private final NamespacedKey key;

        protected Settings(NamespacedKey key) {
            this.key = key;
        }

        protected Settings(Settings settings) {
            this.key = settings.key;
        }

        public abstract ItemStack apply(ItemStack[] ingredients, ItemStack result);

        public NamespacedKey getKey() {
            return key;
        }

        public abstract Settings clone();

        /**
         * Provider is used to construct the correct type of Settings from JSON.
         * It is not necessary to extend this class. However, It is possible, if for example more custom parsing is required.
         *
         * @param <M> The type of the {@link CustomMod.Settings}
         */
        public static class Provider<M extends CustomMod.Settings> implements Keyed {

            private final NamespacedKey namespacedKey;
            private final Class<M> settingsType;

            public Provider(NamespacedKey namespacedKey, Class<M> settingsType) {
                this.namespacedKey = namespacedKey;
                this.settingsType = settingsType;
            }

            public M parse(JsonNode node) {
                return JacksonUtil.getObjectMapper().convertValue(node, settingsType);
            }

            @Override
            public NamespacedKey getNamespacedKey() {
                return namespacedKey;
            }
        }

    }

}
