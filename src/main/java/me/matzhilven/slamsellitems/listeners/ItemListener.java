package me.matzhilven.slamsellitems.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.tr7zw.nbtapi.NBTItem;
import me.matzhilven.slamsellitems.SlamSellItems;
import me.matzhilven.slamsellitems.listeners.packetwrapper.WrapperPlayServerSetSlot;
import me.matzhilven.slamsellitems.listeners.packetwrapper.WrapperPlayServerWindowItems;
import me.matzhilven.slamsellitems.utils.ItemBuilder;
import me.matzhilven.slamsellitems.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ItemListener extends PacketAdapter {

    private final SlamSellItems main;
    private final List<String> lore;

    public ItemListener(Plugin plugin) {
        super(plugin, PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT);
        this.main = (SlamSellItems) plugin;
        this.lore = main.getConfig().getStringList("sell-wand-item.lore");
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        if (packet.getType() == PacketType.Play.Server.SET_SLOT) {
            WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(packet);
            if (setSlot.getSlotData() != null) {
                ItemStack toModify = setSlot.getSlotData();

                if (modify(toModify) == null) return;

                setSlot.setSlotData(modify(toModify));
            }
        } else if (packet.getType() == PacketType.Play.Server.WINDOW_ITEMS) {
            WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(packet);
            ArrayList<ItemStack> result = new ArrayList<>();
            for (ItemStack item : windowItems.getSlotData()) {
                if (item == null || item.getType() == Material.AIR) {
                    result.add(item);
                    continue;
                }
                if (modify(item) == null) {
                    result.add(item);
                    continue;
                }
                result.add(modify(item));
            }
            windowItems.setSlotData(result);
        }

    }

    private ItemStack modify(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;

        NBTItem nbtItem = new NBTItem(item);

        if (!nbtItem.hasKey("sell_wand")) return null;

        return new ItemBuilder(item)
                .setLore(lore)
                .replaceAll("%uses-left%", nbtItem.getInteger("uses") == -1 ? "Infinite" : StringUtils.format(nbtItem.getInteger("uses")))
                .toItemStack();
    }

}
