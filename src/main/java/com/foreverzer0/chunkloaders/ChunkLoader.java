package com.foreverzer0.chunkloaders;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ChunkLoader implements ConfigurationSerializable {

    private final String playerName;
    private final UUID playerId;
    private final Location location;
    private LoaderParticleTask task = null;

    @SuppressWarnings("unused")
    ChunkLoader(Map<String, Object> map) {

        String worldName = (String) map.get("world");
        int x = (int) map.get("x");
        int y = (int) map.get("y");
        int z = (int) map.get("z");

        World world = Bukkit.getServer().getWorld(worldName);
        this.location = new Location(world, x, y, z);
        this.playerName = map.get("name").toString();
        this.playerId = UUID.fromString(map.get("uuid").toString());
    }

    public static ChunkLoader deserialize(Map<String, Object> map) {
        return new ChunkLoader(map);
    }

    public ChunkLoader(Player player, Location location) {
        this.location = location;
        this.playerName = player.getName();
        this.playerId = player.getUniqueId();
    }

    public ChunkLoader(Player player, Block block) {
        this(player, block.getLocation());
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("world", Objects.requireNonNull(location.getWorld()).getName());
        map.put("x", location.getBlockX());
        map.put("y", location.getBlockY());
        map.put("z", location.getBlockZ());
        map.put("name", playerName);
        map.put("uuid", playerId.toString());
        return map;
    }

    public Location getLocation() {
        return location;
    }

    public World getWorld() {
        return location.getWorld();
    }

    public Chunk getChunk() {
        return location.getChunk();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getWorldName() {
        World world = location.getWorld();
        return world == null ? "unknown" : world.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkLoader other) {
            return location.equals(other.location) && playerId.equals(other.playerId);
        }
        return false;
    }

    public LoaderParticleTask getTask() {
        return task;
    }

    void setTask(LoaderParticleTask task) {
        if (this.task != null)
            this.task.cancel();
        this.task = task;
    }
}
