package me.matzhilven.slamsellitems.chest;

import me.matzhilven.slamsellitems.SlamSellItems;
import me.matzhilven.slamsellitems.utils.ShopGUIUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class SellChest {

    private final UUID uuid;
    private final UUID owner;
    private Block block;
    private Inventory defaultInventory;
    private boolean loaded;

    public SellChest(UUID uuid, UUID owner, Block block, boolean loaded) {
        this.uuid = uuid;
        this.owner = owner;
        this.block = block;
        this.loaded = loaded;

        if (block != null) defaultInventory = ((Chest) block.getState()).getBlockInventory();

    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return block.getLocation();
    }

    public Inventory getDefaultInventory() {
        return defaultInventory;
    }

    public void load(Player player, SlamSellItems main, Location location) {
        Block block = location.getWorld().getBlockAt(location);

        if (!(block.getState() instanceof Chest)) {
            main.getChestManager().removeChest(location, null, false);
            return;
        }

        block.setMetadata("sell_chest", new FixedMetadataValue(main, uuid));

        Chest chest = (Chest) block.getState();
        defaultInventory = chest.getBlockInventory();

        sellInventory(main, player);

        this.block = block;
        loaded = true;
    }

    public void sellInventory(SlamSellItems main, Player player) {
        for (ItemStack itemStack : defaultInventory.getContents()) {
            double price = ShopGUIUtils.getWorth(player, itemStack);
            if (price == 0.0) continue;

            main.getEcon().depositPlayer(player, price);
            main.getChestManager().addItem(player, itemStack);
            defaultInventory.remove(itemStack);
        }
    }

    public void unload() {
        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
