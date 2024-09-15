package io.github.altkat.cropsRegen.Farm;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.github.altkat.cropsRegen.PAPI.CountdownPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class FarmScheduler {

    private final JavaPlugin plugin;
    private final String regionName;
    private final Material cropMaterial;
    private final int interval;
    private final String worldName;
    private final CountdownPlaceholder countdownPlaceholder;

    public FarmScheduler(JavaPlugin plugin, String regionName, Material cropMaterial) {
        this.plugin = plugin;
        this.regionName = regionName;
        this.cropMaterial = cropMaterial;
        interval = plugin.getConfig().getInt("Farm.refreshInterval");
        worldName = plugin.getConfig().getString("Farm.worldName");
        countdownPlaceholder = new CountdownPlaceholder(interval * 1000L);
        startScheduler();
    }

    private void startScheduler() {
        countdownPlaceholder.resetTimer();
        Bukkit.getScheduler().runTaskTimer(plugin, this::plantCropsInRegion, 0L, interval * 20L);
    }

    public synchronized void plantCropsInRegion() {
        plantCrops(regionName, cropMaterial);
        countdownPlaceholder.resetTimer();
    }

    protected void plantCrops(String regionName, Material cropMaterial) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("[CropsRegen] World with name '" + worldName + "' not found!");
                return;
            }



            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            if (regions == null) {
                plugin.getLogger().warning("[CropsRegen] RegionManager cannot be found!");
                return;
            }

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) {
                plugin.getLogger().warning("[CropsRegen] " + regionName + " region cannot be found!");
                return;
            }

            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (min.getX() > max.getX() || min.getY() > max.getY() || min.getZ() > max.getZ()) {
                    plugin.getLogger().warning("[CropsRegen] Invalid region dimensions!");
                    return;
                }

                for (int x = min.getX(); x <= max.getX(); x++) {
                    for (int y = min.getY(); y <= max.getY(); y++) {
                        for (int z = min.getZ(); z <= max.getZ(); z++) {
                            Location loc = new Location(world, x, y, z);
                            Block block = loc.getBlock();

                            if (block.getType() == Material.FARMLAND) {
                                Block aboveBlock = block.getRelative(BlockFace.UP);
                                if (aboveBlock.getType() == Material.AIR) {
                                    aboveBlock.setType(cropMaterial);

                                    if (cropMaterial == Material.WHEAT || cropMaterial == Material.CARROTS || cropMaterial == Material.POTATOES || cropMaterial == Material.BEETROOTS) {
                                        if (aboveBlock.getBlockData() instanceof Ageable crop) {
                                            crop.setAge(crop.getMaximumAge());
                                            aboveBlock.setBlockData(crop);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        });
    }


}

