package com.github.rfsmassacre.heavenquests.listeners;

import com.github.rfsmassacre.heavenquests.players.Quester;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        Quester.loadQuester(player.getUniqueId(), (quester) ->
        {
            if (quester == null)
            {
                quester = new Quester(player);
            }

            Quester.addQuester(quester);
            Quester.saveQuester(quester);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        Quester quester = Quester.getQuester(player.getUniqueId());
        if (quester == null)
        {
            return;
        }

        Quester.saveQuester(quester);
        Quester.removeQuester(player.getUniqueId());
    }
}
