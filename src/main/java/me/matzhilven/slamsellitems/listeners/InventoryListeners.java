package me.matzhilven.slamsellitems.listeners;

import me.matzhilven.slamsellitems.SlamSellItems;
import me.matzhilven.slamsellitems.chest.SellChest;
import me.matzhilven.slamsellitems.utils.ShopGUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class InventoryListeners implements Listener {

    private final SlamSellItems main;

    public InventoryListeners(SlamSellItems main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    private void onInventoryMoveItem(InventoryMoveItemEvent e) {
        Inventory destination = e.getDestination();
        Optional<SellChest> optionalSellChest = main.getChestManager().byInventory(destination);

        if (!optionalSellChest.isPresent()) return;
        SellChest chest = optionalSellChest.get();

        if (!chest.isLoaded()) return;

        ItemStack itemStack = e.getItem();

        Player player = Bukkit.getPlayer(optionalSellChest.get().getOwner());

        if (player == null) {
            chest.unload();
            return;
        }

        double price = ShopGUIUtils.getWorth(player, itemStack);
        if (price == -1.0) return;

        main.getEcon().depositPlayer(player, price);
        main.getChestManager().addItem(player, itemStack);

        e.setItem(new ItemStack(Material.AIR));

    }

}