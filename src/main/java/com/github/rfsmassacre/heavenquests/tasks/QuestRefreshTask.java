package com.github.rfsmassacre.heavenquests.tasks;

import com.github.rfsmassacre.heavenquests.players.Quester;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestRefreshTask extends BukkitRunnable
{
    /**
     * Runs this operation.
     */
    @Override
    public void run()
    {
        for (Quester quester : Quester.getQuesters())
        {
            if (quester.isExpired())
            {
                quester.refreshAvailableQuests();
            }
        }
    }
}
