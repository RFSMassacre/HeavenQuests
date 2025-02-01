package com.github.rfsmassacre.heavenquests.listeners;

import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import com.github.rfsmassacre.heavenquests.quests.Quest;
import com.github.rfsmassacre.heavenquests.players.Quester;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.text.DecimalFormat;
import java.util.*;

public class QuestListener implements Listener
{
    @Getter
    private static class FurnaceItem
    {
        private final UUID playerId;
        private final Material material;
        private final InventoryType inventoryType;
        private final Block block;
        private int amount;

        public FurnaceItem(UUID playerId, Material material, InventoryType inventoryType, Block block, int amount)
        {
            this.playerId = playerId;
            this.material = material;
            this.inventoryType = inventoryType;
            this.block = block;
            this.amount = amount;
        }

        public Player getPlayer()
        {
            return Bukkit.getPlayer(playerId);
        }

        public void setAmount(int amount)
        {
            this.amount = Math.max(0, amount);
        }

        public void removeAmount(int amount)
        {
            setAmount(this.amount - amount);
        }
    }

    private final PaperConfiguration config;
    private final PaperLocale locale;
    private final Map<Block, FurnaceItem> furnaceItems;
    private final Map<UUID, Block> harvestedBlocks;

    public QuestListener()
    {
        this.config = HeavenQuests.getInstance().getConfiguration();
        this.locale = HeavenQuests.getInstance().getLocale();
        this.furnaceItems = new HashMap<>();
        this.harvestedBlocks = new HashMap<>();
    }

