package com.github.rfsmassacre.heavenquests.quests;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.BiomeKeys;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
public class Quest
{
    private static final Set<EntityType> IMPOSSIBLE_ENTITIES = Set.of(EntityType.ILLUSIONER,
            EntityType.GIANT,
            EntityType.ZOMBIE_HORSE,
            EntityType.WITHER,
            EntityType.ENDER_DRAGON,
            EntityType.ELDER_GUARDIAN);

    public static Quest generateQuest(Objective objective)
    {
        PaperConfiguration config = HeavenQuests.getInstance().getConfiguration();
        int min = config.getInt("objectives." + objective.toString().toLowerCase() + ".min");
        int max = config.getInt("objectives." + objective.toString().toLowerCase() + ".max");
        return new Quest(objective, new SecureRandom().nextInt(min, max + 1));
    }

    public static Map<Objective, Quest> generateQuests()
    {
        Map<Objective, Quest> quests = new HashMap<>();
        for (Objective objective : Objective.values())
        {
            quests.put(objective, generateQuest(objective));
        }

        return quests;
    }

    private static double getMultiplier(Objective objective, String data)
    {
        String type = null;
        switch (objective)
        {
            case KILL_ANIMAL, KILL_MONSTER, TAME, FISH -> type = "entities";
            case BREAK_BLOCK, HARVEST, ENCHANT, CRAFT, SMELT, COOK -> type = "materials";
        }

        if (type == null)
        {
            return 1.0;
        }

        PaperConfiguration config = HeavenQuests.getInstance().getConfiguration();
        ConfigurationSection section = config.getSection("multipliers." + type);
        double multiplier = 1.0;
        if (section != null)
        {
            for (String key : section.getKeys(false))
            {
                if (data.toLowerCase().contains(key.toLowerCase()))
                {
                    multiplier *= section.getDouble(key);
                }
            }
        }

        return multiplier;
    }

