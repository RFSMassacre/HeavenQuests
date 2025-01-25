package com.github.rfsmassacre.heavenquests;

import com.github.rfsmassacre.heavenlibrary.paper.HeavenPaperPlugin;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenquests.commands.AdminCommand;
import com.github.rfsmassacre.heavenquests.commands.QuestCommand;
import com.github.rfsmassacre.heavenquests.data.QuesterGson;
import com.github.rfsmassacre.heavenquests.listeners.LoginListener;
import com.github.rfsmassacre.heavenquests.listeners.QuestListener;
import com.github.rfsmassacre.heavenquests.players.Quester;
import com.github.rfsmassacre.heavenquests.utils.TaskUtil;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;

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
