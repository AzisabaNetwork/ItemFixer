package net.azisaba.itemFixer.listeners;

import net.azisaba.itemFixer.ItemFixer;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class FixItemOnClickListener implements Listener {
    private final ItemFixer plugin;

    public FixItemOnClickListener(ItemFixer plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        if (clickedInventory.getType() != InventoryType.PLAYER) {
            return;
        }
        if (e.getWhoClicked().hasPermission("itemfixer.exempt.fix_on_click") ||
                e.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        e.setCurrentItem(plugin.fixSingle("cursor of " + e.getWhoClicked().getName(), item));
    }
}
