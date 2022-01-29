package net.azisaba.itemFixer.listeners;

import net.azisaba.itemFixer.ItemFixer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FixItemsOnJoinListener implements Listener {
    private final ItemFixer plugin;

    public FixItemsOnJoinListener(ItemFixer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.fixNow(e.getPlayer()), 20 * 3);
    }
}
