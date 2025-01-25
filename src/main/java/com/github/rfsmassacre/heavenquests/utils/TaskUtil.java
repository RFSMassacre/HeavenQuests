package com.github.rfsmassacre.heavenquests.utils;

import com.github.rfsmassacre.heavenlibrary.paper.utils.PaperTaskUtil;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import com.github.rfsmassacre.heavenquests.tasks.AutoSaveTask;
import com.github.rfsmassacre.heavenquests.tasks.QuestRefreshTask;

/**
 * Handle starting, stopping, and tracking repeating tasks.
 */
public class TaskUtil extends PaperTaskUtil
{
    public TaskUtil()
    {
        super(HeavenQuests.getInstance());

        startTimers();
    }

    /**
     * Manually add all the timers to start.
     */
    public void startTimers()
    {
        startTimerAsync(new AutoSaveTask(), 0L, 1200);
        startTimerAsync(new QuestRefreshTask(), 0L, 20L);
    }
}
