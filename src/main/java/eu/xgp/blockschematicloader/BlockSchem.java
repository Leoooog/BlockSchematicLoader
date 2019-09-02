package eu.xgp.blockschematicloader;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockSchem extends ItemStack {
	private String schematic;
	private String path;
	private double price;
	private static BSLMain main = BSLMain.getInstance();

	public BlockSchem(String schematic, Material type, String displayname, String path, double price) {
		super(type, 1);
		this.path = path;
		this.schematic = schematic;
		this.price = price;
		ItemMeta meta = getItemMeta();
		meta.setDisplayName(displayname);
		setItemMeta(meta);
		main.getUtils().getBlockSchems().put(getDisplayName(), path);
	}

	public double getPrice() {
		return price;
	}

	public String getSchematic() {
		return schematic;
	}

	public String getDisplayName() {
		return this.getItemMeta().getDisplayName();
	}

	public void save() {
		main.getConfig().set(path + ".schem", schematic);
		main.getConfig().set(path + ".type", getType().toString());
		main.getConfig().set(path + ".name", getDisplayName().replaceAll("§", "&"));
		main.getConfig().set(path + ".price", price);
		main.saveConfig();
	}

	public static BlockSchem getFromPath(String path) {
		String schem = main.getConfig().getString(path + ".schem");
		Material type = Material.valueOf(main.getConfig().getString(path + ".type"));
		String name = main.getConfig().getString(path + ".name").replaceAll("&", "§");
		double price = main.getConfig().getDouble(path+".price");
		return new BlockSchem(schem, type, name, path, price);
	}
}
