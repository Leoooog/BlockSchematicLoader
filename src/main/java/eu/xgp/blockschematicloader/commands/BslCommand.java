package eu.xgp.blockschematicloader.commands;

import eu.xgp.blockschematicloader.BSLMain;
import eu.xgp.blockschematicloader.BlockSchem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BslCommand implements CommandExecutor {
    private BSLMain main = BSLMain.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bsl")) {
            if (sender.hasPermission("bsl.give")) {
                if (args.length < 2) {
                    sender.sendMessage("Uso: /bsl <player> <schematicname> [true/false]");
                    return false;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(args[0] + " non e' online");
                    return false;
                }
                BlockSchem schem = BlockSchem.getFromPath("schematics." + args[1]);

                if (Boolean.valueOf(args[2])) {
                    if (main.getUtils().getEconomy().getBalance(target) < schem.getPrice()) {
                        sender.sendMessage(
                                ChatColor.RED + "Il player " + target.getDisplayName() + " non ha abbastanza soldi.\nMoney: "
                                        + main.getUtils().getEconomy().getBalance(target) + main.getUtils().getEconomy().currencyNamePlural());
                        return false;
                    } else {
                        main.getUtils().getEconomy().withdrawPlayer(target, schem.getPrice());
                        target.sendMessage("§aHai comprato la schematic " + schem.getDisplayName() + "§a per il prezzo di "
                                + schem.getPrice() + "\n§aIl tuo bilancio ora è: " + main.getUtils().getEconomy().getBalance(target));
                        sender.sendMessage(target.getDisplayName() + " §aha ricevuto la schematic " + schem.getDisplayName());
                    }
                } else {
                    target.sendMessage("§aTi è stata donata la schematic " + schem.getDisplayName() + " §ada " + sender.getName());
                    sender.sendMessage(target.getDisplayName() + " §aha ricevuto la schematic " + schem.getDisplayName());
                }


                ItemMeta schemeta = schem.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add("§7§oPrice: " + schem.getPrice());
                schemeta.setLore(lore);
                schem.setItemMeta(schemeta);

                target.getInventory().addItem(schem);
                target.updateInventory();

                return false;
            }
        }else{
            sender.sendMessage("§cYou cannot execute this command.");
        }
        return false;
    }

}
