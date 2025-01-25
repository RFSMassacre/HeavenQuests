package com.github.rfsmassacre.heavenquests.listeners;

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
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;

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

    private final PaperLocale locale;
    private final Map<Block, FurnaceItem> furnaceItems;

    public QuestListener()
    {
        this.locale = HeavenQuests.getInstance().getLocale();
        this.furnaceItems = new HashMap<>();
    }

    private void processQuest(Player player, Quest.Objective objective, int amount, String data)
    {
        Quester quester = Quester.getQuester(player.getUniqueId());
        if (quester == null)
        {
            return;
        }

        Quest quest = quester.getAvailableQuests().get(objective);
        if (quest == null || quest.isComplete() || !quest.isData(data))
        {
            return;
        }

        quest.addAmount(amount);
        if (quest.isComplete())
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "silver give " + player.getName() + " " +
                    quest.getPrize());
            locale.sendLocale(player, "success", "{quest}", quest.getDisplayName(), "{prize}",
                    new DecimalFormat("#,###.##").format(quest.getPrize()));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() >= ageable.getMaximumAge())
        {
            processQuest(event.getPlayer(), Quest.Objective.HARVEST, 1, block.getType().toString());
        }
        else
        {
            processQuest(event.getPlayer(), Quest.Objective.BREAK_BLOCK, 1, block.getType().toString());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        processQuest(event.getPlayer(), Quest.Objective.BREAK_BLOCK, -1, event.getBlock().getType().toString());
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
            processQuest(player, Quest.Objective.CRAFT, getAmount(event.getInventory()),
                    recipe.getResult().getType().toString());
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
            else if (furnaceItems.containsKey(block))
            {
                furnaceItems.remove(block);
                Bukkit.getLogger().info(player.getName() + " has removed " + event.getCurrentItem().getType() +
                        "from a " + inventoryType + ". They have " + getActiveFurnaces(player.getUniqueId()) +
                        "  active!");
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
                processQuest(player, Quest.Objective.COOK, 1, event.getRecipe().getInputChoice().getItemStack()
                        .getType().toString());
            }
            else
            {
                processQuest(player, Quest.Objective.SMELT, 1, event.getRecipe().getInputChoice().getItemStack()
                        .getType().toString());
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
        processQuest(event.getEnchanter(), Quest.Objective.ENCHANT, 1, event.getItem().getType().toString());
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
                processQuest(player, Quest.Objective.KILL_ANIMAL, 1, animal.getType().toString());
            }
            case Monster monster ->
            {
                processQuest(player, Quest.Objective.KILL_MONSTER, 1, monster.getType().toString());
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
            processQuest(event.getPlayer(), Quest.Objective.FISH, 1, entity.getType().toString());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityTame(EntityTameEvent event)
    {
        if (event.getOwner() instanceof Player player)
        {
            processQuest(player, Quest.Objective.TAME, 1, event.getEntityType().toString());
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
