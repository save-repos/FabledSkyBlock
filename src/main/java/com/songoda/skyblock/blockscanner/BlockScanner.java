package com.songoda.skyblock.blockscanner;

import com.google.common.collect.Lists;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.utils.version.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class BlockScanner extends BukkitRunnable {

    private static final Method ID_FIELD;

    static {
        Method temp = null;

        try {
            temp = ChunkSnapshot.class.getMethod("getBlockTypeId", int.class, int.class, int.class);
        } catch (NoSuchMethodException ignored) {

        }

        ID_FIELD = temp;
    }

    public static int getBlockTypeID(ChunkSnapshot snapshot, int x, int y, int z) {

        int id = 0;

        try {
            id = (Integer) ID_FIELD.invoke(snapshot, x, y, z);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return id;
    }

    private final static int VERSION = NMSUtil.getVersionNumber();

    private int completedNum;

    private final int threadCount;
    private final Queue<BlockInfo> blocks;
    private final ScannerTasks tasks;
    
    private final boolean ignoreLiquids;
    private final boolean ignoreAir;

    private BlockScanner(Map<World, List<ChunkSnapshot>> snapshots, boolean ignoreLiquids, boolean ignoreLiquidsY, boolean ignoreAir, boolean ignoreY, ScannerTasks tasks) {
        this.ignoreLiquids = ignoreLiquids;
        this.ignoreAir = ignoreAir;
        this.blocks = new ConcurrentLinkedQueue<>();
        this.tasks = tasks;

        FileConfiguration config = SkyBlock.getInstance().getFileManager().getConfig(new File(SkyBlock.getInstance().getDataFolder(), "config.yml")).getFileConfiguration();

        int threadCount = 0;

        for (Entry<World, List<ChunkSnapshot>> entry : snapshots.entrySet()) {

            final List<List<ChunkSnapshot>> parts = Lists.partition(entry.getValue(), 16);

            threadCount += parts.size();

            World world = entry.getKey();
            final String env;

            switch (world.getEnvironment()) {
            case NETHER:
                env = "Nether";
                break;
            case THE_END:
                env = "End";
                break;
            default:
                env = "Normal";
                break;
            }

            final ConfigurationSection liquidSection = config.getConfigurationSection("Island.World." + env + ".Liquid");

            int startY;
            if(ignoreY){
                startY = 255;
            } else {
                startY = !ignoreLiquidsY && liquidSection.getBoolean("Enable") && !config.getBoolean("Island.Levelling.ScanLiquid") ? liquidSection.getInt("Height") + 1 : 0;
            }

            for (List<ChunkSnapshot> sub : parts) {
               queueWork(world, startY, sub);
            }
        }

        this.threadCount = threadCount;
    }

    private void queueWork(World world, int scanY, List<ChunkSnapshot> subList) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(SkyBlock.getInstance(), () -> {
            for (ChunkSnapshot shot : subList) {

                final int cX = shot.getX() << 4;
                final int cZ = shot.getZ() << 4;

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = scanY; y < 256; y++) {


                            final CompatibleMaterial type = CompatibleMaterial.getBlockMaterial(VERSION > 12
                                    ? shot.getBlockType(x, y, z) : MaterialIDHelper.getLegacyMaterial(getBlockTypeID(shot, x, y, z)));


                            if(type == null){
                                continue;
                            } else if(type.equals(CompatibleMaterial.AIR) && ignoreAir){
                                continue;
                            } else if(type.equals(CompatibleMaterial.WATER) && ignoreLiquids){
                                continue;
                            }

                            blocks.add(new BlockInfo(world, x + (cX), y, z + (cZ)));
                        }
                    }
                }
            }
            increment();
        });
    }

    private synchronized int increment() {
        completedNum += 1;
        return completedNum;
    }

    private synchronized int get() {
        return completedNum;
    }

    @Override
    public void run() {
        if (get() != threadCount) return;

        tasks.onComplete(blocks);
        cancel();
    }

    public static void startScanner(Map<World, List<ChunkSnapshot>> snapshots, boolean ignoreLiquids, boolean ignoreLiquidsY, boolean ignoreAir, boolean ignoreY, ScannerTasks tasks) {

        if (snapshots == null) throw new IllegalArgumentException("snapshots cannot be null");
        if (tasks == null) throw new IllegalArgumentException("tasks cannot be null");

        final BlockScanner scanner = new BlockScanner(snapshots, ignoreLiquids, ignoreLiquidsY, ignoreAir, ignoreY, tasks);

        scanner.runTaskTimer(SkyBlock.getInstance(), 5, 5);
    }

    public interface ScannerTasks {

        void onComplete(Queue<BlockInfo> blocks);

    }

}
