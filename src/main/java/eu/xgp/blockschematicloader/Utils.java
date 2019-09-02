package eu.xgp.blockschematicloader;

import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import net.milkbowl.vault.economy.Economy;

public class Utils {
	private Plugin plugin;
	private HashMap<String, String> blockSchems = new HashMap<>();
	private FileConfiguration config;
	private Economy economy;
	private int schematics;

	public void saveConfig() {
		BSLMain.getInstance().saveConfig();
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public HashMap<String, String> getBlockSchems() {
		return blockSchems;
	}

	public Economy getEconomy() {
		return economy;
	}

	public int getSchematics() {
		return schematics;
	}

	public Utils(Plugin plugin) {
		this.plugin = plugin;
		config = plugin.getConfig();
		economy = BSLMain.econ;
		try {
			schematics = config.getConfigurationSection("schematics").getKeys(false).size();
		} catch (NullPointerException e) {
			schematics = 0;
		}
		blockSchems.clear();
		for (String key : config.getConfigurationSection("schematics").getKeys(false)) {
			blockSchems.put(config.getString("schematics." + key + ".name").replaceAll("&", "§"), "schematics." + key);
		}
		schematics = config.getConfigurationSection("schematics").getKeys(false).size();
	}

}
