package me.matzhilven.slamsellitems.utils;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {

    private ItemStack is;

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param m The material to create the ItemBuilder with.
     */
    public ItemBuilder(Material m) {
        this(m, 1);
    }

    /**
     * Create a new ItemBuilder over an existing itemstack.
     *
     * @param is The itemstack to create the ItemBuilder over.
     */
    public ItemBuilder(ItemStack is) {
        this.is = is;
    }

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param m      The material of the item.
     * @param amount The amount of the item.
     */
    public ItemBuilder(Material m, int amount) {
        is = new ItemStack(m, amount);
    }


    /**
     * Clone the ItemBuilder into a new one.
     *
     * @return The cloned instance.
     */
    public ItemBuilder clone() {
        return new ItemBuilder(is);
    }

    /**
     * Set the amount of the item
     *
     * @param amount
     * @return
     */
    public ItemBuilder setAmount(int amount) {
        is.setAmount(amount);
        return this;
    }

    /**
     * Set the displayname of the item.
     *
     * @param name The name to change it to.
     */
    public ItemBuilder setName(String name) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(StringUtils.colorize(name));
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add an enchant to the item.
     *
     * @param ench  The enchant to add
     * @param level The level
     */
    public ItemBuilder addEnchant(Enchantment ench, int level) {
        ItemMeta im = is.getItemMeta();
        im.addEnchant(ench, level, true);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addEnchants(List<String> stringList) {
        for (String s : stringList) {
            String[] split = s.split(";");
            addEnchant(Enchantment.getByName(split[0]), Integer.parseInt(split[1]));
        }
        return this;
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(String... lore) {
        ItemMeta im = is.getItemMeta();
        im.setLore(StringUtils.colorize(Arrays.asList(lore)));
        is.setItemMeta(im);
        return this;
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(List<String> lore) {
        ItemMeta im = is.getItemMeta();
        im.setLore(StringUtils.colorize(lore));
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder removeLine(String val) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (im.hasLore()) lore = new ArrayList<>(im.getLore());
        lore.removeIf(key -> key.contains(val));
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param line The lore line to add.
     */
    public ItemBuilder addLoreLine(String line) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (im.hasLore()) lore = new ArrayList<>(im.getLore());
        lore.add(StringUtils.colorize(line));
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Makes the current item glowing
     */
    public ItemBuilder addGlow(boolean add) {
        if (!add) return this;
        ItemMeta meta = is.getItemMeta();
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        is.setItemMeta(meta);
        return this;
    }

    /**
     * Add itemflags
     */
    public ItemBuilder addItemFlag(ItemFlag... itemFlags) {
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(itemFlags);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Replace placeholders in the lore
     *
     * @param placeholder The placeholder you want to replace
     * @param value       The value you want the placeholder to be
     */
    public ItemBuilder replace(String placeholder, String value) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = im.getLore();
        im.setLore(lore.stream().map(line -> line = line.replace(placeholder, value)).collect(Collectors.toList()));
        is.setItemMeta(im);
        return this;
    }

    /**
     * Replace placeholders in the lore and displayname
     *
     * @param placeholder The placeholder you want to replace
     * @param value       The value you want the placeholder to be
     */
    public ItemBuilder replaceAll(String placeholder, String value) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = im.getLore();
        im.setDisplayName(im.getDisplayName().replace(placeholder, value));
        im.setLore(lore.stream().map(line -> line = line.replace(placeholder, value)).collect(Collectors.toList()));
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addNBT(String key, String value) {
        NBTItem nbtItem = new NBTItem(is);
        nbtItem.setString(key, value);

        this.is = nbtItem.getItem();

        return this;
    }

    public ItemBuilder addNBT(String key, int value) {
        NBTItem nbtItem = new NBTItem(is);
        nbtItem.setInteger(key, value);

        this.is = nbtItem.getItem();

        return this;
    }

    /**
     * Retrieves the itemstack from the ItemBuilder.
     *
     * @return The itemstack created/modified by the ItemBuilder instance.
     */
    public ItemStack toItemStack() {
        return is;
    }
}

