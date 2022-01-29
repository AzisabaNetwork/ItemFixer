package net.azisaba.itemFixer.screens;

import net.azisaba.itemFixer.ItemFixer;
import net.azisaba.itemFixer.util.Chain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;

public class RegisterFixScreen implements InventoryHolder, Listener {
    private final ItemFixer plugin;
    private final Inventory inventory = Bukkit.createInventory(this, 27, "アイテム登録");
    private final ItemStack registerBarrier;
    private final ItemStack registerGlassPane;

    public RegisterFixScreen(ItemFixer plugin) {
        this.plugin = plugin;
        registerBarrier = Chain.of(new ItemStack(Material.BARRIER)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName(ChatColor.GREEN + "登録");
                    meta.setLore(Collections.singletonList(ChatColor.RED + "修正前/後のアイテムが指定されていません。"));
                    item.setItemMeta(meta);
                })
        ).getValue();
        registerGlassPane = Chain.of(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName(ChatColor.GREEN + "登録");
                    meta.setCustomModelData(plugin.getCustomModelDataGreenCheckMark());
                    item.setItemMeta(meta);
                })
        ).getValue();
        initInventory();
    }

    public void initInventory() {
        inventory.clear();
        ItemStack blackGlassPane = Chain.of(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName(" ");
                    item.setItemMeta(meta);
                })
        ).getValue();
        ItemStack arrowItem = Chain.of(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName("" + ChatColor.GOLD + ChatColor.BOLD + "<- 修正前のアイテム   ");
                    meta.setLore(Arrays.asList(
                            "",
                            "" + ChatColor.GOLD + ChatColor.BOLD + "   修正後のアイテム ->"
                    ));
                    meta.setCustomModelData(plugin.getCustomModelDataRightArrow());
                    item.setItemMeta(meta);
                })
        ).getValue();
        ItemStack cancelGlassPane = Chain.of(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName(ChatColor.RED + "キャンセル");
                    meta.setCustomModelData(plugin.getCustomModelDataRedCrossMark());
                    item.setItemMeta(meta);
                })
        ).getValue();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, blackGlassPane);
        }
        inventory.setItem(11, null);
        inventory.setItem(12, arrowItem);
        inventory.setItem(13, null);
        inventory.setItem(15, cancelGlassPane);
        inventory.setItem(16, registerBarrier);
    }

    public void recheckRegisterButton(Inventory inventory) {
        ItemStack left = inventory.getItem(11);
        if (left == null || left.getType().isAir()) {
            inventory.setItem(16, registerBarrier);
            return;
        }
        ItemStack right = inventory.getItem(13);
        if (right == null || right.getType().isAir()) {
            inventory.setItem(16, registerBarrier);
            return;
        }
        inventory.setItem(16, registerGlassPane);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getInventory().getHolder() instanceof RegisterFixScreen)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // the code MUST NOT use "this"
        if (!(e.getInventory().getHolder() instanceof RegisterFixScreen)) return;
        if (e.getClickedInventory() == null) return;
        e.setCancelled(true);
        if (e.getClickedInventory().getHolder() instanceof RegisterFixScreen) {
            if (e.getSlot() == 11 || e.getSlot() == 13) {
                ItemStack stack = e.getClickedInventory().getItem(e.getSlot());
                if (stack == null || stack.getType().isAir()) return;
                e.getClickedInventory().setItem(e.getSlot(), null);
                e.getWhoClicked().getInventory().addItem(stack);
                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_PICKUP, 2F, 1F);
                recheckRegisterButton(e.getClickedInventory());
            } else if (e.getSlot() == 15) {
                giveBackItems(e.getWhoClicked(), e.getClickedInventory());
                e.getWhoClicked().closeInventory();
            } else if (e.getSlot() == 16) {
                ItemStack left = e.getClickedInventory().getItem(11);
                if (left == null || left.getType().isAir()) return;
                ItemStack right = e.getClickedInventory().getItem(13);
                if (right == null || right.getType().isAir()) return;
                e.getClickedInventory().setItem(11, null);
                e.getClickedInventory().setItem(13, null);
                left.setAmount(1);
                right.setAmount(1);
                plugin.getItems().add(new AbstractMap.SimpleEntry<>(left, right));
                plugin.save();
                e.getWhoClicked().sendMessage(ChatColor.GREEN + "アイテムを登録しました。");
                e.getWhoClicked().closeInventory();
            }
        } else {
            ItemStack clicked = e.getClickedInventory().getItem(e.getSlot());
            if (clicked == null || clicked.getType().isAir()) return;
            ItemStack left = e.getInventory().getItem(11);
            if (left == null || left.getType().isAir()) {
                e.getClickedInventory().setItem(e.getSlot(), null);
                e.getInventory().setItem(11, clicked);
                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_PICKUP, 2F, 1F);
                recheckRegisterButton(e.getInventory());
                return;
            }
            ItemStack right = e.getInventory().getItem(13);
            if (right == null || right.getType().isAir()) {
                e.getClickedInventory().setItem(e.getSlot(), null);
                e.getInventory().setItem(13, clicked);
                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_PICKUP, 2F, 1F);
                recheckRegisterButton(e.getInventory());
                //return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof RegisterFixScreen)) return;
        giveBackItems(e.getPlayer(), e.getInventory());
    }

    public void giveBackItems(HumanEntity humanEntity, Inventory inventory) {
        ItemStack left = inventory.getItem(11);
        ItemStack right = inventory.getItem(13);
        inventory.setItem(11, null);
        inventory.setItem(13, null);
        if (left != null && !left.getType().isAir()) {
            humanEntity.getInventory().addItem(left);
        }
        if (right != null && !right.getType().isAir()) {
            humanEntity.getInventory().addItem(right);
        }
    }
}
