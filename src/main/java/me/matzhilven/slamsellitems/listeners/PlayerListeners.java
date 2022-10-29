package me.matzhilven.slamsellitems.listeners;

import de.tr7zw.nbtapi.NBTItem;
import me.matzhilven.slamsellitems.SlamSellItems;
import me.matzhilven.slamsellitems.chest.SellChest;
import me.matzhilven.slamsellitems.utils.ShopGUIUtils;
import me.matzhilven.slamsellitems.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PlayerListeners implements Listener {

    private final static BlockFace[] BLOCK_FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private final SlamSellItems main;

    public PlayerListeners(SlamSellItems main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        main.getChestManager().loadChests(e.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        main.getChestManager().unloadChests(e.getPlayer());
        main.getChestManager().saveChests(e.getPlayer());
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().getType() != Material.CHEST) return;

        Block block = e.getBlock();

        NBTItem nbtItem = new NBTItem(e.getItemInHand());
        if (!nbtItem.hasKey("sell_chest")) {
            for (BlockFace face : BLOCK_FACES) {
                Block loopBlock = block.getRelative(face);
                if (loopBlock.equals(block)) continue;
                if (loopBlock.getType() == Material.CHEST && main.getChestManager().isSellChest(loopBlock.getLocation())) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }

        for (BlockFace face : BLOCK_FACES) {
            Block loopBlock = block.getRelative(face);
            if (loopBlock.equals(block)) continue;
            if (loopBlock.getType() == Material.CHEST) {
                e.setCancelled(true);
                return;
            }
        }

        main.getChestManager().addChest(e.getBlock(), e.getPlayer().getUniqueId());

        StringUtils.sendMessage(e.getPlayer(), main.getConfig().getString("messages.placed"));
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.CHEST) return;
        if (!main.getChestManager().isSellChest(e.getBlock().getLocation())) return;

        e.setDropItems(false);
        main.getChestManager().removeChest(e.getBlock().getLocation(), e.getPlayer(), true);
        StringUtils.sendMessage(e.getPlayer(), main.getConfig().getString("messages.removed"));
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock() || !event.hasItem() || event.getClickedBlock() == null
                || event.getItem() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        NBTItem nbtItem = new NBTItem(event.getItem());

        if (block.getType() != Material.CHEST || !nbtItem.hasKey("sell_wand")) return;

        if (event.isCancelled()) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        double price = 0;

        Inventory inventory = ((Chest) block.getState()).getInventory();

        ItemStack[] contents;
        if (inventory instanceof DoubleChestInventory) {
            DoubleChestInventory doubleChestInventory = (DoubleChestInventory) inventory;
            contents = doubleChestInventory.getContents();
        } else {
            contents = inventory.getContents();
        }

        for (ItemStack itemStack : contents) {
            if (itemStack == null) continue;
            double loopPrice = ShopGUIUtils.getWorth(player, itemStack);

            if (loopPrice == 0.0) continue;

            price += loopPrice;
            inventory.remove(itemStack);
        }

        if (price == 0) return;

        main.getEcon().depositPlayer(player, price);
        StringUtils.sendMessage(player, main.getConfig().getString("messages.sold-contents")
                .replace("%price%", StringUtils.format(price)));

        int uses = nbtItem.getInteger("uses");
        if (uses == -1) return;

        if (--uses <= 0) {
            StringUtils.sendMessage(player, main.getConfig().getString("messages.sellwand-broke"));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            return;
        }

        nbtItem.setInteger("uses", uses);
        player.getInventory().setItemInMainHand(nbtItem.getItem());
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        Optional<SellChest> optionalSellChest = main.getChestManager().byInventory(event.getClickedInventory());

        if (!optionalSellChest.isPresent()) {
            optionalSellChest = main.getChestManager().byInventory(event.getView().getTopInventory());

            if (!optionalSellChest.isPresent() ||
                    (event.getRawSlot() >= event.getView().getTopInventory().getSize() &&
                            event.getClick() != ClickType.SHIFT_LEFT &&
                            event.getClick() != ClickType.SHIFT_RIGHT)) return;
        }

        SellChest sellChest = optionalSellChest.get();

        Player chestOwner = Bukkit.getPlayer(sellChest.getOwner());
        if (chestOwner == null) return;

        if (event.getClick() == ClickType.NUMBER_KEY) {
            optionalSellChest.get().sellInventory(main, chestOwner);
            chestOwner.updateInventory();
        }

        ItemStack item = event.getCursor();
        boolean cursor = true;

        if (item == null || item.getType() == Material.AIR) {
            item = event.getCurrentItem();

            if (item == null || item.getType() == Material.AIR) return;
            cursor = false;
        }


        double price = ShopGUIUtils.getWorth(chestOwner, item);

        if (price == -1.0) return;

        main.getEcon().depositPlayer(chestOwner, price);
        main.getChestManager().addItem(chestOwner, item);

        if (cursor) {
            event.setCursor(new ItemStack(Material.AIR));
        } else {
            event.setCurrentItem(new ItemStack(Material.AIR));
        }

    }
}
