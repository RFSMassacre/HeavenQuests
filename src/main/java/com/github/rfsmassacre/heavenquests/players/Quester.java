package com.github.rfsmassacre.heavenquests.players;

import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenquests.HeavenQuests;
import com.github.rfsmassacre.heavenquests.quests.Quest;
import com.github.rfsmassacre.heavenquests.data.QuesterGson;
import com.github.rfsmassacre.heavenquests.utils.TaskUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Getter
public class Quester
{
    public static final Map<UUID, Quester> QUESTERS = new HashMap<>();

    private static final ConcurrentHashMap<UUID, Quester> PLAYERS = new ConcurrentHashMap<>();

    public static void addQuester(Quester quester)
    {
        PLAYERS.put(quester.playerId, quester);
    }

    public static void removeQuester(UUID playerId)
    {
        PLAYERS.remove(playerId);
    }

    public static Quester getQuester(UUID playerId)
    {
        return PLAYERS.get(playerId);
    }

    public static Quester getQuester(String playerName)
    {
        for (Quester quester : PLAYERS.values())
        {
            if (quester.getUsername().equals(playerName))
            {
                return quester;
            }
        }

        return null;
    }

    public static Set<Quester> getQuesters()
    {
        return new HashSet<>(PLAYERS.values());
    }

    public static void getOrLoadQuester(UUID playerId, Consumer<Quester> callback)
    {
        Quester quester = getQuester(playerId);
        if (quester != null)
        {
            callback.accept(quester);
            return;
        }

        loadQuester(playerId, callback);
    }

    public static void getOrLoadQuester(String playerName, Consumer<Quester> callback)
    {
        Quester quester = getQuester(playerName);
        if (quester != null)
        {
            callback.accept(quester);
            return;
        }

        QuesterGson gson = HeavenQuests.getInstance().getQuesterGson();
        if (gson == null)
        {
            return;
        }

        gson.allAsync((questers) ->
        {
            for (Quester offline : questers)
            {
                if (offline.getUsername().equals(playerName))
                {
                    callback.accept(offline);
                    return;
                }
            }
        });
    }

    public static void getOrLoadAll(Consumer<Set<Quester>> callback)
    {
        Set<Quester> all = new HashSet<>(PLAYERS.values());
        QuesterGson gson = HeavenQuests.getInstance().getQuesterGson();
        if (gson == null)
        {
            return;
        }

        gson.allAsync((questers) ->
        {
            for (Quester offline : questers)
            {
                if (getQuester(offline.getPlayerId()) == null)
                {
                    all.add(offline);
                }
            }

            callback.accept(all);
        });
    }

    public static void getOrLoadAll(Consumer<Set<Quester>> callback, UUID... whitelist)
    {
        getOrLoadAll((questers) ->
        {
            Set<UUID> whitelistIds = Set.of(whitelist);
            Set<Quester> whitelistQuesters = new HashSet<>();
            for (Quester quester : questers)
            {
                if (whitelistIds.contains(quester.playerId))
                {
                    whitelistQuesters.add(quester);
                }
            }

            callback.accept(whitelistQuesters);
        });
    }

    public static void getOrLoadAll(Consumer<Set<Quester>> callback, String... whitelist)
    {
        getOrLoadAll((questers) ->
        {
            Set<String> whitelistNames = Set.of(whitelist);
            Set<Quester> whitelistQuesters = new HashSet<>();
            for (Quester quester : questers)
            {
                if (whitelistNames.contains(quester.username))
                {
                    whitelistQuesters.add(quester);
                }
            }

            callback.accept(whitelistQuesters);
        });
    }

    public static void saveQuester(Quester quester)
    {
        QuesterGson gson = HeavenQuests.getInstance().getQuesterGson();
        if (gson !=  null)
        {
            gson.writeAsync(quester.playerId.toString(), quester);
        }
    }

    public static void saveQuesters(boolean async)
    {
        QuesterGson gson = HeavenQuests.getInstance().getQuesterGson();
        if (gson == null)
        {
            return;
        }

        if (async)
        {
            TaskUtil taskUtil = HeavenQuests.getInstance().getTaskUtil();
            taskUtil.runAsync(() ->
            {
                for (Quester quester : PLAYERS.values())
                {
                    gson.write(quester.playerId.toString(), quester);
                }
            });
        }
        else
        {
            for (Quester quester : PLAYERS.values())
            {
                gson.write(quester.playerId.toString(), quester);
            }
        }
    }

    public static void loadQuester(UUID playerId, Consumer<Quester> callback)
    {
        QuesterGson gson = HeavenQuests.getInstance().getQuesterGson();
        if (gson != null)
        {
            gson.readAsync(playerId.toString(), callback);
        }
    }

    public static void deleteQuester(UUID playerId)
    {
        QuesterGson gson = HeavenQuests.getInstance().getQuesterGson();
        if (gson != null)
        {
            gson.deleteAsync(playerId.toString());
        }
    }

    private final ConcurrentHashMap<Quest.Objective, Quest> availableQuests;
    private UUID playerId;
    private String username;
    private String displayName;
    @Setter
    private long timeStamp;

    public Quester()
    {
        this.availableQuests = new ConcurrentHashMap<>();
    }

    public Quester(UUID playerId)
    {
        this();

        this.playerId = playerId;
        this.timeStamp = System.currentTimeMillis();
        this.availableQuests.putAll(Quest.generateQuests());
    }

    public Quester(OfflinePlayer player)
    {
        this(player.getUniqueId());
    }

    public Player getPlayer()
    {
        return Bukkit.getPlayer(playerId);
    }

    public String getUsername()
    {
        Player player = getPlayer();
        if (player != null)
        {
            this.username = player.getName();
        }

        return username;
    }

    public String getDisplayName()
    {
        Player player = getPlayer();
        if (player != null)
        {
            this.displayName = player.getDisplayName();
        }

        return displayName;
    }

    public long getTimeElapsed(ChronoUnit unit)
    {
        return Instant.ofEpochMilli(timeStamp).until(Instant.now(), unit);
    }

    public long getTimeLeft()
    {
        PaperConfiguration config = HeavenQuests.getInstance().getConfiguration();
        int interval = config.getInt("daily-interval");
        return Math.max(0L, interval - getTimeElapsed(ChronoUnit.SECONDS));
    }

    public boolean isExpired()
    {
        return getTimeLeft() <= 0L;
    }

    public void refreshAvailableQuests()
    {
        availableQuests.clear();
        availableQuests.putAll(Quest.generateQuests());
        this.timeStamp = System.currentTimeMillis();
    }

    public void refreshCompletedQuests()
    {
        PaperConfiguration config = HeavenQuests.getInstance().getConfiguration();
        for (Quest.Objective objective : Quest.Objective.values())
        {
            Quest quest = availableQuests.get(objective);
            if (quest == null || (quest.isComplete() && quest.isExpired()))
            {
                int min = config.getInt("objectives." + objective.toString().toLowerCase() + ".min");
                int max = config.getInt("objectives." + objective.toString().toLowerCase() + ".max");
                availableQuests.put(objective, new Quest(objective, new SecureRandom().nextInt(min, max + 1)));
            }
        }
    }
}
