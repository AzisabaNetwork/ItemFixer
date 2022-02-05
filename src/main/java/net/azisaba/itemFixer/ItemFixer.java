package net.azisaba.itemFixer;

import net.azisaba.itemFixer.commands.FixItemsCommand;
import net.azisaba.itemFixer.commands.ItemFixerCommand;
import net.azisaba.itemFixer.listeners.FixItemsOnJoinListener;
import net.azisaba.itemFixer.screens.RegisterFixScreen;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemFixer extends JavaPlugin {
    private final List<Map.Entry<ItemStack, ItemStack>> items = new ArrayList<>();
    private int customModelDataRightArrow = 0;
    private int customModelDataRightTriangle = 0;
    private int customModelDataLeftTriangle = 0;
    private int customModelDataGreenCheckMark = 0;
    private int customModelDataRedCrossMark = 0;

    @Override
    public void onEnable() {
        reload();
        Bukkit.getPluginManager().registerEvents(new RegisterFixScreen(this), this);
        Bukkit.getPluginManager().registerEvents(new FixItemsOnJoinListener(this), this);
        Objects.requireNonNull(Bukkit.getPluginCommand("fixitems")).setExecutor(new FixItemsCommand(this));
        Objects.requireNonNull(Bukkit.getPluginCommand("itemfixer")).setExecutor(new ItemFixerCommand(this));
    }

    public void save() {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Map.Entry<ItemStack, ItemStack> entry : new ArrayList<>(items)) {
            Map<String, Object> map = new HashMap<>();
            map.put("left", entry.getKey().serialize());
            map.put("right", entry.getValue().serialize());
            mapList.add(map);
        }
        getConfig().set("items", mapList);
        try {
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdir();
            }
            getConfig().save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().severe("Error saving config.yml");
            e.printStackTrace();
        }
    }

    /**
     * Reloads the config from config.yml.
     */
    @SuppressWarnings("unchecked")
    public void reload() {
        reloadConfig();
        items.clear();
        for (Map<?, ?> map : getConfig().getMapList("items")) {
            Object ol = map.get("left");
            Object or = map.get("right");
            ItemStack left = null;
            ItemStack right = null;
            if (ol instanceof ItemStack) {
                left = (ItemStack) ol;
            } else if (ol instanceof Map<?, ?>) {
                left = ItemStack.deserialize((Map<String, Object>) ol);
            }
            if (or instanceof ItemStack) {
                right = (ItemStack) or;
            } else if (or instanceof Map<?, ?>) {
                right = ItemStack.deserialize((Map<String, Object>) or);
            }
            if (left != null && right != null) {
                items.add(new AbstractMap.SimpleEntry<>(left, right));
            }
        }
        customModelDataRightArrow = getConfig().getInt("customModelData.rightArrow", 0);
        customModelDataRightTriangle = getConfig().getInt("customModelData.rightTriangle", 0);
        customModelDataLeftTriangle = getConfig().getInt("customModelData.leftTriangle", 0);
        customModelDataGreenCheckMark = getConfig().getInt("customModelData.greenCheckMark", 0);
        customModelDataRedCrossMark = getConfig().getInt("customModelData.redCrossMark", 0);
    }

    public List<Map.Entry<ItemStack, ItemStack>> getItems() {
        return items;
    }

    /**
     * Config Path: customModelData.rightArrow
     * @return Custom Model Data value of right arrow texture
     */
    public int getCustomModelDataRightArrow() {
        return customModelDataRightArrow;
    }

    /**
     * Config Path: customModelData.rightTriangle
     * @return Custom Model Data value of right triangle texture
     */
    public int getCustomModelDataRightTriangle() {
        return customModelDataRightTriangle;
    }

    /**
     * Config Path: customModelData.leftTriangle
     * @return Custom Model Data value of left triangle texture
     */
    public int getCustomModelDataLeftTriangle() {
        return customModelDataLeftTriangle;
    }

    /**
     * Config Path: customModelData.greenCheckMark
     * @return Custom Model Data value of green check mark texture
     */
    public int getCustomModelDataGreenCheckMark() {
        return customModelDataGreenCheckMark;
    }

    /**
     * Config Path: customModelData.redCrossMark
     * @return Custom Model Data value of red cross mark texture
     */
    public int getCustomModelDataRedCrossMark() {
        return customModelDataRedCrossMark;
    }

    public void fixNow(Player player) {
        int count = 0;
        Inventory inventory = player.getInventory();
        Inventory enderChest = player.getEnderChest();
        for (Map.Entry<ItemStack, ItemStack> entry : items) {
            count += fixItems(entry, inventory);
            count += fixItems(entry, enderChest);
        }
        player.sendMessage(ChatColor.LIGHT_PURPLE + "[ItemFixer] " + ChatColor.GREEN + count + "件のアイテムを修正しました。");
    }

    /**
     * Fixes all items in the inventory.
     * @param entry current entry
     * @param inventory the inventory
     * @return sum of fixed items
     */
    private int fixItems(Map.Entry<ItemStack, ItemStack> entry, Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && entry.getKey().isSimilar(item)) {
                ItemStack newItem = entry.getValue().clone();
                newItem.setAmount(item.getAmount());
                inventory.setItem(i, newItem);
                getLogger().info("Replaced item from " + item + " -> " + newItem);
                count++;
            }
        }
        return count;
    }
}
