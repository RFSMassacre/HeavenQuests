package com.github.rfsmassacre.heavenquests.tasks;

import com.github.rfsmassacre.heavenquests.players.Quester;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveTask extends BukkitRunnable
{
    /**
     * Runs this operation.
     */
    @Override
    public void run()
    {
        Quester.saveQuesters(false);
    }
}