    @Getter
    public enum Objective
    {
        KILL_MONSTER(Arrays.stream(EntityType.values())
                .filter((entityType) -> entityType.getEntityClass() != null &&
                        Monster.class.isAssignableFrom(entityType.getEntityClass())
                        && !IMPOSSIBLE_ENTITIES.contains(entityType))
                .toArray()),
        KILL_ANIMAL(Arrays.stream(EntityType.values())
                .filter((entityType) -> entityType.getEntityClass() != null &&
                        Animals.class.isAssignableFrom(entityType.getEntityClass())
                        && !IMPOSSIBLE_ENTITIES.contains(entityType))
                .toArray()),
        BREAK_BLOCK(getNaturalBlocks()),
        ENCHANT(List.of(
                Material.NETHERITE_SWORD,
                Material.DIAMOND_SWORD,
                Material.IRON_SWORD,
                Material.GOLDEN_SWORD,
                Material.STONE_SWORD,
                Material.WOODEN_SWORD,
                Material.NETHERITE_AXE,
                Material.DIAMOND_AXE,
                Material.IRON_AXE,
                Material.GOLDEN_AXE,
                Material.STONE_AXE,
                Material.WOODEN_AXE,
                Material.NETHERITE_PICKAXE,
                Material.DIAMOND_PICKAXE,
                Material.IRON_PICKAXE,
                Material.GOLDEN_PICKAXE,
                Material.STONE_PICKAXE,
                Material.WOODEN_PICKAXE,
                Material.NETHERITE_SHOVEL,
                Material.DIAMOND_SHOVEL,
                Material.IRON_SHOVEL,
                Material.GOLDEN_SHOVEL,
                Material.STONE_SHOVEL,
                Material.WOODEN_SHOVEL,
                Material.BOW,
                Material.CROSSBOW,
                Material.TRIDENT,
                Material.FISHING_ROD,
                Material.NETHERITE_HELMET,
                Material.DIAMOND_HELMET,
                Material.IRON_HELMET,
                Material.GOLDEN_HELMET,
                Material.CHAINMAIL_HELMET,
                Material.LEATHER_HELMET,
                Material.NETHERITE_CHESTPLATE,
                Material.DIAMOND_CHESTPLATE,
                Material.IRON_CHESTPLATE,
                Material.GOLDEN_CHESTPLATE,
                Material.CHAINMAIL_CHESTPLATE,
                Material.LEATHER_CHESTPLATE,
                Material.NETHERITE_LEGGINGS,
                Material.DIAMOND_LEGGINGS,
                Material.IRON_LEGGINGS,
                Material.GOLDEN_LEGGINGS,
                Material.CHAINMAIL_LEGGINGS,
                Material.LEATHER_LEGGINGS,
                Material.NETHERITE_BOOTS,
                Material.DIAMOND_BOOTS,
                Material.IRON_BOOTS,
                Material.GOLDEN_BOOTS,
                Material.CHAINMAIL_BOOTS,
                Material.LEATHER_BOOTS,
                Material.ELYTRA,
                Material.BOOK
        )),
        FISH(getFish()),
        VISIT_BIOME(getBiomes()),
        CRAFT(getCraftingRecipes()),
        SMELT(getSmeltingRecipes()),
        COOK(getCookingRecipes()),
        TAME(Arrays.stream(EntityType.values())
                .filter((entityType) -> entityType.getEntityClass() != null &&
                        Tameable.class.isAssignableFrom(entityType.getEntityClass()) &&
                        !IMPOSSIBLE_ENTITIES.contains(entityType))
                .toArray()),
        HARVEST(new ArrayList<>(List.of(Material.WHEAT,
                Material.CARROT,
                Material.POTATO,
                Material.BEETROOT,
                Material.MELON_SLICE,
                Material.PUMPKIN,
                Material.SUGAR_CANE,
                Material.CACTUS,
                Material.COCOA_BEANS,
                Material.NETHER_WART,
                Material.BAMBOO,
                Material.KELP,
                Material.SWEET_BERRIES,
                Material.CHORUS_PLANT)));

        private final List<Object> datas;

        Objective(List<Object> datas)
        {
            this.datas = new ArrayList<>(datas);
        }

        Objective(Object[] datas)
        {
            this(List.of(datas));
        }

        public List<String> getDataString()
        {
            List<String> dataString = new ArrayList<>();
            for (Object data : datas)
            {
                if (data instanceof Enum<?> enumeration)
                {
                    dataString.add(enumeration.name());
                }
                else if (data instanceof Keyed keyed)
                {
                    dataString.add(keyed.key().asString());
                }
                else
                {
                    dataString.add(data.toString());
                }
            }

            return dataString;
        }

        public Object getRandomData()
        {
            Map<Object, Double> weights = new HashMap<>();
            double totalWeight = 0.0;
            for (Object data : datas)
            {
                if (!isValid(data))
                {
                    continue;
                }

                double weight;
                if (data instanceof Keyed keyed)
                {
                    weight = 1.0 / Math.max(1.0, getMultiplier(this, keyed.key().asString()));
                }
                else
                {
                    weight = 1.0 / Math.max(1.0, getMultiplier(this, data.toString()));
                }

                weights.put(data, weight);
                totalWeight += weight;
            }

            double random = totalWeight <= 0.0 ? 0.0 : new SecureRandom().nextDouble(totalWeight);
            double cumulativeWeight = 0.0;
            for (Map.Entry<Object, Double> entry : weights.entrySet())
            {
                cumulativeWeight += entry.getValue();
                if (random <= cumulativeWeight)
                {
                    return entry.getKey();
                }
            }

            return null;
        }

