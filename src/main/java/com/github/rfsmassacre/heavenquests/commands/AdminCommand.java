package com.github.rfsmassacre.heavenquests.commands;

import com.github.rfsmassacre.heavenlibrary.paper.commands.PaperCommand;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import com.github.rfsmassacre.heavenquests.players.Quester;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminCommand extends PaperCommand
{
    public AdminCommand()
    {
        super(HeavenQuests.getInstance(), "questadmin");
    }

    private class ReloadCommand extends PaperSubCommand
    {
        public ReloadCommand()
        {
            super("reload");
        }

        @Override
        protected void onRun(CommandSender sender, String... args)
        {
            config.reload();
            locale.reload();
            locale.sendLocale(sender, "reloaded");
            playSound(sender, SoundKey.SUCCESS);
        }
    }

    private class RerollCommand extends PaperSubCommand
    {
        public RerollCommand()
        {
            super("reroll");
        }

        @Override
        protected void onRun(CommandSender sender, String... args)
        {
            String playerName;
            if (args.length < 2)
            {
                if (sender instanceof Player player)
                {
                    playerName = player.getName();
                }
                else
                {
                    onInvalidArgs(sender, "<player>");
                    return;
                }
            }
            else
            {
                playerName = args[1];
            }

            Quester.getOrLoadQuester(playerName, (quester) ->
            {
                if (quester == null)
                {
                    locale.sendLocale(sender, "player-not-found", "{player}", playerName);
                    playSound(sender, SoundKey.INCOMPLETE);
                    return;
                }

                quester.refreshAvailableQuests();
                Quester.saveQuester(quester);
                locale.sendLocale(sender, "rerolled.self", "{player}", quester.getDisplayName());
                locale.sendLocale(quester.getPlayer(), "rerolled.target");
                playSound(sender, SoundKey.SUCCESS);
            });
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String... args)
        {
            List<String> suggestions = new ArrayList<>();
            if (args.length == 2)
            {
                for (Player player : Bukkit.getOnlinePlayers())
                {
                    suggestions.add(player.getName());
                }
            }

            return suggestions;
        }
    }
}
