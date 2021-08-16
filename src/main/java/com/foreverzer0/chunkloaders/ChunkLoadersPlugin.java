package com.foreverzer0.chunkloaders;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public final class ChunkLoadersPlugin extends JavaPlugin {

    private static final Material DEFAULT_BLOCK_MATERIAL = Material.LODESTONE;
    private static final Material DEFAULT_ITEM_MATERIAL = Material.NETHER_STAR;

    private Material blockMaterial;
    private Material itemMaterial;
    private boolean explosionProtect;

    private ArrayList<ChunkLoader> database;
    private boolean enableParticles;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        database = new ArrayList<>();
        blockMaterial = DEFAULT_BLOCK_MATERIAL;
        itemMaterial = DEFAULT_ITEM_MATERIAL;
        explosionProtect = false;
        enableParticles = true;

        getServer().getPluginManager().registerEvents(new PlayerInteractHandler(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakHandler(this), this);
        getServer().getPluginManager().registerEvents(new EntityExplodeHandler(this), this);
        Objects.requireNonNull(getCommand("chunkloaders")).setExecutor(new ChunkLoadersCommand(this));

        loadConfig();
    }

    public void loadConfig() {

        FileConfiguration config = getConfig();
        blockMaterial = parseMaterial(config.getString("material.block"), DEFAULT_BLOCK_MATERIAL);
        itemMaterial = parseMaterial(config.getString("material.item"), DEFAULT_ITEM_MATERIAL);
        explosionProtect = config.getBoolean("explosion-protect");
        enableParticles = true;

        ConfigurationSerialization.registerClass(ChunkLoader.class);
        loadDatabase();

        for (ChunkLoader loader : database) {
            if (loader == null) {
                System.out.println("HOW THE FUCK IS NULL");
                continue;
            }

            applyParticles(loader);
            loader.getChunk().addPluginChunkTicket(this);
        }
    }

    @Override
    public void onDisable() {
        enableParticles = false;
        getServer().getScheduler().cancelTasks(this);
        if (database != null) {
            for (ChunkLoader loader : database) {
                loader.getChunk().removePluginChunkTicket(this);
            }
            database.clear();
        }
    }

    private void loadDatabase() {

        database.clear();

        File file = new File(getDataFolder(), "loaders.yml");
        if (!file.exists()) {
            getLogger().info("No database file found");
        }
        else {
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.load(file);
                //noinspection unchecked
                database = (ArrayList<ChunkLoader>) yaml.getList("loaders", new ArrayList<>());
                getLogger().info("Loaded " + database.size() + " loaders from database");
            } catch (IOException | InvalidConfigurationException e) {
                getLogger().severe("Failed to load database file: " + file.getName());
            }
        }
    }

    private void saveDatabase() {

        File file = new File(getDataFolder(), "loaders.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("loaders", database);
        try {
            config.save(file);
            getLogger().finer("Successfully saved chunk loader database");
        } catch (IOException e) {
            getLogger().severe("Failed to open " + file.getName());
            e.printStackTrace();
        }
    }

    public ArrayList<ChunkLoader> getPlayerLoaders(String playerName) {
        ArrayList<ChunkLoader> list = new ArrayList<>();
        if (playerName == null || playerName.isEmpty())
            return list;

        for (ChunkLoader loader : database) {
            if (loader.getPlayerName().equals(playerName))
                list.add(loader);
        }
        return list;
    }

    public ArrayList<ChunkLoader> getPlayerLoaders(UUID playerId) {

        ArrayList<ChunkLoader> list = new ArrayList<>();
        if (playerId == null)
            return list;

        for (ChunkLoader loader : database) {
            if (loader.getPlayerId().equals(playerId))
                list.add(loader);
        }
        return list;
    }

    public boolean isChunkLoader(Location location) {

        if (location == null)
            return false;

        for (ChunkLoader loader : database) {
            if (loader.getLocation().equals(location))
                return true;
        }

        return false;
    }

    public boolean registerChunkLoader(Player player, Block block) {

        Location location = block.getLocation();
        Chunk chunk = location.getChunk();
        if (!chunk.addPluginChunkTicket(this))
            return false;

        ChunkLoader loader = new ChunkLoader(player, block);
        database.add(loader);
        applyParticles(loader);

        getLogger().info(player.getName() + " created a chunk loader at " +
                block.getX() + "," +
                block.getY() + "," +
                block.getZ() + "," +
                " in  " + loader.getWorldName()
        );

        saveDatabase();
        return true;
    }

    public ChunkLoader getLoader(Location location) {
        for (ChunkLoader loader : database) {
            if (loader.getLocation().equals(location))
                return loader;
        }
        return null;
    }

    public ChunkLoader getLoader(Block block) {
        return getLoader(block.getLocation());
    }

    public boolean registerLoader(Block block) {
        return unregisterLoader("An explosion", block);
    }

    public boolean unregisterLoader(Player player, Block block) {
        return unregisterLoader(player.getName(), block);
    }

    private boolean unregisterLoader(String reason, Block block) {

        ChunkLoader loader = getLoader(block);
        if (loader == null)
            return false;

        LoaderParticleTask task = loader.getTask();
        if (task != null) {
            task.cancel();
            loader.setTask(null);
        }

        Location location = block.getLocation();
        database.remove(loader);
        location.getChunk().removePluginChunkTicket(this);

        World world = location.getWorld();
        getLogger().info(reason + " removed a chunk loader at " +
                block.getX() + "," +
                block.getY() + "," +
                block.getZ() + "," +
                " in " + (world == null ? "unknown world" : world.getName())
        );

        saveDatabase();
        return true;
    }

    public boolean canExplosionDestroy() {
        return !explosionProtect;
    }

    public void playSoundEffect(Block block) {

        String name = getConfig().getString("sound.name");
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("none"))
            return;

        Sound sound;
        try {
            sound = Sound.valueOf(name);
        } catch (IllegalArgumentException e) {
            getLogger().severe("Invalid sound name specified");
            return;
        }

        float volume = (float) getConfig().getDouble("sound.volume");
        float pitch = (float) getConfig().getDouble("sound.pitch");
        block.getWorld().playSound(block.getLocation(), sound, volume, pitch);
    }

    public void applyParticles(ChunkLoader loader) {

        if (!enableParticles)
            return;

        LoaderParticleTask task = new LoaderParticleTask(this, loader.getLocation());
        loader.setTask(task);
        task.runTaskTimerAsynchronously(this, 0, 40);
    }

    public boolean getParticlesEnabled() {
        return enableParticles;
    }

    public Player getOwner(Block block) {
        Location location = block.getLocation();
        for (ChunkLoader loader : database) {
            if (loader.getLocation().equals(location)) {
                return getServer().getPlayer(loader.getPlayerId());
            }
        }
        return null;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }

    private Material parseMaterial(String name, Material ifInvalid) {

        if (name == null || name.isEmpty()) {
            getLogger().warning("Undefined material specified, using default value");
            return ifInvalid;
        }

        Material material = Material.getMaterial(name);
        if (material == null) {
            getLogger().warning("Invalid material \"" + name + "\" specified, using default value");
            return ifInvalid;
        }
        return material;
    }
}