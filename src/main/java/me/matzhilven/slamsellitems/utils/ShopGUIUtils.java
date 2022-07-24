package me.matzhilven.slamsellitems.utils;

import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopGUIUtils {

    public static double getWorth(Player player, ItemStack itemStack) {
        double worth = ShopGuiPlusApi.getItemStackPriceSell(player, itemStack);

        return worth == -1 ? 0 : worth;
    }

}
