package me.matzhilven.slamsellitems;

import com.comphenix.protocol.ProtocolLibrary;
import me.matzhilven.slamsellitems.chest.ChestManager;
import me.matzhilven.slamsellitems.commands.SellItemsCommand;
import me.matzhilven.slamsellitems.data.DataConfig;
import me.matzhilven.slamsellitems.listeners.InventoryListeners;
import me.matzhilven.slamsellitems.listeners.ItemListener;
import me.matzhilven.slamsellitems.listeners.PlayerListeners;
import me.matzhilven.slamsellitems.utils.ItemBuilder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class SlamSellItems extends JavaPlugin {

    private ChestManager chestManager;
    private DataConfig dataConfig;

    private ItemBuilder sellChest;
    private ItemBuilder sellWand;

    private Economy econ = null;

    @Override
    public void onEnable() {

        if (!setupEconomy()) {
            Bukkit.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("PlotSquared") == null) {
            Bukkit.getLogger().severe(String.format("[%s] Disabled due to no PlotSquared dependency found!", getDescription().getName()));
            setEnabled(false);
            return;
        }

        chestManager = new ChestManager(this);

        saveFiles();

        new SellItemsCommand(this);

        new PlayerListeners(this);
        new InventoryListeners(this);

        new ItemListener(this);

        sellChest = new ItemBuilder(Material.CHEST)
                .setName(getConfig().getString("sell-chest-item.name"))
                .setLore(getConfig().getStringList("sell-chest-item.lore"))
                .addGlow(true)
                .addNBT("sell_chest", "");

        sellWand = new ItemBuilder(Material.matchMaterial(getConfig().getString("sell-wand-item.material")))
                .setName(getConfig().getString("sell-wand-item.name"))
                .setLore(getConfig().getStringList("sell-wand-item.lore"))
                .addGlow(getConfig().getBoolean("sell-wand-item.glow"))
                .addNBT("sell_wand", "");
    }

    private void saveFiles() {
        saveDefaultConfig();
        dataConfig = new DataConfig(this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public DataConfig getDataConfig() {
        return dataConfig;
    }

    public ItemBuilder getChestItem() {
        return sellChest;
    }

    public ItemBuilder getWandItem() {
        return sellWand;
    }

    public Economy getEcon() {
        return econ;
    }
}
