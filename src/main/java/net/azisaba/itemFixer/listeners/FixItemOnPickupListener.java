package net.azisaba.itemFixer.listeners;

import net.azisaba.itemFixer.ItemFixer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class FixItemOnPickupListener implements Listener {

    private final ItemFixer plugin;

    public FixItemOnPickupListener(ItemFixer plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent e) {
        Entity eveEntity = e.getEntity();

        if(eveEntity.getType() != EntityType.PLAYER) {
            return;
        }
        if(eveEntity.hasPermission("itemfixer.exempt.fix_on_pickup")) {
            return;
        }

        ItemStack item = e.getItem().getItemStack();
        plugin.fixSingle(eveEntity.getName() + " picked up an item", item);

    }
}
