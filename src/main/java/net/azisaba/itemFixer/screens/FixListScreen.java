package net.azisaba.itemFixer.screens;

import net.azisaba.itemFixer.ItemFixer;
import net.azisaba.itemFixer.util.Chain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FixListScreen implements InventoryHolder, Listener {
    private static final int ITEMS_PER_PAGE = 15;
    private final ItemFixer plugin;
    private final ItemStack arrowItem;
    private final ItemStack selectedArrowItem;
    private List<Map.Entry<ItemStack, ItemStack>> items;
    private Inventory inventory = Bukkit.createInventory(this, 54, "アイテムリスト (ページ1)");
    private int currentPage = 0;
    private int selectedIndex = -1;

    public FixListScreen(ItemFixer plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        arrowItem = Chain.of(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName("" + ChatColor.GOLD + ChatColor.BOLD + "<- 修正前のアイテム   ");
                    meta.setLore(Arrays.asList(
                            "",
                            "" + ChatColor.GOLD + ChatColor.BOLD + "   修正後のアイテム ->",
                            "",
                            "" + ChatColor.GRAY + "左クリックで並び替え",
                            "" + ChatColor.GRAY + "右クリックで削除"
                    ));
                    meta.setCustomModelData(plugin.getCustomModelDataRightArrow());
                    item.setItemMeta(meta);
                })
        ).getValue();
        selectedArrowItem = Chain.of(arrowItem.clone()).apply(item -> item.setType(Material.ORANGE_STAINED_GLASS_PANE)).getValue();
        refreshItemsAndScreen();
    }

    public void refreshItemsAndScreen() {
        this.items = plugin.getItems();
        refreshInventory(currentPage);
    }

    public void initInventory(/*@Range(from = 0, to = Integer.MAX_VALUE)*/ int page) {
        inventory = Bukkit.createInventory(this, 54, "アイテムリスト (ページ" + (page + 1) + ")");
        refreshInventory(page);
    }

    public void refreshInventory(int page) {
        inventory.clear();
        ItemStack blackGlassPane = Chain.of(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName(" ");
                    item.setItemMeta(meta);
                })
        ).getValue();
        ItemStack prevPage = Chain.of(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName(ChatColor.GOLD + "前のページ");
                    meta.setCustomModelData(plugin.getCustomModelDataLeftTriangle());
                    item.setItemMeta(meta);
                })
        ).getValue();
        ItemStack nextPage = Chain.of(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)).apply(item ->
                Chain.of(item.getItemMeta()).notNull().apply(meta -> {
                    meta.setDisplayName(ChatColor.GOLD + "次のページ");
                    meta.setCustomModelData(plugin.getCustomModelDataRightTriangle());
                    item.setItemMeta(meta);
                })
        ).getValue();
        int initialIndex = currentPage * ITEMS_PER_PAGE;
        for (int i = initialIndex; i < Math.min(items.size(), initialIndex + ITEMS_PER_PAGE); i++) {
            Map.Entry<ItemStack, ItemStack> entry = items.get(i);
            ItemStack left = entry.getKey();
            ItemStack right = entry.getValue();
            int slotIndex = (i - initialIndex) * 3;
            inventory.setItem(slotIndex, left);
            ItemStack item;
            if (selectedIndex == i) {
                item = selectedArrowItem;
            } else {
                item = arrowItem;
            }
            inventory.setItem(slotIndex + 1, item);
            inventory.setItem(slotIndex + 2, right);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, blackGlassPane);
        }
        if (hasPrevPage(page)) {
            inventory.setItem(45, prevPage);
        }
        if (hasNextPage(page)) {
            inventory.setItem(53, nextPage);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() != this) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getHolder() == this &&
                e.getClick() == ClickType.MIDDLE &&
                e.getSlot() < 45 &&
                (e.getSlot() - 1) % 3 != 0) {
            return;
        }
        e.setCancelled(true);
        if (e.getClickedInventory().getHolder() == this) {
            if (e.getSlot() < 45) {
                int relativeIndex = (int) Math.floor((double) e.getSlot() / 3);
                int absoluteIndex = currentPage * 15 + relativeIndex;
                if (e.getClick() == ClickType.LEFT || e.getClick() == ClickType.SHIFT_LEFT) {
                    // reorder
                    if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;
                    if (selectedIndex == -1) {
                        selectedIndex = absoluteIndex;
                        refreshInventory(currentPage);
                    } else {
                        if (selectedIndex == absoluteIndex) {
                            selectedIndex = -1;
                            refreshInventory(currentPage);
                            return;
                        }
                        int min = Math.min(selectedIndex, absoluteIndex);
                        int max = Math.max(selectedIndex, absoluteIndex);
                        Map.Entry<ItemStack, ItemStack> minEntry = items.get(min);
                        Map.Entry<ItemStack, ItemStack> maxEntry = items.get(max);
                        int minEntryAt = plugin.getItems().indexOf(minEntry);
                        int maxEntryAt = plugin.getItems().indexOf(maxEntry);
                        if (minEntryAt == -1 || maxEntryAt == -1) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "指定したFixはすでに削除されています。");
                        } else {
                            plugin.getItems().remove(maxEntryAt);
                            plugin.getItems().remove(minEntryAt);
                            plugin.getItems().add(minEntryAt, maxEntry);
                            plugin.getItems().add(maxEntryAt, minEntry);
                            e.getWhoClicked().sendMessage(ChatColor.GREEN + "指定したFixを並び替えました。");
                        }
                        selectedIndex = -1;
                        plugin.save();
                        refreshItemsAndScreen();
                    }
                } else if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
                    // remove
                    Map.Entry<ItemStack, ItemStack> entry = items.get(absoluteIndex);
                    if (plugin.getItems().remove(entry)) {
                        e.getWhoClicked().sendMessage(ChatColor.GREEN + "指定したFixを削除しました。");
                        plugin.save();
                    } else {
                        e.getWhoClicked().sendMessage(ChatColor.RED + "指定されたFixは存在しません。");
                    }
                    refreshItemsAndScreen();
                }
            } else if (e.getSlot() == 45) {
                // prev page
                if (hasPrevPage(currentPage)) {
                    currentPage--;
                    initInventory(currentPage);
                    e.getWhoClicked().openInventory(inventory);
                }
            } else if (e.getSlot() == 53) {
                // next page
                if (hasNextPage(currentPage)) {
                    currentPage++;
                    initInventory(currentPage);
                    e.getWhoClicked().openInventory(inventory);
                }
            }
        }
    }

    public boolean hasPrevPage(int page) {
        return page > 0;
    }

    public boolean hasNextPage(int page) {
        return ((int) Math.floor((double) items.size() / ITEMS_PER_PAGE)) > page;
    }
}
