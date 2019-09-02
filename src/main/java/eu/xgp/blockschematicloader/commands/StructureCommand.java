package eu.xgp.blockschematicloader.commands;

import eu.xgp.blockschematicloader.SchemProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StructureCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("structureshop") && sender instanceof Player) {
        	Player p = (Player)sender;
        	if(p.hasPermission("bsl.structureshop")) {
				SchemProvider.INVENTORY.open((Player) sender);
			}else{
        		p.sendMessage("§cHey, you cannot execute this command.");
			}

        } else {
			sender.sendMessage("§4Only players can execute this command.");
        }
        return false;
    }

}