        private boolean isValid(Object data)
        {
            PaperConfiguration config = HeavenQuests.getInstance().getConfiguration();
            switch (data)
            {
                case Material material ->
                {
                    List<String> blackListedMaterials = config.getStringList("blacklist.materials");
                    for (String materialName : blackListedMaterials)
                    {
                        if (material.name().contains(materialName.toUpperCase()))
                        {
                            return false;
                        }
                    }
                }
                case EntityType entityType ->
                {
                    List<String> blackListedEntities = config.getStringList("blacklist.entities");
                    for (String entityName : blackListedEntities)
                    {
                        if (entityType.name().contains(entityName.toUpperCase()))
                        {
                            return false;
                        }
                    }
                }
                case CraftingRecipe recipe ->
                {
                    List<String> blackListedMaterials = config.getStringList("blacklist.materials");
                    for (String materialName : blackListedMaterials)
                    {
                        if (recipe.getResult().getType().toString().contains(materialName.toUpperCase()))
                        {
                            return false;
                        }
                    }
                }
                case CookingRecipe<?> recipe ->
                {
                    List<String> blackListedMaterials = config.getStringList("blacklist.materials");
                    for (String materialName : blackListedMaterials)
                    {
                        if (recipe.getInputChoice().getItemStack().getType().toString()
                                .contains(materialName.toUpperCase()))
                        {
                            return false;
                        }
                    }
                }
                default ->
                {
                    if (Bukkit.getPluginManager().isPluginEnabled("CustomFishing") && this.equals(Objective.FISH))
                    {
                        List<String> blacklistedFish = config.getStringList("blacklist.custom-fishing");
                        if (!BukkitCustomFishingPlugin.getInstance().getItemManager().getItemIDs()
                                .contains(data.toString()))
                        {
                            return false;
                        }

                        for (String fish : blacklistedFish)
                        {
                            if (data.toString().toLowerCase().contains(fish.toLowerCase()))
                            {
                                return false;
                            }
                        }
                    }
                }
            }

            return true;
        }

        public static Objective fromString(String name)
        {
            try
            {
                return Objective.valueOf(name.toUpperCase());
            }
            catch (IllegalArgumentException exception)
            {
                return null;
            }
        }

        public static List<Object> getNaturalBlocks()
        {
            List<Object> materials = new ArrayList<>(Arrays.stream(Material.values())
                    .filter((material) -> material.isBlock() && material.isSolid() && material.isItem())
                    .toList());
            List<CraftingRecipe> recipes = getCraftingRecipes().stream()
                    .map((recipe) -> (CraftingRecipe) recipe)
                    .toList();
            for (CraftingRecipe recipe : recipes)
            {
                materials.remove(recipe.getResult().getType());
            }

            return materials;
        }

        private static List<Object> getCraftingRecipes()
        {
            List<Object> recipes = new ArrayList<>();
            Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();
            while (iterator.hasNext())
            {
                Recipe recipe = iterator.next();
                if (recipe instanceof CraftingRecipe craftingRecipe)
                {
                    recipes.add(craftingRecipe);
                }
            }

            return recipes;
        }

        private static List<Object> getSmeltingRecipes()
        {
            List<Object> recipes = new ArrayList<>();
            Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();
            while (iterator.hasNext())
            {
                Recipe recipe = iterator.next();
                if (recipe instanceof CookingRecipe<?> cookingRecipe &&
                        !cookingRecipe.getCategory().equals(CookingBookCategory.FOOD))
                {
                    recipes.add(cookingRecipe);
                }
            }

            return recipes;
        }

        private static List<Object> getCookingRecipes()
        {
            List<Object> recipes = new ArrayList<>();
            Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();
            while (iterator.hasNext())
            {
                Recipe recipe = iterator.next();
                if (recipe instanceof CookingRecipe<?> cookingRecipe &&
                        cookingRecipe.getCategory().equals(CookingBookCategory.FOOD))
                {
                    recipes.add(cookingRecipe);
                }
            }

            return recipes;
        }

