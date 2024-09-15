package io.github.altkat.cropsRegen;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Listeners implements Listener {
    private final CropsRegen plugin;
    private List<String> farmCommand;
    private List<String> dropMessage;
    private final boolean status;
    private final String worldName;
    private final Set<String> regionNames;
    private final Map<Material, Double> chanceRates;
    private final Map<Material, List<String>> commands;
    private final Map<Material, List<String>> messages;
    private final Random random = new Random();
    private final RegionContainer regionContainer;
    private RegionManager regions;

    public Listeners(CropsRegen plugin) {
        this.plugin = plugin;
        this.regionNames = new HashSet<>(plugin.getConfig().getStringList("Farm.farmRegions"));
        status = plugin.getConfig().getBoolean("Drops.enabled");
        worldName = plugin.getConfig().getString("Farm.worldName");

        chanceRates = Map.of(
                Material.WHEAT, plugin.getConfig().getDouble("Drops.wheatChance"),
                Material.CARROTS, plugin.getConfig().getDouble("Drops.carrotChance"),
                Material.POTATOES, plugin.getConfig().getDouble("Drops.potatoChance"),
                Material.BEETROOTS, plugin.getConfig().getDouble("Drops.beetrootChance")
        );
        commands = Map.of(
                Material.WHEAT, plugin.getConfig().getStringList("Drops.wheatDropCommands"),
                Material.CARROTS, plugin.getConfig().getStringList("Drops.carrotDropCommands"),
                Material.POTATOES, plugin.getConfig().getStringList("Drops.potatoDropCommands"),
                Material.BEETROOTS, plugin.getConfig().getStringList("Drops.beetrootDropCommands")
        );
        messages = Map.of(
                Material.WHEAT, plugin.getConfig().getStringList("Drops.wheatDropMessages"),
                Material.CARROTS, plugin.getConfig().getStringList("Drops.carrotDropMessages"),
                Material.POTATOES, plugin.getConfig().getStringList("Drops.potatoDropMessages"),
                Material.BEETROOTS, plugin.getConfig().getStringList("Drops.beetrootDropMessages")
        );

        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        this.regions = regionContainer.get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!player.getWorld().getName().equals(worldName)) return;

        if (regions == null) {
            plugin.getLogger().warning("[CropsRegen] RegionManager cannot be found!");
            return;
        }

        CompletableFuture.runAsync(() -> {
            BlockVector3 blockVector = BukkitAdapter.asBlockVector(block.getLocation());
            ApplicableRegionSet regionSet = regions.getApplicableRegions(blockVector);

            if (!regionSet.getRegions().isEmpty()) {
                for (ProtectedRegion region : regionSet) {
                    if (regionNames.contains(region.getId())) {
                        if (isCrop(block.getType()) && status) {
                            double chance = random.nextDouble() * 100;
                            double chanceRate = chanceRates.getOrDefault(block.getType(), 100.0);
                            farmCommand = commands.get(block.getType());
                            dropMessage = messages.get(block.getType());

                            if (chance < chanceRate) {
                                Bukkit.getScheduler().runTask(plugin, () -> {

                                    for (String s : farmCommand) {
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName()));
                                    }
                                    for (String s : dropMessage) {
                                        player.sendMessage(s.replace("%chance_rate%", String.valueOf(chanceRate)).replace("&", "§"));
                                    }
                                });
                            }
                        } else if (!player.hasPermission("*")) {
                            Bukkit.getScheduler().runTask(plugin, () -> event.setCancelled(true));
                        }
                    }
                }
            }
        });
    }

    private boolean isCrop(Material material) {
        return material == Material.WHEAT || material == Material.CARROTS || material == Material.POTATOES || material == Material.BEETROOTS;
    }
}


