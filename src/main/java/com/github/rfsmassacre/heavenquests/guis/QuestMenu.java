package com.github.rfsmassacre.heavenquests.guis;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenlibrary.paper.menu.Icon;
import com.github.rfsmassacre.heavenlibrary.paper.menu.Menu;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import com.github.rfsmassacre.heavenquests.quests.Quest;
import com.github.rfsmassacre.heavenquests.players.Quester;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class QuestMenu extends Menu
{
    private final PaperConfiguration config;
    private final PaperLocale locale;

    public QuestMenu()
    {
        super("&0Daily Quests", 4, 1);

        this.config = HeavenQuests.getInstance().getConfiguration();
        this.locale = HeavenQuests.getInstance().getLocale();
    }

    @Override
    public void updateIcons(Player player)
    {
        Quester quester = Quester.getQuester(player.getUniqueId());
        if (quester == null)
        {
            return;
        }

        Currency currency = CoinsEngineAPI.getCurrency(config.getString("currency.reroll"));
        if (currency == null)
        {
            return;
        }

        for (Quest quest : Arrays.stream(Quest.Objective.values())
                .map((objective) -> quester.getAvailableQuests().get(objective))
                .filter(Objects::nonNull)
                .toList())
        {
            for (int x = 1; x <= 9; x++)
            {
                QuestIcon icon = new QuestIcon(x, 2, quest, config.getInt("single-price"), currency);
                if (!slotTaken(icon.getSlot()))
                {
                    addIcon(icon);
                    break;
                }
            }
        }

        addIcon(new RerollIcon(5, 4, config.getInt("reroll-price"), currency, quester.getTimeLeft()));
    }

    private class QuestIcon extends Icon
    {
        private final Quest quest;
        private final int price;
        private final Currency currency;

        public QuestIcon(int x, int y, Quest quest, int price, Currency currency)
        {
            super(x, y, 1, quest.isComplete(), quest.getIcon(), "&f" + quest.getIconDisplayName(),
                    quest.getLore());

            this.quest = quest;
            this.price = price;
            this.currency = currency;
            if (quest.isComplete())
            {
                List<String> lore = quest.getLore();
                lore.addAll(List.of(
                        "  &7Click to get a new quest for the day or wait  ",
                        "  &7until it refreshes.  ",
                        "",
                        "  &eReroll Price: &6(&f" + price + " " + currency.getSymbol() + "&6)",
                        "",
                        "  &4(&e" + LocaleData.formatTime(quest.getTimeLeft()) + " Left&4)",
                        ""));
                setLore(lore);
            }
        }

        @Override
        public void onClick(Player player)
        {
            if (!quest.isComplete())
            {
                return;
            }

            Quester quester = Quester.getQuester(player.getUniqueId());
            if (quester == null)
            {
                locale.sendLocale(player, "no-data");
                return;
            }

            double balance = CoinsEngineAPI.getBalance(player, currency);
            DecimalFormat format = new DecimalFormat("#,###.##");
            if (balance < amount)
            {
                locale.sendLocale(player, "not-enough", "{price}", format.format(price) + " " +
                        currency.getSymbol(), "{balance}", format.format(balance) + " " + currency.getSymbol());
                return;
            }

            CoinsEngineAPI.removeBalance(player, currency, price);
            balance = CoinsEngineAPI.getBalance(player, currency);
            Quest newQuest = Quest.generateQuest(quest.getObjective());
            quester.getAvailableQuests().put(newQuest.getObjective(), newQuest);
            locale.sendLocale(player, "rerolled.single", "{old}", quest.getDisplayName(), "{new}",
                    newQuest.getDisplayName(), "{price}", format.format(price) + " " + currency.getSymbol(),
                    "{balance}", format.format(balance) + " " + currency.getSymbol());
        }
    }

    private class RerollIcon extends Icon
    {
        private final double price;
        private final Currency currency;

        public RerollIcon(int x, int y, int price, Currency currency, long timeRemaining)
        {
            super(x, y, 1, true, Material.EXPERIENCE_BOTTLE, "&fReroll Quests &6(&f" +
                            price + " " + currency.getSymbol() + "&6)",
                    List.of("",
                            "  &7Get a new set a quests for the day or wait  ",
                            "  &7until they expire.  ",
                            "",
                            "  &4(&e" + LocaleData.formatTime(timeRemaining) + " Left&4)",
                            ""));

            this.price = price;
            this.currency = currency;
        }

        @Override
        public void onClick(Player player)
        {
            Quester quester = Quester.getQuester(player.getUniqueId());
            if (quester == null)
            {
                locale.sendLocale(player, "no-data");
                return;
            }

            double balance = CoinsEngineAPI.getBalance(player, currency);
            DecimalFormat format = new DecimalFormat("#,###.##");
            if (balance < amount)
            {
                locale.sendLocale(player, "not-enough", "{price}", format.format(price) + " " +
                        currency.getSymbol(), "{balance}", format.format(balance) + " " + currency.getSymbol());
                return;
            }

            CoinsEngineAPI.removeBalance(player, currency, price);
            balance = CoinsEngineAPI.getBalance(player, currency);
            quester.refreshAvailableQuests();
            locale.sendLocale(player, "rerolled.menu", "{price}", format.format(price) + " " +
                    currency.getSymbol(), "{balance}", format.format(balance) + " " + currency.getSymbol());
        }
    }
}
