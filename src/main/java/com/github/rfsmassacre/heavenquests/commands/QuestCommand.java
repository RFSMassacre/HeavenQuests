package com.github.rfsmassacre.heavenquests.commands;

import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import com.github.rfsmassacre.heavenlibrary.paper.menu.Menu;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import com.github.rfsmassacre.heavenquests.guis.QuestMenu;
import com.github.rfsmassacre.heavenquests.players.Quester;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuestCommand extends SimplePaperCommand
{
    public QuestCommand()
    {
        super(HeavenQuests.getInstance(), "quest");
    }

    @Override
    protected void onRun(CommandSender sender, String... args)
    {
        Player player;
        if (!(sender instanceof Player))
        {
            if (args.length < 1)
            {
                onInvalidArgs(sender, "<player>");
                return;
            }

            String playerName = args[0];
            player = Bukkit.getPlayer(playerName);
            if (player == null)
            {
                locale.sendLocale(sender, "player-not-found", "{player}", playerName);
                playSound(sender, SoundKey.INCOMPLETE);
                return;
            }
        }
        else
        {
            player = (Player) sender;
        }

        Quester quester = Quester.getQuester(player.getUniqueId());
        if (quester == null)
        {
            locale.sendLocale(player, "no-data");
            return;
        }

        Menu menu = new QuestMenu();
        Menu.addView(player.getUniqueId(), menu);
        player.openInventory(menu.createInventory(player));
        playSound(player, SoundKey.SUCCESS);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args)
    {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                suggestions.add(player.getName());
            }
        }

        return suggestions;
    }
}