        public static List<Object> getFish()
        {
            if (!Bukkit.getPluginManager().isPluginEnabled("CustomFishing"))
            {
                return List.of(EntityType.COD,
                        EntityType.SALMON,
                        EntityType.PUFFERFISH,
                        EntityType.TROPICAL_FISH);
            }

            List<String> blacklist = HeavenQuests.getInstance().getConfiguration()
                    .getStringList("blacklist.custom-fishing");
            return new ArrayList<>(BukkitCustomFishingPlugin.getInstance().getItemManager().getItemIDs().stream()
                    .filter((id) ->
                    {
                        for (String blacklistedId : blacklist)
                        {
                            if (id.toLowerCase().contains(blacklistedId.toLowerCase()))
                            {
                                return false;
                            }
                        }

                        return true;
                    })
                    .toList());
        }

        private static List<Object> getBiomes()
        {
            List<String> blacklist = HeavenQuests.getInstance().getConfiguration()
                    .getStringList("blacklist.biomes");
            return new ArrayList<>(Registry.BIOME.stream()
                    .filter((biome) ->
                    {
                        for (String blacklistedBiome : blacklist)
                        {
                            if (biome.key().asString().toLowerCase().contains(blacklistedBiome.toLowerCase()))
                            {
                                return false;
                            }
                        }

                        return true;
                    })
                    .toList());
        }
    }

    private final Objective objective;
    private final String data;
    private int max;
    private int amount;
    @Setter
    private long timeCompleted;

    public Quest(Objective objective, String data, int max)
    {
        this.objective = objective;
        this.data = data;
        this.amount = 0;
        this.max = max;
    }

    public Quest(Objective objective, int max)
    {
        this.objective = objective;
        this.amount = 0;
        this.max = max;
        Object randomData = objective.getRandomData();
        switch (randomData)
        {
            case Material material -> this.data = material.toString();
            case EntityType entityType ->
            {
                this.data = entityType.toString();
                if (max > 1)
                {
                    this.max = Math.max(1, (int) Math.round(this.max / getMultiplier()));
                }
            }
            case CraftingRecipe recipe ->
            {
                this.data = recipe.key().asString();
                this.max *= recipe.getResult().getAmount();
            }
            case CookingRecipe<?> recipe -> this.data = recipe.key().asString();
            case Biome biome -> this.data = biome.key().asString();
            case null -> this.data = null;
            default -> this.data = randomData.toString();
        }
    }

    public boolean isData(String data)
    {
        try
        {
            switch (objective)
            {
                case CRAFT ->
                {
                    Material material = Material.valueOf(data);
                    return ((CraftingRecipe) getData()).getResult().getType().equals(material);
                }
                case SMELT, COOK ->
                {
                    Material material = Material.valueOf(data);
                    return ((CookingRecipe<?>) getData()).getInputChoice().getItemStack().getType().equals(material);
                }
                case VISIT_BIOME ->
                {
                    return this.data.contains(data);
                }
                default ->
                {
                    return this.data.equals(data);
                }
            }
        }
        catch (IllegalArgumentException | ClassCastException exception)
        {
            return this.data.equals(data);
        }
    }

