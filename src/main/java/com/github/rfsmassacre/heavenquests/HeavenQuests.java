package com.github.rfsmassacre.heavenquests;

import com.github.rfsmassacre.heavenlibrary.paper.HeavenPaperPlugin;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenquests.commands.AdminCommand;
import com.github.rfsmassacre.heavenquests.commands.QuestCommand;
import com.github.rfsmassacre.heavenquests.data.QuesterGson;
import com.github.rfsmassacre.heavenquests.listeners.CustomFishingListener;
import com.github.rfsmassacre.heavenquests.listeners.LoginListener;
import com.github.rfsmassacre.heavenquests.listeners.QuestListener;
import com.github.rfsmassacre.heavenquests.players.Quester;
import com.github.rfsmassacre.heavenquests.quests.Quest;
import com.github.rfsmassacre.heavenquests.utils.TaskUtil;
import lombok.Getter;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class HeavenQuests extends HeavenPaperPlugin
{
    @Getter
    private static HeavenQuests instance;
    private QuesterGson questerGson;
    private TaskUtil taskUtil;

    @Override
    public void onEnable()
    {
        instance = this;
        getDataFolder().mkdir();
        addYamlManager(new PaperConfiguration(this, "", "config.yml", true));
        addYamlManager(new PaperLocale(this, "", "locale.yml", true));
        this.questerGson = new QuesterGson();
        this.taskUtil = new TaskUtil();
        PluginManager plugins = getServer().getPluginManager();
        plugins.registerEvents(new LoginListener(), this);
        plugins.registerEvents(new QuestListener(), this);
        if (plugins.isPluginEnabled("CustomFishing"))
        {
            Quest.Objective.FISH.getDatas().clear();
            Quest.Objective.FISH.getDatas().addAll(BukkitCustomFishingPlugin.getInstance().getItemManager()
                    .getItemIDs());
            for (Object data : Quest.Objective.FISH.getDatas())
            {
                getLogger().info(data.toString());
            }

            plugins.registerEvents(new CustomFishingListener(), this);
        }

        getCommand("quest").setExecutor(new QuestCommand());
        getCommand("questadmin").setExecutor(new AdminCommand());
        taskUtil.startTimers();
    }

    @Override
    public void onDisable()
    {
        Quester.saveQuesters(false);
    }
}
