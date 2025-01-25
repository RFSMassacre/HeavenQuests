package com.github.rfsmassacre.heavenquests.quests;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;

@Getter
public class Quest
{
    private static final Set<EntityType> IMPOSSIBLE_ENTITIES = Set.of(EntityType.ILLUSIONER,
            EntityType.GIANT,
            EntityType.ZOMBIE_HORSE);

    public static Map<Objective, Quest> generateQuests()
    {
        PaperConfiguration config = HeavenQuests.getInstance().getConfiguration();
        Map<Objective, Quest> quests = new HashMap<>();
        for (Objective objective : Objective.values())
        {
            int min = config.getInt("objectives." + objective.toString().toLowerCase() + ".min");
            int max = config.getInt("objectives." + objective.toString().toLowerCase() + ".max");
            Quest quest = new Quest(objective, objective.getRandomData(), new SecureRandom().nextInt(min,
                    max + 1));
            quests.put(objective, quest);
        }

        return quests;
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
        FISH(List.of(EntityType.COD,
                EntityType.SALMON,
                EntityType.PUFFERFISH,
                EntityType.TROPICAL_FISH)),
        VISIT_BIOME(Registry.BIOME.stream().toArray()),
        CRAFT(getCraftingRecipes()),
        SMELT(getSmeltingRecipes()),
        COOK(getCookingRecipes()),
        TAME(Arrays.stream(EntityType.values())
                .filter((entityType) -> entityType.getEntityClass() != null &&
                        Tameable.class.isAssignableFrom(entityType.getEntityClass()))
                .toArray()),
        HARVEST(List.of(Material.WHEAT,
                Material.CARROTS,
                Material.POTATOES,
                Material.BEETROOTS,
                Material.MELON,
                Material.PUMPKIN,
                Material.SUGAR_CANE,
                Material.CACTUS,
                Material.COCOA,
                Material.NETHER_WART,
                Material.BAMBOO,
                Material.KELP,
                Material.SWEET_BERRIES,
                Material.CHORUS_PLANT));

        private final List<Object> datas;

        Objective(List<Object> datas)
        {
            this.datas = datas;
        }

        Objective(Object[] datas)
        {
            this.datas = List.of(datas);
        }

        public Object getRandomData()
        {
            return datas.get(new SecureRandom().nextInt(0, datas.size()));
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
    }

    private final Objective objective;
    private final String data;
    private final int max;
    private int amount;

    public Quest(Objective objective, Object data, int max)
    {;
        this.objective = objective;
        this.amount = 0;
        switch (data)
        {
            case Material material -> this.data = material.name();
            case EntityType entityType -> this.data = entityType.name();
            case CraftingRecipe recipe ->
            {
                this.data = recipe.key().asString();
                max *= recipe.getResult().getAmount();
            }
            case CookingRecipe<?> recipe -> this.data = recipe.key().asString();
            case Biome biome -> this.data = biome.key().asString();
            default -> this.data = null;
        }

        this.max = max;
    }

    public boolean isData(String data)
    {
        try
        {
            Material material = Material.valueOf(data);
            switch (objective)
            {
                case CRAFT ->
                {
                    return ((CraftingRecipe) getData()).getResult().getType().equals(material);
                }
                case SMELT, COOK ->
                {
                    return ((CookingRecipe<?>) getData()).getInputChoice().getItemStack().getType().equals(material);
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
            case KILL_ANIMAL, KILL_MONSTER, TAME, FISH ->
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
        return config.getDouble("objectives." + objective.toString().toLowerCase() + ".prize") * max;
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

        return LocaleData.format("&f" + displayName);
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
                        " " + (max - amount) + " " + LocaleData.capitalize(getData().toString());
            }
            case KILL_ANIMAL ->
            {
                displayName = LocaleData.capitalize(objective.toString().replace("_ANIMAL", "")) +
                        " " + (max - amount) + " " + LocaleData.capitalize(getData().toString());
            }
            case BREAK_BLOCK ->
            {
                displayName = LocaleData.capitalize(objective.toString().replace("_BLOCK", "")) +
                        " " + (max - amount) + " " + LocaleData.capitalize(getData().toString());
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
                        LocaleData.capitalize(getData().toString());
            }
        }

        return LocaleData.format("&f" + displayName);
    }

    public List<String> getLore()
    {
        List<String> lore = new ArrayList<>();
        lore.addAll(List.of("",
                "  &aReward: &f" + new DecimalFormat("#,###.##").format(getPrize()) + " ꐘ  ",
                ""));
        return LocaleData.formatLore(lore);
    }

    public Material getIcon()
    {
        switch (objective)
        {
            case KILL_ANIMAL, KILL_MONSTER, TAME ->
            {
                try
                {
                    return Material.valueOf(data + "_SPAWN_EGG");
                }
                catch (IllegalArgumentException exception)
                {
                    return Material.BARRIER;
                }
            }
            case BREAK_BLOCK, FISH->
            {
                try
                {
                    Material material = Material.valueOf(data);
                    if (material.isItem())
                    {
                        return material;
                    }
                }
                catch (IllegalArgumentException exception)
                {
                    return Material.BARRIER;
                }
            }
            case SMELT, COOK ->
            {
                return ((CookingRecipe<?>) getData()).getInputChoice().getItemStack()
                    .getType();
            }
            case CRAFT ->
            {
                return ((CraftingRecipe) getData()).getResult().getType();
            }
            case HARVEST ->
            {
                try
                {
                    switch (data)
                    {
                        case "POTATOES" ->
                        {
                            return Material.POTATO;
                        }
                        case "CARROTS" ->
                        {
                            return Material.CARROT;
                        }
                        case "BEETROOTS" ->
                        {
                            return Material.BEETROOT;
                        }
                        default ->
                        {
                            Material material = Material.valueOf(data);
                            if (material.isItem())
                            {
                                return material;
                            }
                        }
                    }
                }
                catch (IllegalArgumentException exception)
                {
                    return Material.BARRIER;
                }
            }
            case VISIT_BIOME ->
            {
                return Material.GRASS_BLOCK;
            }
            case ENCHANT ->
            {
                return Material.ENCHANTED_BOOK;
            }
        }

        return Material.BARRIER;
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
}
