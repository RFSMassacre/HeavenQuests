package com.github.rfsmassacre.heavenquests.listeners;

import com.github.rfsmassacre.heavenquests.quests.Quest;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.CustomFishingReloadEvent;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CustomFishingListener implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPluginReload(CustomFishingReloadEvent event)
    {
        Quest.Objective.FISH.getDatas().clear();
        Quest.Objective.FISH.getDatas().addAll(Quest.Objective.getFish());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFishingResult(FishingLootSpawnEvent event)
    {
        String generalId = event.getLoot().id()
                .replace("gold_star", "")
                .replace("silver_star", "");
        if (QuestListener.processQuest(event.getPlayer(), Quest.Objective.FISH, 1, generalId))
        {
            if (event.getEntity() instanceof Item item)
            {
                item.remove();
            }
        }
    }
}
