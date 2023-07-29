package com.songoda.skyblock.listeners;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.island.Island;
import com.songoda.skyblock.island.IslandLevel;
import com.songoda.skyblock.island.IslandManager;
import com.songoda.skyblock.world.WorldManager;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

public class PistonListeners implements Listener {
    private final SkyBlock plugin;

    public PistonListeners(SkyBlock plugin) {
        this.plugin = plugin;
    }

    // Prevent point farming dragon eggs.
    @EventHandler
    public void onPistonMove(BlockPistonExtendEvent event) {
        Block block = event.getBlock().getRelative(event.getDirection());

        IslandManager islandManager = this.plugin.getIslandManager();
        WorldManager worldManager = this.plugin.getWorldManager();
        if (!worldManager.isIslandWorld(block.getWorld())) {
            return;
        }

        Island island = islandManager.getIslandAtLocation(block.getLocation());
        if (island == null || CompatibleMaterial.getMaterial(block) != CompatibleMaterial.DRAGON_EGG) {
            return;
        }

        FileConfiguration configLoad = this.plugin.getConfiguration();
        if (!configLoad.getBoolean("Island.Block.Level.Enable")) {
            return;
        }

        CompatibleMaterial material = CompatibleMaterial.getMaterial(block);
        if (material == null) {
            return;
        }

        IslandLevel level = island.getLevel();
        if (!level.hasMaterial(material.name())) {
            return;
        }

        long materialAmount = level.getMaterialAmount(material.name());
        if (materialAmount <= 1) {
            level.removeMaterial(material.name());
        } else {
            level.setMaterialAmount(material.name(), materialAmount - 1);
        }
    }
}
