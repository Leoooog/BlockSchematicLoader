package eu.xgp.blockschematicloader;

import eu.xgp.blockschematicloader.commands.BslCommand;
import eu.xgp.blockschematicloader.commands.SchemCommands;
import eu.xgp.blockschematicloader.commands.StructureCommand;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;


public class BSLMain extends JavaPlugin {
    public static Economy econ;
    private static BSLMain instance;

    public static BSLMain getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().log(Level.SEVERE, "BSL disabled due to Vault dependency not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        instance = this;
        getServer().getConsoleSender().sendMessage(ChatColor.RED + getDescription().getName() + " has been enabled!");
        registerEvents();
        registerCommands();
        getUtils();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    private void registerCommands() {
        getCommand("bsl").setExecutor(new BslCommand());
        getCommand("addschem").setExecutor(new SchemCommands());
        getCommand("removeschem").setExecutor(new SchemCommands());
        getCommand("structureshop").setExecutor(new StructureCommand());
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
        return econ != null;
    }

    public Utils getUtils() {
        return new Utils(this);
    }


}
