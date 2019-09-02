package eu.xgp.blockschematicloader;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator.Type;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SchemProvider implements InventoryProvider {
    private BSLMain main = BSLMain.getInstance();
    public static SmartInventory INVENTORY = SmartInventory.builder().id("customInventory")
            .provider(new SchemProvider()).size(4, 9).title(ChatColor.RED + "Schematics Store!").closeable(true)
            .build();

    @Override
    public void init(Player p, InventoryContents contents) {
        Pagination pag = contents.pagination();
        List<ClickableItem> itemss = new ArrayList<>();
        main.getUtils().getBlockSchems().values().forEach((s) -> {
            BlockSchem schem = BlockSchem.getFromPath(s);
            ItemMeta schemeta = schem.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7§oPrice: " + schem.getPrice());
            schemeta.setLore(lore);
            schem.setItemMeta(schemeta);
            itemss.add(ClickableItem.of(schem, e -> {
                Player clicker = (Player) e.getWhoClicked();
                if (clicker.hasPermission("bsl.buy." + schem.getSchematic())) {
                    sellStructure(clicker, schem);
                    ItemStack money = new ItemStack(Material.GOLD_INGOT);
                    ItemMeta moneymeta = money.getItemMeta();
                    moneymeta.setDisplayName("§aMoney: " + main.getUtils().getEconomy().getBalance(p));
                    money.setItemMeta(moneymeta);
                    contents.set(2, 7, ClickableItem.empty(money));
                } else {
                    clicker.sendMessage("§4You cannot buy the schematic " + schem.getDisplayName() + "§4.");
                    p.closeInventory();
                }
            }));
        });
        pag.setItems(itemss.toArray(new ClickableItem[itemss.size()]));
        pag.setItemsPerPage(7);
        pag.addToIterator(contents.newIterator(Type.HORIZONTAL, 1, 1));
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closemeta = close.getItemMeta();
        closemeta.setDisplayName("§4Close");
        close.setItemMeta(closemeta);
        contents.set(2, 4, ClickableItem.of(close, e -> INVENTORY.close(p)));

        ItemStack back = new ItemStack(Material.PAPER);
        ItemMeta backmeta = back.getItemMeta();
        backmeta.setDisplayName("§o<- Previous");
        back.setItemMeta(backmeta);
        ItemStack after = new ItemStack(Material.PAPER);
        ItemMeta aftermeta = after.getItemMeta();
        aftermeta.setDisplayName("§oNext ->");
        after.setItemMeta(aftermeta);
        contents.set(2, 6, ClickableItem.of(after,
                e -> SchemProvider.INVENTORY.open((Player) e.getWhoClicked(), pag.next().getPage())));
        contents.set(2, 2, ClickableItem.of(back,
                e -> SchemProvider.INVENTORY.open((Player) e.getWhoClicked(), pag.previous().getPage())));

        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE)));
        ItemStack money = new ItemStack(Material.GOLD_INGOT);
        ItemMeta moneymeta = money.getItemMeta();
        moneymeta.setDisplayName("§aMoney: " + main.getUtils().getEconomy().getBalance(p));
        money.setItemMeta(moneymeta);
        contents.set(2, 7, ClickableItem.empty(money));

        ItemStack page = new ItemStack(Material.BRICKS);
        ItemMeta pagemeta = page.getItemMeta();
        pagemeta.setDisplayName("§5Page §5§o#" + (contents.pagination().getPage() + 1));
        page.setItemMeta(pagemeta);
        contents.set(2, 1, ClickableItem.empty(page));

    }

    private void sellStructure(Player target, BlockSchem schem) {
        if (main.getUtils().getEconomy().getBalance(target) < schem.getPrice()) {
            target.sendMessage(ChatColor.RED + "Non hai abbastanza soldi.\nSoldi mancanti: "
                    + (schem.getPrice() - main.getUtils().getEconomy().getBalance(target))
                    + main.getUtils().getEconomy().currencyNamePlural());
            return;
        }
        main.getUtils().getEconomy().withdrawPlayer(target, schem.getPrice());
        target.sendMessage("§aHai comprato la schematic " + schem.getDisplayName() + "§a per il prezzo di "
                + schem.getPrice() + "\n§aIl tuo bilancio ora è: " + main.getUtils().getEconomy().getBalance(target));
        target.getInventory().addItem(schem);
    }

    @Override
    public void update(Player p, InventoryContents contents) {

    }

}
