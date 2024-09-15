package io.github.altkat.cropsRegen.Farm;

import io.github.altkat.cropsRegen.CropsRegen;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.List;

public class Scheduler extends FarmScheduler {
    private final CropsRegen plugin;

    public Scheduler(CropsRegen plugin) {
        super(plugin, "solfarm", Material.WHEAT);
        this.plugin = plugin;
    }

    public synchronized void plantCropsInRegion() {
        List<String> regions = plugin.getConfig().getStringList("Farm.farmRegions");
        List<String> cropTypes = plugin.getConfig().getStringList("Farm.cropTypes");

        if (regions.size() != cropTypes.size()) {
            Bukkit.getLogger().warning("The number of regions (" + regions.size() + ") does not match the number of crop types (" + cropTypes.size() + ")!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                for (int i = 0; i < regions.size(); i++) {
                    Material cropMaterial;
                    try {
                        cropMaterial = Material.valueOf(cropTypes.get(i));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid crop type: " + cropTypes.get(i));
                        continue;
                    }
                    plantCrops(regions.get(i), cropMaterial);
                }
            }
        });
    }
}