    public Object getData()
    {
        switch (objective)
        {
            case FISH ->
            {
                if (Bukkit.getPluginManager().isPluginEnabled("CustomFishing"))
                {
                    return BukkitCustomFishingPlugin.getInstance().getItemManager().buildInternal(Context.player(null),
                            data);
                }
                else
                {
                    try
                    {
                        return EntityType.valueOf(data);
                    }
                    catch (IllegalStateException exception)
                    {
                        //Do nothing
                    }
                }
            }
            case KILL_ANIMAL, KILL_MONSTER, TAME ->
            {
                try
                {
                    return EntityType.valueOf(data);
                }
                catch (IllegalStateException exception)
                {
                    //Do nothing
                }
            }
            case BREAK_BLOCK, HARVEST, ENCHANT ->
            {
                try
                {
                    return Material.valueOf(data);
                }
                catch (IllegalStateException exception)
                {
                    //Do nothing
                }
            }
            case CRAFT ->
            {
                NamespacedKey key = NamespacedKey.fromString(data);
                if (key != null)
                {
                    CraftingRecipe recipe = (CraftingRecipe) Bukkit.getRecipe(key);
                    if (recipe != null)
                    {
                        return recipe;
                    }
                }
            }
            case SMELT, COOK ->
            {
                NamespacedKey key = NamespacedKey.fromString(data);
                if (key != null)
                {
                    CookingRecipe<?> recipe = (CookingRecipe<?>) Bukkit.getRecipe(key);
                    if (recipe != null)
                    {
                        return recipe;
                    }
                }
            }
            case VISIT_BIOME ->
            {
                return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(Key.key(data));
            }
        }

        return null;
    }

    public double getPrize()
    {
        PaperConfiguration config = HeavenQuests.getInstance().getConfiguration();
        return config.getDouble("objectives." + objective.toString().toLowerCase() + ".prize") * max *
                getMultiplier();
    }

    public String getDisplayName()
    {
        String displayName;
        switch (objective)
        {
            case KILL_MONSTER ->
            {
                displayName = LocaleData.capitalize(objective.toString().replace("_MONSTER", "")) +
                        " " + max + " " + LocaleData.capitalize(getData().toString());
            }
            case KILL_ANIMAL ->
            {
                displayName = LocaleData.capitalize(objective.toString().replace("_ANIMAL", "")) +
                        " " + max + " " + LocaleData.capitalize(getData().toString());
            }
            case BREAK_BLOCK ->
            {
                displayName = LocaleData.capitalize(objective.toString().replace("_BLOCK", "")) +
                        " " + max + " " + LocaleData.capitalize(getData().toString());
            }

            case SMELT, COOK ->
            {
                displayName = LocaleData.capitalize(objective + " " + max + " " +
                        LocaleData.capitalize(((CookingRecipe<?>) getData()).getInputChoice().getItemStack()
                                .getType().toString()));
            }
            case CRAFT ->
            {
                displayName = LocaleData.capitalize(objective + " " + max + " " +
                        LocaleData.capitalize(((CraftingRecipe) getData()).getResult().getType().toString()));
            }
            case VISIT_BIOME ->
            {
                displayName = "Visit " + LocaleData.capitalize(((Biome) getData()).key().value());
            }
            default ->
            {
                displayName = LocaleData.capitalize(objective.toString()) + " " + max + " " +
                        LocaleData.capitalize(getData().toString());
            }
        }

        return LocaleData.format(displayName);
    }

    public String getIconDisplayName()
    {
        if (isComplete())
        {
            return getDisplayName() + LocaleData.format(" &4(&c&lCOMPLETED&4)");
        }

        String displayName;
        switch (objective)
        {
            case KILL_MONSTER ->
            {
                displayName = LocaleData.capitalize(objective.toString().replace("_MONSTER", "")) +
                        " " + (max - amount) + " " + LocaleData.capitalize(data);
            }
            case KILL_ANIMAL ->
            {
                displayName = LocaleData.capitalize(objective.toString().replace("_ANIMAL", "")) +
                        " " + (max - amount) + " " + LocaleData.capitalize(data);
            }
            case BREAK_BLOCK ->
            {
                displayName = LocaleData.capitalize(objective.toString().replace("_BLOCK", "")) +
                        " " + (max - amount) + " " + LocaleData.capitalize(data);
            }
            case SMELT, COOK ->
            {
                displayName = LocaleData.capitalize(objective + " " + (max - amount) + " " +
                        LocaleData.capitalize(((CookingRecipe<?>) getData()).getInputChoice().getItemStack()
                                .getType().toString()));
            }
            case CRAFT ->
            {
                displayName = LocaleData.capitalize(objective + " " + (max - amount) + " " +
                        LocaleData.capitalize(((CraftingRecipe) getData()).getResult().getType().toString()));
            }
            case VISIT_BIOME ->
            {
                displayName = "Visit " + LocaleData.capitalize(((Biome) getData()).key().value());
            }
            default ->
            {
                displayName = LocaleData.capitalize(objective.toString()) + " " + (max - amount) + " " +
                        LocaleData.capitalize(data);
            }
        }

        return LocaleData.format(displayName);
    }

