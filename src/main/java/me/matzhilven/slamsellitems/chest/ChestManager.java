package me.matzhilven.slamsellitems.chest;

import me.matzhilven.slamsellitems.SlamSellItems;
import me.matzhilven.slamsellitems.utils.ShopGUIUtils;
import me.matzhilven.slamsellitems.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ChestManager {

    private final SlamSellItems main;
    private final HashMap<Location, SellChest> chests;
    private final HashMap<InventoryHolder, SellChest> inventoryCache;
    private final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<ItemStack>> soldItems;

    public ChestManager(SlamSellItems main) {
        this.main = main;
        chests = new HashMap<>();
        inventoryCache = new HashMap<>();
        soldItems = new ConcurrentHashMap<>();

        Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {

            for (Map.Entry<UUID, ConcurrentLinkedQueue<ItemStack>> entry : soldItems.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if (player == null) continue;
                if (entry.getValue() == null) continue;
                if (entry.getValue().size() == 0) continue;

                double totalPrice = entry.getValue()
                        .stream()
                        .filter(Objects::nonNull)
                        .mapToDouble(itemStack -> ShopGUIUtils.getWorth(player, itemStack))
                        .sum();

                StringUtils.sendMessage(player, main.getConfig().getString("messages.sold-message")
                    .replace("%amount%", StringUtils.format(totalPrice))
                );

                StringUtils.sendMessage(player, " ");

                HashMap<Material, Long> items = new HashMap<>();
                final HashMap<Material, Double> priceMap = new HashMap<>();

                entry.getValue().forEach(itemStack -> {
                    if (itemStack == null) return;

                    items.computeIfPresent(itemStack.getType(), (material, aLong) -> aLong += itemStack.getAmount());
                    items.computeIfAbsent(itemStack.getType(), material -> (long) itemStack.getAmount());

                    if (!priceMap.containsKey(itemStack.getType())) {
                        priceMap.put(itemStack.getType(), ShopGUIUtils.getWorth(player, new ItemStack(itemStack.getType())));
                    }
                });


                items.forEach((material, amount) -> {
                    StringUtils.sendMessage(player, main.getConfig().getString("messages.sold-message-item")
                            .replace("%material%", material.toString())
                            .replace("%amount%", StringUtils.format(amount))
                            .replace("%price%", StringUtils.format(priceMap.get(material)))
                    );
                });

                StringUtils.sendMessage(player, " ");

                StringUtils.sendMessage(player, main.getConfig().getString("messages.sold-message-footer")
                        .replace("%amount%", StringUtils.format(totalPrice))
                );

                soldItems.put(entry.getKey(), new ConcurrentLinkedQueue<>());
            }

            soldItems.clear();
        }, 20L * 60L * main.getConfig().getInt("message-loop"), 20L * 60L * main.getConfig().getInt("message-loop"));
    }

    public void addItem(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();

        if (item == null) return;

        if (!soldItems.containsKey(uuid)) {
            soldItems.put(uuid, new ConcurrentLinkedQueue<ItemStack>() {{
                add(item);
            }});
            return;
        }

        soldItems.computeIfPresent(uuid, (uuid1, items) -> {
            items.add(item);
            return items;
        });
    }

    public void addChest(Block block, UUID owner) {
        SellChest sellChest = new SellChest(UUID.randomUUID(), owner, block, true);
        block.setMetadata("sell_chest", new FixedMetadataValue(main, sellChest.getUuid().toString()));

        chests.put(block.getLocation(), sellChest);
        main.getDataConfig().saveData(sellChest);
    }

    public void addChest(Location location, SellChest infiniteChest) {
        chests.put(location, infiniteChest);
    }

    public void removeChest(Location location, Player player, boolean drop) {
        SellChest chest = chests.remove(location);
        main.getDataConfig().removeData(chest);
        if (drop) {
            if (player.getInventory().firstEmpty() == -1) {
                location.getWorld().dropItem(location, main.getChestItem().toItemStack());
            } else {
                player.getInventory().addItem(main.getChestItem().toItemStack());
            }

        }
    }

    public Optional<SellChest> byInventory(Inventory inventory) {
        if (inventory == null) return Optional.empty();

        if (!inventoryCache.containsKey(inventory.getHolder())) {
            Optional<SellChest> optionalSellChest = chests.values()
                    .stream()
                    .filter(chest -> chest.getDefaultInventory() != null)
                    .filter(chest -> chest.getDefaultInventory().toString().equals(inventory.toString()))
                    .findFirst();

            optionalSellChest.ifPresent(chest -> inventoryCache.put(inventory.getHolder(), chest));

            return optionalSellChest;
        }
        return Optional.of(inventoryCache.get(inventory.getHolder()));
    }

    public List<Map.Entry<Location, SellChest>> getChests(UUID owner) {
        return chests.entrySet().stream().filter(entry -> entry.getValue().getOwner().equals(owner)).collect(Collectors.toList());
    }

    public void loadChests(Player player) {
        main.getChestManager().getChests(player.getUniqueId()).forEach(entry -> entry.getValue().load(player, main, entry.getKey()));
    }

    public void unloadChests(Player player) {
        getChests(player.getUniqueId()).forEach(entry -> {
            main.getDataConfig().saveData(entry.getValue());
            entry.getValue().unload();
        });
    }

}
