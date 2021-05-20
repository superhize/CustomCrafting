package me.wolfyscript.customcrafting.utils.recipe_item;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.Registry;
import me.wolfyscript.customcrafting.utils.recipe_item.extension.ResultExtension;
import me.wolfyscript.customcrafting.utils.recipe_item.modifications.Modification;
import me.wolfyscript.customcrafting.utils.recipe_item.target.EmptyTarget;
import me.wolfyscript.customcrafting.utils.recipe_item.target.ResultTarget;
import me.wolfyscript.utilities.api.inventory.custom_items.CustomItem;
import me.wolfyscript.utilities.api.inventory.custom_items.references.APIReference;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.annotation.*;
import me.wolfyscript.utilities.libraries.com.fasterxml.jackson.databind.JsonNode;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.RandomCollection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result<T extends ResultTarget> extends RecipeItemStack {

    @JsonIgnore
    private final Map<UUID, CustomItem> cachedItems = new HashMap<>();
    private T target;
    /**
     * Extensions are executed when the craft process is completed and allow to execute code.
     * Default Extensions are the {@link me.wolfyscript.customcrafting.utils.recipe_item.extension.CommandResultExtension}, {@link me.wolfyscript.customcrafting.utils.recipe_item.extension.SoundResultExtension}, and {@link me.wolfyscript.customcrafting.utils.recipe_item.extension.MythicMobResultExtension}
     */
    private final List<ResultExtension> extensions;
    /**
     * Modifications allow to modify the result depending on the ingredients.
     * This makes it possible to modify the NBT of the result, completely replace the result ItemStack, etc.
     */
    private final List<Modification> modifications;

    @JsonCreator
    private Result(@JsonProperty("target") T target, @JsonProperty("modifications") List<Modification> modifications, @JsonProperty("extensions") List<JsonNode> extensionNodes) {
        super();
        this.target = target;
        this.modifications = modifications;
        this.extensions = extensionNodes.stream().map(node -> {
            NamespacedKey key = NamespacedKey.of(node.path("key").asText());
            ResultExtension.Provider<?> provider = Registry.RESULT_EXTENSIONS.get(key);
            if (provider != null) {
                ResultExtension extension = provider.parse(node);
                if (extension != null) {
                    return extension;
                }
            }
            CustomCrafting.inst().getLogger().log(Level.WARNING, "Failed to load Result Extension \"{0}\"", key);
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Result() {
        super();
        this.modifications = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    public Result(Result<T> result) {
        super(result);
        this.extensions = result.extensions.stream().map(ResultExtension::clone).collect(Collectors.toList());
        this.modifications = result.modifications.stream().map(Modification::clone).collect(Collectors.toList());
        this.target = result.target;
    }

    public Result(Material... materials) {
        super(materials);
        this.modifications = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    public Result(ItemStack... items) {
        super(items);
        this.modifications = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    public Result(NamespacedKey... tags) {
        super(tags);
        this.modifications = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    public Result(APIReference... references) {
        super(references);
        this.modifications = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    public Result(List<APIReference> references, Set<NamespacedKey> tags) {
        super(references, tags);
        this.modifications = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    @Override
    public Result<T> clone() {
        return new Result<>(this);
    }

    public void setTarget(T target) {
        this.target = target;
    }

    public T getTarget() {
        return target;
    }

    public List<ResultExtension> getExtensions() {
        return extensions;
    }

    public void addExtension(ResultExtension extension) {
        this.extensions.add(extension);
    }

    public void removeExtension(ResultExtension extension) {
        this.extensions.remove(extension);
    }

    public void removeExtension(int index) {
        this.extensions.remove(index);
    }

    public List<Modification> getModifications() {
        return modifications;
    }

    public RandomCollection<CustomItem> getRandomChoices(@Nullable Player player) {
        return (player == null ? getChoices() : getChoices(player)).stream().collect(RandomCollection.getCollector((rdmCollection, customItem) -> rdmCollection.add(customItem.getRarityPercentage(), customItem.clone())));
    }

    public Result<?> get(@Nullable ItemStack[] ingredients) {
        Optional<Result<EmptyTarget>> targetResult = target == null ? Optional.empty() : target.get(ingredients);
        if (targetResult.isPresent()) {
            return targetResult.get();
        }
        return this;
    }

    public Optional<CustomItem> getItem(@Nullable Player player) {
        CustomItem item = cachedItems.computeIfAbsent(player == null ? null : player.getUniqueId(), uuid -> getRandomChoices(player).next());
        addCachedItem(player, item);
        return Optional.ofNullable(item);
    }

    @JsonIgnore
    public Optional<CustomItem> getItem() {
        return getItem(null);
    }

    private void addCachedItem(Player player, CustomItem customItem) {
        if (player != null) {
            if (customItem == null) {
                cachedItems.remove(player.getUniqueId());
            } else {
                cachedItems.put(player.getUniqueId(), customItem);
            }
        }
    }

    public void removeCachedItem(Player player) {
        if (player != null) {
            if (target != null) {
                target.clearCachedItems(player);
            }
            cachedItems.remove(player.getUniqueId());
        }
    }

    public void executeExtensions(@NotNull Location location, boolean isWorkstation, @Nullable Player player) {
        Bukkit.getScheduler().runTaskLater(CustomCrafting.inst(), () -> extensions.forEach(resultExtension -> resultExtension.onCraft(location, isWorkstation, player)), 2);
    }
}
