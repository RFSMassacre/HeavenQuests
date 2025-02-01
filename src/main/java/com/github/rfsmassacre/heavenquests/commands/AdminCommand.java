package com.github.rfsmassacre.heavenquests.commands;

import com.github.rfsmassacre.heavenlibrary.paper.commands.PaperCommand;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import com.github.rfsmassacre.heavenquests.players.Quester;
import com.github.rfsmassacre.heavenquests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                if (!sender.equals(quester.getPlayer()))
                {
                    locale.sendLocale(sender, "rerolled.self", "{player}", quester.getDisplayName());
                }

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

    private class SetCommand extends PaperSubCommand
    {
        public SetCommand()
        {
            super("set");
        }

        @Override
        protected void onRun(CommandSender sender, String... args)
        {
            if (args.length < 4)
            {
                onInvalidArgs(sender, "<objective>", "<data>", "<amount>", "[player]");
                return;
            }

            String playerName;
            if (args.length > 4)
            {
                playerName = args[4];
            }
            else if (sender instanceof Player player)
            {
                playerName = player.getName();
            }
            else
            {
                playerName = null;
            }

            if (playerName == null)
            {
                onInvalidArgs(sender, "<objective>", "<data>", "<amount>", "[player]");
                return;
            }

            Quest.Objective objective = Quest.Objective.fromString(args[1]);
            if (objective == null)
            {
                return;
            }

            String dataString = args[2];
            if (!objective.getDataString().contains(dataString))
            {
                return;
            }

            int amount;
            try
            {
                amount = Integer.parseInt(args[3]);
            }
            catch (NumberFormatException exception)
            {
                return;
            }

            Quest quest = new Quest(objective, dataString, amount);
            Quester.getOrLoadQuester(playerName, (quester) ->
            {
                if (quester == null)
                {
                    locale.sendLocale(sender, "player-not-found", "{player}", playerName);
                    playSound(sender, SoundKey.INCOMPLETE);
                    return;
                }

                quester.getAvailableQuests().put(objective, quest);
                Quester.saveQuester(quester);
                if (!sender.equals(quester.getPlayer()))
                {
                    locale.sendLocale(sender, "set.self", "{player}", quester.getDisplayName(), "{quest}",
                            quest.getDisplayName());
                }

                locale.sendLocale(quester.getPlayer(), "set.target", "{quest}", quest.getDisplayName());
                playSound(sender, SoundKey.SUCCESS);
            });
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String... args)
        {
            List<String> suggestions = new ArrayList<>();
            if (args.length == 2)
            {
                for (Quest.Objective objective : Quest.Objective.values())
                {
                    suggestions.add(objective.name());
                }
            }
            else if (args.length == 3)
            {
                Quest.Objective objective = Quest.Objective.fromString(args[1]);
                if (objective != null)
                {
                    suggestions.addAll(objective.getDataString());
                }
            }
            else if (args.length == 5)
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
