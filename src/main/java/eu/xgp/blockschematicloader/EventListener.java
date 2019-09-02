package eu.xgp.blockschematicloader;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.minecraft.server.v1_14_R1.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;

public class EventListener implements Listener {
    private BSLMain main = BSLMain.getInstance();

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block b = event.getBlock();
        Player p = event.getPlayer();
        if (main.getUtils().getBlockSchems()
                .containsKey(p.getInventory().getItemInMainHand().getItemMeta().getDisplayName())) {
            BlockSchem schem = BlockSchem.getFromPath(main.getUtils().getBlockSchems()
                .get(p.getInventory().getItemInMainHand().getItemMeta().getDisplayName()));
            if(p.hasPermission("bsl.place."+schem.getSchematic())) {
                b.setType(Material.AIR);
                if (p.isSneaking())
                    event.setCancelled(true);
                pasteSchematic(schem, p, b.getLocation());
            }else{
                p.sendMessage("§cYou can't place the schematic '"+schem.getDisplayName()+"§c'.");
            }
        }
    }

    private void pasteSchematic(BlockSchem schem, Player p, Location b) {
        try {

            File file = new File(Bukkit.getPluginManager().getPlugin("WorldEdit").getDataFolder() + File.separator
                    + "schematics" + File.separator + schem.getSchematic() + ".schem");
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            ClipboardReader reader = format.getReader(new FileInputStream(file));
            Clipboard clipboard = reader.read();
            String dir = dir(p);
            if (!p.isSneaking()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                                .getEditSession(new BukkitWorld(b.getWorld()), -1)) {
                            ClipboardHolder operation = new ClipboardHolder(clipboard);
                            AffineTransform transform = new AffineTransform();
                            if (dir.equalsIgnoreCase("S")) {
                                transform = transform.rotateY(-180);
                            } else if (dir.equalsIgnoreCase("E")) {
                                transform = transform.rotateY(-90);
                            } else if (dir.equalsIgnoreCase("W")) {
                                transform = transform.rotateY(-270);
                            }
                            operation.setTransform(operation.getTransform().combine(transform));
                            Operation op = operation.createPaste(editSession)
                                    .to(BlockVector3.at(b.getX(), b.getY(), b.getZ())).ignoreAirBlocks(false).build();
                            Operations.complete(op);

                        } catch (WorldEditException e) {
                            e.printStackTrace();
                        }
                    }

                }.runTask(BSLMain.getInstance());
            } else {
                Region reg = clipboard.getRegion();

                Location loc = b.clone();
                loc.add(reg.getLength(), reg.getHeight(), reg.getWidth());

                CuboidRegion rreg = new CuboidRegion(reg.getWorld(), reg.getMaximumPoint(), reg.getMinimumPoint());
                rreg.setWorld(new BukkitWorld(b.getWorld()));
                switch (dir) {
                    case "S":
                        rreg.setPos1(BlockVector3.at(b.getBlockX() - reg.getWidth() + 1,
                                b.getBlockY() + reg.getHeight() - 1, b.getBlockZ() + 1));
                        rreg.setPos2(BlockVector3.at(b.getBlockX(), b.getBlockY(), b.getBlockZ() + reg.getLength()));
                        break;
                    case "N":
                        rreg.setPos1(BlockVector3.at(b.getBlockX() + reg.getWidth() - 1,
                                b.getBlockY() + reg.getHeight() - 1, b.getBlockZ() - 1));
                        rreg.setPos2(BlockVector3.at(b.getBlockX(), b.getBlockY(), b.getBlockZ() - reg.getLength()));
                        break;
                    case "E":
                        rreg.setPos1(BlockVector3.at(b.getBlockX() + 1, b.getBlockY() + reg.getHeight() - 1,
                                b.getBlockZ() + reg.getWidth() - 1));
                        rreg.setPos2(BlockVector3.at(b.getBlockX() + reg.getLength(), b.getBlockY(), b.getBlockZ()));
                        break;

                    case "W":
                        rreg.setPos1(BlockVector3.at(b.getBlockX() - 1, b.getBlockY() + reg.getHeight() - 1,
                                b.getBlockZ() - reg.getWidth() + 1));
                        rreg.setPos2(BlockVector3.at(b.getBlockX() - reg.getLength(), b.getBlockY(), b.getBlockZ()));
                        break;

                }

                new BukkitRunnable() {

                    @Override
                    public void run() {

                        for (int xx = rreg.getMinimumPoint().getBlockX(); xx <= rreg.getMaximumPoint()
                                .getBlockX(); xx++) {
                            for (int y = rreg.getMinimumPoint().getBlockY(); y <= rreg.getMaximumPoint()
                                    .getBlockY(); y++) {
                                for (int z = rreg.getMinimumPoint().getBlockZ(); z <= rreg.getMaximumPoint()
                                        .getBlockZ(); z++) {
                                    try {
                                        //Constructor<?> particleConstructor = getNMSClass("PacketPlayOutWorldParticles")
                                        //.getConstructors()[1];
                                        PacketPlayOutWorldParticles packetplayoutworldparticles = new PacketPlayOutWorldParticles(CraftParticle.toNMS(Particle.BARRIER, null), true, (float) xx + 0.5f, (float) y + 0.5f, (float) z + 0.5f, (float) 0, (float) 0, (float) 0, (float) 0, 0);
                                        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetplayoutworldparticles);

                                        //Object packet = particleConstructor.newInstance(CraftParticle.toNMS(Particle.BARRIER, null), true,
                                        //		xx + 0.5, y + 0.5, z + 0.5, 0, 0, 0, 0, 0);

                                        //sendPacket(p, packetplayoutworldparticles);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    // p.spawnParticle(Particle.BARRIER,
                                    // new Location(b.getWorld(), xx + 0.5, y + 0.5, z + 0.5), 0, 0, 0, 0, 0);
                                }
                            }

                        }
                    }

                }.runTaskAsynchronously(BSLMain.getInstance());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String dir(Player player) {
        player.getLocation().setPitch(0.0F);
        double rotation = player.getLocation().getYaw() - 180;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 45) {
            return "N";
        }
        if (45 <= rotation && rotation < 135) {
            return "E";
        }
        if (135 <= rotation && rotation < 225) {
            return "S";
        }
        if (225 <= rotation && rotation < 315) {
            return "W";
        }
        if (315 <= rotation && rotation <= 360) {
            return "N";
        }
        return "N";
    }

    private void sendPacket(Player p, Object packet) {
        try {
            Object handle = p.getClass().getMethod("getHandle").invoke(p);
            Object playerConnectionField = handle.getClass().getField("playerConnection");
            playerConnectionField.getClass().getMethod("sendPacket", getNMSClass("Packet"))
                    .invoke(playerConnectionField, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
