package me.matzhilven.slamsellitems.data;

import me.matzhilven.slamsellitems.SlamSellItems;
import me.matzhilven.slamsellitems.chest.SellChest;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class PlayerDataFile {

    private final SlamSellItems main;

    private final String name;
    private final File file;

    private FileConfiguration config;

    public PlayerDataFile(SlamSellItems main, File file) {
        this.main = main;
        this.name = "/playerdata/" + file.getName();
        this.file = new File(main.getDataFolder(), name);

        setup();
        load();
    }

    public PlayerDataFile(SlamSellItems main, Player player) {
        this.main = main;
        this.name = "/playerdata/" + player.getUniqueId() + ".yml";
        this.file = new File(main.getDataFolder(), name);

        setup();
    }

    private void setup() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
            }
        }

        config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            main.getServer().getConsoleSender().sendMessage("Error saving " + name);
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveData(UUID uuid) {
        List<SellChest> sellChests = main.getChestManager().getPlayerChests(uuid);

        if (sellChests.size() == 0) {
            config.set("chests", null);
            save();
            return;
        }

        int id = 1;
        for (SellChest sellChest : sellChests) {
            config.set("chests." + id + ".location", sellChest.getLocation());
            id++;
        }

        save();
    }

    public void load() {
        if (!config.isConfigurationSection("chests")) return;

        UUID owner = UUID.fromString(file.getName().replace(".yml", ""));

        for (String id : config.getConfigurationSection("chests").getKeys(false)) {
            Location location = config.getLocation("chests." + id + ".location");

            SellChest sellChest = new SellChest(owner, null, false);
            main.getChestManager().addChest(location, sellChest);
        }

    }

}
