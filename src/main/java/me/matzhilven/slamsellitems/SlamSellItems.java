package me.matzhilven.slamsellitems;

import me.matzhilven.slamsellitems.chest.ChestManager;
import me.matzhilven.slamsellitems.commands.SellItemsCommand;
import me.matzhilven.slamsellitems.data.PlayerDataFile;
import me.matzhilven.slamsellitems.listeners.InventoryListeners;
import me.matzhilven.slamsellitems.listeners.ItemListener;
import me.matzhilven.slamsellitems.listeners.PlayerListeners;
import me.matzhilven.slamsellitems.utils.ItemBuilder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public final class SlamSellItems extends JavaPlugin {

    private ChestManager chestManager;

    private ItemBuilder sellChest;
    private ItemBuilder sellWand;

    private Economy econ = null;

    private HashMap<UUID, PlayerDataFile> dataCache;

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
        dataCache = new HashMap<>();

        saveDefaultConfig();
        loadCache();

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

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            chestManager.unloadChests(player);
            chestManager.saveChests(player);
        });
    }

    private void loadCache() {
        File dataFolder = new File(getDataFolder(), "playerdata");

        if (!dataFolder.exists()) dataFolder.mkdirs();

        for (File file : dataFolder.listFiles()) {
            YamlConfiguration configuration = new YamlConfiguration();

            try {
                configuration.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }

            PlayerDataFile dataFile = new PlayerDataFile(this, file);
            dataFile.load();

            dataCache.put(UUID.fromString(file.getName().replace(".yml", "")), dataFile);
        }
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

    public ItemBuilder getChestItem() {
        return sellChest;
    }

    public ItemBuilder getWandItem() {
        return sellWand;
    }

    public Economy getEcon() {
        return econ;
    }

    public HashMap<UUID, PlayerDataFile> getDataCache() {
        return dataCache;
    }

}
