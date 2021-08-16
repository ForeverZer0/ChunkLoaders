package com.foreverzer0.chunkloaders;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class LoaderParticleTask extends BukkitRunnable {

    private final ChunkLoadersPlugin plugin;
    private final Location location;
    private final World world;

    public LoaderParticleTask(ChunkLoadersPlugin plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
        this.world = location.getWorld();
    }

    @Override
    public void run() {

        if (!plugin.getParticlesEnabled() || !plugin.isChunkLoader(location)) {
            this.cancel();
            return;
        }
        world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0, 24);
    }
}
