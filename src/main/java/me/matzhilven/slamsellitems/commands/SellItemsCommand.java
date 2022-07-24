package me.matzhilven.slamsellitems.commands;

import me.matzhilven.slamsellitems.SlamSellItems;
import me.matzhilven.slamsellitems.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SellItemsCommand implements CommandExecutor, TabExecutor {

    private final SlamSellItems main;

    public SellItemsCommand(SlamSellItems main) {
        this.main = main;
        main.getCommand("sellitems").setExecutor(this);
        main.getCommand("sellitems").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("slamsellitems.admin")) {
            StringUtils.sendMessage(sender, main.getConfig().getString("messages.invalid-perms"));
            return true;
        }

        if (args.length < 4) {
            if (args.length == 1 && args[0].equals("reload")) {
                main.reloadConfig();
                StringUtils.sendMessage(sender, main.getConfig().getString("messages.reloaded"));
                return true;
            }

            StringUtils.sendMessage(sender, main.getConfig().getString("messages.usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            StringUtils.sendMessage(sender, main.getConfig().getString("messages.invalid-player"));
            return true;
        }

        String item = args[2].toLowerCase();

        if (!item.equals("chest") && !item.equals("wand")) {
            StringUtils.sendMessage(sender, main.getConfig().getString("messages.invalid-item"));
            return true;
        }

        int wandUses = 10;

        if (item.equals("wand")) {
            if (args.length != 5) {
                StringUtils.sendMessage(sender, main.getConfig().getString("messages.usage"));
                return true;
            }

            if (args[4].equalsIgnoreCase("infinite")) {
                wandUses = -1;
            } else {
                try {
                    wandUses = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    StringUtils.sendMessage(sender, main.getConfig().getString("messages.invalid-number"));
                    return true;
                }

                if (wandUses <= 0) {
                    StringUtils.sendMessage(sender, main.getConfig().getString("messages.invalid-number"));
                    return true;
                }
            }
        }

        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            StringUtils.sendMessage(sender, main.getConfig().getString("messages.invalid-number"));
            return true;
        }

        if (amount <= 0) {
            StringUtils.sendMessage(sender, main.getConfig().getString("messages.invalid-number"));
            return true;
        }

        ItemStack itemStack;
        if (item.equals("chest")) {
            itemStack = main.getChestItem().clone().setAmount(amount)
                    .toItemStack();
        } else {
            itemStack = main.getWandItem().clone().setAmount(amount)
                    .addNBT("uses", wandUses)
                    .addNBT("total-uses", wandUses)
                    .replaceAll("%uses%", wandUses == -1 ? "Infinite" : StringUtils.format(wandUses))
                    .toItemStack();
        }

        String name = itemStack.getItemMeta().getDisplayName();
        target.getInventory().addItem(itemStack);


        StringUtils.sendMessage(sender, main.getConfig().getString("messages.given")
                .replace("%player%", target.getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", name)
        );
        StringUtils.sendMessage(target, main.getConfig().getString("messages.received")
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", name)
        );

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> cmds = new ArrayList<>();

        switch (args.length) {
            case 1:
                cmds.add("give");
                if (sender.hasPermission("slamsellitems.admin")) cmds.add("reload");
                return StringUtil.copyPartialMatches(args[0], cmds, new ArrayList<>());
            case 2:
                return StringUtil.copyPartialMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), new ArrayList<>());

            case 3:
                cmds.add("chest");
                cmds.add("wand");
                return StringUtil.copyPartialMatches(args[2], cmds, new ArrayList<>());
            case 4:
                cmds.add("1");
                cmds.add("5");
                cmds.add("10");
                return StringUtil.copyPartialMatches(args[3], cmds, new ArrayList<>());
            case 5:
                if (args[2].equalsIgnoreCase("chest")) return new ArrayList<>();
                cmds.add("100");
                cmds.add("500");
                cmds.add("infinite");
                return StringUtil.copyPartialMatches(args[4], cmds, new ArrayList<>());
        }

        return null;
    }
}
