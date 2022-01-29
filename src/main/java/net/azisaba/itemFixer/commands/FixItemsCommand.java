package net.azisaba.itemFixer.commands;

import net.azisaba.itemFixer.ItemFixer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FixItemsCommand implements TabExecutor {
    private final Map<UUID, Long> lastExecution = new HashMap<>();
    private final ItemFixer plugin;

    public FixItemsCommand(ItemFixer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "コンソールからは実行できません。");
            return true;
        }
        UUID uuid = ((Player) sender).getUniqueId();
        long lastExecutedAt = lastExecution.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - lastExecutedAt < 60000) {
            // the player must wait 60 seconds to run the command again
            sender.sendMessage(ChatColor.RED + "再度実行するにはあと" + (60 - (System.currentTimeMillis() - lastExecutedAt) / 1000) + "秒待つ必要があります。");
            return true;
        }
        lastExecution.put(uuid, System.currentTimeMillis());
        plugin.fixNow((Player) sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