    public List<String> getLore()
    {
        List<String> lore = new ArrayList<>(List.of("",
                "  &aReward: &2(&f" + new DecimalFormat("#,###.##").format(getPrize()) + " ꐘ&2)",
                ""));
        return LocaleData.formatLore(lore);
    }

    public ItemStack getIcon()
    {
        Material material = Material.BARRIER;
        switch (objective)
        {
            case FISH ->
            {
                if (Bukkit.getPluginManager().isPluginEnabled("CustomFishing"))
                {
                    return BukkitCustomFishingPlugin.getInstance().getItemManager().buildInternal(Context.player(null),
                            data);
                }
                else
                {
                    try
                    {
                        material = Material.valueOf(data);
                    }
                    catch (IllegalArgumentException exception)
                    {
                        //Do nothing.
                    }
                }
            }
            case KILL_ANIMAL, KILL_MONSTER, TAME ->
            {
                try
                {
                    material = Material.valueOf(data + "_SPAWN_EGG");
                }
                catch (IllegalArgumentException exception)
                {
                    //Do nothing.
                }
            }
            case BREAK_BLOCK ->
            {
                try
                {
                    Material testMaterial = Material.valueOf(data);
                    if (testMaterial.isItem())
                    {
                        material = testMaterial;
                    }
                }
                catch (IllegalArgumentException exception)
                {
                    //Do nothing;
                }
            }
            case SMELT, COOK ->
            {
                material = ((CookingRecipe<?>) getData()).getInputChoice().getItemStack()
                    .getType();
            }
            case CRAFT ->
            {
                material = ((CraftingRecipe) getData()).getResult().getType();
            }
            case HARVEST ->
            {
                try
                {
                    switch (data)
                    {
                        case "POTATOES" ->
                        {
                            material = Material.POTATO;
                        }
                        case "CARROTS" ->
                        {
                            material = Material.CARROT;
                        }
                        case "BEETROOTS" ->
                        {
                            material = Material.BEETROOT;
                        }
                        default ->
                        {
                            Material testMaterial = Material.valueOf(data);
                            if (testMaterial.isItem())
                            {
                                material = testMaterial;
                            }
                        }
                    }
                }
                catch (IllegalArgumentException exception)
                {
                    //Do nothing.
                }
            }
            case VISIT_BIOME ->
            {
                material = Material.GRASS_BLOCK;
            }
            case ENCHANT ->
            {
                material = Material.ENCHANTED_BOOK;
            }
        }

        return new ItemStack(material);
    }

    public void setAmount(int amount)
    {
        this.amount = Math.max(0, Math.min(amount, max));
    }

    public void addAmount(int amount)
    {
        setAmount(this.amount + amount);
    }

    public boolean isComplete()
    {
        return amount >= max;
    }

    private double getMultiplier()
    {
        return getMultiplier(objective, data);
    }

    public long getLastCompleted(ChronoUnit unit)
    {
        return Instant.ofEpochMilli(timeCompleted).until(Instant.now(), unit);
    }

    public long getTimeLeft()
    {
        if (!isComplete())
        {
            return 0L;
        }

        PaperConfiguration config = HeavenQuests.getInstance().getConfiguration();
        int interval = config.getInt("completed-interval");
        return Math.max(0L, interval - getLastCompleted(ChronoUnit.SECONDS));
    }

    public boolean isExpired()
    {
        return getTimeLeft() <= 0L;
    }
}