    private boolean processQuest(Player player, Quest.Objective objective, int amount, String data)
    {
        Currency currency = CoinsEngineAPI.getCurrency(config.getString("currency.prize"));
        if (currency == null)
        {
            return false;
        }

        Quester quester = Quester.getQuester(player.getUniqueId());
        if (quester == null)
        {
            return false;
        }

        Quest quest = quester.getAvailableQuests().get(objective);
        if (quest == null || quest.isComplete() || !quest.isData(data))
        {
            return false;
        }

        quest.addAmount(amount);
        int fadeIn = config.getInt("title-duration.fade-in");
        int stay = config.getInt("title-duration.stay");
        int fadeOut = config.getInt("title-duration.fade-out");
        if (!quest.isComplete())
        {
            locale.sendTitleLocale(player, fadeIn, stay, fadeOut, "notify.progress.title",
                    "notify.progress.subtitle", "{quest}", quest.getIconDisplayName());
            return true;
        }

        quest.setTimeCompleted(System.currentTimeMillis());
        CoinsEngineAPI.addBalance(player, currency, quest.getPrize());
        DecimalFormat format =  new DecimalFormat("#,###.##");
        locale.sendTitleLocale(player, fadeIn, stay, fadeOut, "notify.complete.title",
                "notify.complete.subtitle", "{prize}", format.format(quest.getPrize()) + " " +
                        currency.getSymbol());
        locale.sendLocale(player, "success", "{quest}", quest.getDisplayName(), "{prize}",
                format.format(quest.getPrize()) + " " + currency.getSymbol());
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (processQuest(event.getPlayer(), Quest.Objective.BREAK_BLOCK, 1, block.getType().name()))
        {
            event.setDropItems(false);
        }
        else
        {
            harvestedBlocks.put(player.getUniqueId(), block);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerHarvestBlock(PlayerHarvestBlockEvent event)
    {
        Player player = event.getPlayer();
        List<ItemStack> crops = event.getItemsHarvested();
        for (ItemStack item : new ArrayList<>(crops))
        {
            Material material = item.getType();
            int amount = item.getAmount();
            if (amount > 1)
            {
                int difference = amount - 1;
                if (processQuest(player, Quest.Objective.HARVEST, difference, material.name()))
                {
                    item.setAmount(1);
                }
            }
            else if (amount == 1)
            {
                if (processQuest(player, Quest.Objective.HARVEST, 1, material.name()))
                {
                    crops.remove(item);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockDropItem(BlockDropItemEvent event)
    {
        Player player = event.getPlayer();
        if (!event.getBlock().equals(harvestedBlocks.get(player.getUniqueId())))
        {
            return;
        }

        List<Item> drops = event.getItems();
        for (Item item : new ArrayList<>(drops))
        {
            Material material = item.getItemStack().getType();
            int amount = item.getItemStack().getAmount();
            if (amount > 1)
            {
                int difference = amount - 1;
                if (processQuest(player, Quest.Objective.HARVEST, difference, material.name()))
                {
                    item.getItemStack().setAmount(1);
                    harvestedBlocks.remove(player.getUniqueId());
                }
            }
            else if (amount == 1)
            {
                if (processQuest(player, Quest.Objective.HARVEST, 1, material.name()))
                {
                    drops.remove(item);
                    harvestedBlocks.remove(player.getUniqueId());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        processQuest(event.getPlayer(), Quest.Objective.BREAK_BLOCK, -1, event.getBlock().getType().name());
    }

    private int getAmount(CraftingInventory inventory)
    {
        int lastAmount = 0;
        int leastAmount = 1;
        for (ItemStack item : inventory.getMatrix())
        {
            if (item != null && !item.getType().equals(Material.AIR))
            {
                if (lastAmount == 0)
                {
                    lastAmount = item.getAmount();
                }

                leastAmount = Math.min(lastAmount, item.getAmount());
                lastAmount = item.getAmount();
            }
        }

        return leastAmount * (inventory.getResult() != null ? inventory.getResult().getAmount() : 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCraftItemEvent(CraftItemEvent event)
    {
        if (event.getWhoClicked() instanceof Player player && event.getRecipe() instanceof CraftingRecipe recipe)
        {
            if (processQuest(player, Quest.Objective.CRAFT, getAmount(event.getInventory()),
                    recipe.getResult().getType().name()))
            {

                ItemStack item = recipe.getResult().clone();
                item.setAmount(Math.max(0, item.getAmount() - getAmount(event.getInventory())));
                if (item.getAmount() == 0)
                {
                    event.getInventory().removeItem(item);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceOpen(InventoryClickEvent event)
    {
        InventoryType inventoryType = event.getInventory().getType();
        if (!(inventoryType == InventoryType.FURNACE || inventoryType == InventoryType.BLAST_FURNACE ||
                inventoryType == InventoryType.SMOKER))
        {
            return;
        }

        if (event.getInventory().getLocation() == null)
        {
            return;
        }

        Block block = event.getInventory().getLocation().getBlock();
        ItemStack item = null;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() instanceof FurnaceInventory && event.getRawSlot() == 0)
        {
            if (!event.getClick().equals(ClickType.SHIFT_LEFT) && !event.getCursor().getType().equals(Material.AIR))
            {
                item = event.getCursor();
            }
            else
            {
                furnaceItems.remove(block);
            }
        }
        else if (event.getClickedInventory() instanceof PlayerInventory &&
                event.getClick().equals(ClickType.SHIFT_LEFT))
        {
            item = event.getCurrentItem();
        }

        if (item == null)
        {
            return;
        }

        Quester quester = Quester.getQuester(player.getUniqueId());
        if (quester == null)
        {
           return;
        }

        Quest smeltQuest = quester.getAvailableQuests().get(Quest.Objective.SMELT);
        if (smeltQuest != null && !smeltQuest.isComplete() && ((CookingRecipe<?>) smeltQuest.getData())
                .getInputChoice().getItemStack().getType().equals(item.getType()))
        {
            FurnaceItem furnaceItem = new FurnaceItem(player.getUniqueId(), item.getType(), inventoryType, block,
                    item.getAmount());
            furnaceItems.put(block, furnaceItem);
            Bukkit.getLogger().info(player.getName() + " has placed " + item.getType() +
                    " in a " + inventoryType + ". They have " + getActiveFurnaces(player.getUniqueId()) +
                    " furnaces active!");
            return;
        }

        Quest cookQuest = quester.getAvailableQuests().get(Quest.Objective.COOK);
        if (cookQuest != null && !cookQuest.isComplete() && ((CookingRecipe<?>) cookQuest.getData())
                .getInputChoice().getItemStack().getType().equals(item.getType()))
        {
            FurnaceItem furnaceItem = new FurnaceItem(player.getUniqueId(), item.getType(), inventoryType, block,
                    item.getAmount());
            furnaceItems.put(block, furnaceItem);
            Bukkit.getLogger().info(player.getName() + " has placed " + item.getType() +
                    " in a " + inventoryType + ". They have " + getActiveFurnaces(player.getUniqueId())
                    + " furnaces active!");
        }
    }

    public Player getPlayer(Block block)
    {
        FurnaceItem furnaceItem = furnaceItems.get(block);
        if (furnaceItem != null)
        {
            return furnaceItem.getPlayer();
        }

        return null;
    }

    public int getActiveFurnaces(UUID playerId)
    {
        int amount = 0;
        for (FurnaceItem furnaceItem : furnaceItems.values())
        {
            if (furnaceItem.playerId.equals(playerId))
            {
                amount++;
            }
        }

        return amount;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSmeltItemEvent(FurnaceSmeltEvent event)
    {
        if (event.getRecipe() == null)
        {
            return;
        }

        Player player = getPlayer(event.getBlock());
        if (player != null)
        {
            if (event.getRecipe().getCategory().equals(CookingBookCategory.FOOD))
            {
                if (processQuest(player, Quest.Objective.COOK, 1, event.getRecipe().getInputChoice().getItemStack()
                        .getType().name()))
                {
                    event.setResult(new ItemStack(Material.AIR));
                }

            }
            else
            {
                if (processQuest(player, Quest.Objective.SMELT, 1, event.getRecipe().getInputChoice().getItemStack()
                        .getType().name()))
                {
                    event.setResult(new ItemStack(Material.AIR));
                }
            }

            FurnaceItem furnaceItem = furnaceItems.get(event.getBlock());
            furnaceItem.removeAmount(1);
            if (furnaceItem.getAmount() == 0)
            {
                furnaceItems.remove(event.getBlock());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemEnchant(EnchantItemEvent event)
    {
        if (processQuest(event.getEnchanter(), Quest.Objective.ENCHANT, 1, event.getItem().getType().name()))
        {
            event.setItem(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (!(event.getEntity() instanceof Mob mob))
        {
            return;
        }

        Player player = mob.getKiller();
        if (player == null)
        {
            return;
        }

        switch (mob)
        {
            case Animals animal ->
            {
                if (processQuest(player, Quest.Objective.KILL_ANIMAL, 1, animal.getType().name()))
                {
                    event.getDrops().clear();
                }
            }
            case Monster monster ->
            {
                if (processQuest(player, Quest.Objective.KILL_MONSTER, 1, monster.getType().name()))
                {
                    event.getDrops().clear();
                }
            }
            default ->
            {
                //Do nothing.
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerFish(PlayerFishEvent event)
    {
        Entity entity = event.getCaught();
        if (entity != null)
        {
            if (processQuest(event.getPlayer(), Quest.Objective.FISH, 1, entity.getType().name()))
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityTame(EntityTameEvent event)
    {
        if (event.getOwner() instanceof Player player)
        {
            processQuest(player, Quest.Objective.TAME, 1, event.getEntityType().name());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        Location to = event.getTo();
        processQuest(player, Quest.Objective.VISIT_BIOME, 1, to.getBlock().getBiome().key().asString());
    }
}
