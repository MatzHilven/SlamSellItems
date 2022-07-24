package me.matzhilven.slamsellitems.data;

import me.matzhilven.slamsellitems.SlamSellItems;
import me.matzhilven.slamsellitems.chest.SellChest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataConfig {

    private final File data;

    private final SlamSellItems main;

    private FileConfiguration config;

    public DataConfig(SlamSellItems main) {
        this.main = main;
        this.data = new File(main.getDataFolder(), "data.yml");

        if (!data.exists()) {
            data.getParentFile().mkdir();
            main.saveResource("data.yml", false);
        }

        config = new YamlConfiguration();

        try {
            config.load(data);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        loadData();
    }

    public void loadData() {
        if (config.get("chests") == null) return;

        config.getConfigurationSection("chests").getKeys(false).forEach(chest -> {
            main.getChestManager().addChest(config.getLocation("chests." + chest + ".location"),
                    new SellChest(
                            UUID.fromString(chest),
                            UUID.fromString(config.getString("chests." + chest + ".owner")),
                            null,
                            false
                    ));
        });

    }

    public void removeData(SellChest chest) {
        if (chest == null) return;
        config.set("chests." + chest.getUuid(), null);
        save();
    }

    public void saveData(SellChest chest) {
        UUID uuid = chest.getUuid();
        config.set("chests." + uuid + ".location", chest.getLocation());
        config.set("chests." + uuid + ".owner", chest.getOwner().toString());
        save();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(this.data);
    }

    public void save() {
        try {
            config.save(this.data);
        } catch (IOException e) {
            main.getServer().getConsoleSender().sendMessage("Error saving data.yml");
            e.printStackTrace();
        }
    }
}
