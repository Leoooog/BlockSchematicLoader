package eu.xgp.blockschematicloader.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import eu.xgp.blockschematicloader.BSLMain;
import eu.xgp.blockschematicloader.BlockSchem;
import eu.xgp.blockschematicloader.Utils;
import net.md_5.bungee.api.ChatColor;

public class SchemCommands implements CommandExecutor {
	
	private BSLMain main = BSLMain.getInstance();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender.hasPermission("bsl.manage")) {
			if (cmd.getName().equalsIgnoreCase("addschem")) {
				if (args.length != 4) {
					sender.sendMessage(ChatColor.RED + "Uso: /addschem <name> <material> <schematic> <price>");
					return false;
				}
				File dir = new File(
						Bukkit.getPluginManager().getPlugin("WorldEdit").getDataFolder() + File.separator + "schematics");
				List<File> files = Arrays.asList(dir.listFiles());
				List<String> filenames = new ArrayList<>();
				files.forEach((file) -> filenames.add(file.getName()));
				if (filenames.contains(args[2] + ".schem")) {
					if (main.getConfig().getConfigurationSection("schematics." + args[2]) != null) {
						sender.sendMessage(ChatColor.RED + "La BlockSchematic '" + args[2] + "' gia' esiste!");
						return false;
					} else {
						try {
							Material m = Material.valueOf(args[1].toUpperCase());
							BlockSchem schem = new BlockSchem(args[2], m, args[0].replaceAll("&", "§"),
									"schematics." + args[2], Double.parseDouble(args[3]));
							schem.save();
							main.getUtils().getBlockSchems().put(args[0].replaceAll("&", "§"), "schematics." + args[2]);
							sender.sendMessage(ChatColor.GREEN + "Schematic '" + args[2] + "' salvata!");
							main.getUtils();
						} catch (Exception e) {
							sender.sendMessage(ChatColor.RED + "Il materiale " + args[1] + " non e' valido!");
						}
					}
				} else {
					sender.sendMessage(ChatColor.RED + "La schematic '" + args[2] + "' non esiste!");
					return false;
				}

			} else if (cmd.getName().equalsIgnoreCase("removeschem")) {
				if (args.length != 1) {
					sender.sendMessage(ChatColor.RED + "Uso: /removeschem <schematic>");
					return false;
				}
				if (main.getConfig().get("schematics." + args[0]) == null) {
					sender.sendMessage(ChatColor.RED + "La schematic '" + args[0] + "' non esiste!");
					return false;
				}
				main.getUtils().getBlockSchems().remove(main.getConfig().getString("schematics." + args[0] + ".name").replaceAll("&", "§"));
				main.getConfig().set("schematics." + args[0], null);
				main.saveConfig();
				main.getUtils();
				sender.sendMessage(ChatColor.GREEN + "La schematic '" + args[0] + "' e' stata eliminata!");
			}
		}else{
			sender.sendMessage("§cYou cannot execute this command.");
		}
		return false;
	}

}
