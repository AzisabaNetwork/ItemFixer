package net.azisaba.itemFixer.commands;

import net.azisaba.itemFixer.ItemFixer;
import net.azisaba.itemFixer.screens.FixListScreen;
import net.azisaba.itemFixer.screens.RegisterFixScreen;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ItemFixerCommand implements TabExecutor {
    private static final List<String> SUB_COMMANDS = Arrays.asList("reload", "register", "list", "fix");
    private final ItemFixer plugin;

    public ItemFixerCommand(ItemFixer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "/itemfixer (reload|register|list|fix)");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("itemfixer.command.reload")) {
                sender.sendMessage(ChatColor.RED + "このコマンドを実行する権限がありません。");
                return true;
            }
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "設定を再読み込みしました。");
            sender.sendMessage("" + ChatColor.GREEN + plugin.getItems().size() + "件のFixを読み込みました。");
        } else if (args[0].equalsIgnoreCase("register")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "コンソールからは実行できません。");
                return true;
            }
            ((Player) sender).openInventory(new RegisterFixScreen(plugin).getInventory());
        } else if (args[0].equalsIgnoreCase("list")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "コンソールからは実行できません。");
                return true;
            }
            ((Player) sender).openInventory(new FixListScreen(plugin).getInventory());
        } else if (args[0].equalsIgnoreCase("fix")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "コンソールからは実行できません。");
                return true;
            }
            plugin.fixNow((Player) sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return Collections.singletonList("itemfixer");
        if (args.length == 1) {
            return filter(SUB_COMMANDS, args[0]);
        }
        return Collections.emptyList();
    }

    private static List<String> filter(List<String> list, String s) {
        return list.stream().filter(s1 -> s1.toLowerCase(Locale.ROOT).startsWith(s.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    }
}
