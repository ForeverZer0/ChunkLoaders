package com.foreverzer0.chunkloaders;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class HandlerBase implements Listener {

    protected final ChunkLoadersPlugin plugin;

    protected HandlerBase(ChunkLoadersPlugin plugin) {
        this.plugin = plugin;
    }

    private static HoverEvent getLocationComponent(Location loc) {

        String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "unknown";
        //noinspection deprecation
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder()
                .append("World: ").color(net.md_5.bungee.api.ChatColor.GREEN).bold(true)
                .append(worldName).color(net.md_5.bungee.api.ChatColor.GRAY).bold(false)
                .append("\n")
                .append("X: ").color(net.md_5.bungee.api.ChatColor.GREEN).bold(true)
                .append(String.valueOf(loc.getBlockX())).color(net.md_5.bungee.api.ChatColor.GRAY).bold(false)
                .append("\n")
                .append("Y: ").color(net.md_5.bungee.api.ChatColor.GREEN).bold(true)
                .append(String.valueOf(loc.getBlockY())).color(net.md_5.bungee.api.ChatColor.GRAY).bold(false)
                .append("\n")
                .append("Z: ").color(net.md_5.bungee.api.ChatColor.GREEN).bold(true)
                .append(String.valueOf(loc.getBlockZ())).color(net.md_5.bungee.api.ChatColor.GRAY).bold(false)
                .append("\n")
                .append("Biome: ").color(net.md_5.bungee.api.ChatColor.YELLOW).bold(true)
                .append(String.valueOf(loc.getBlock().getBiome())).color(net.md_5.bungee.api.ChatColor.GOLD).bold(false)
                .create());
    }

    protected static void sendLocationMessage(Player player, String message, Location location) {

        if (player == null || message == null || location == null)
            return;
        var hover = new ComponentBuilder(message)
                .color(net.md_5.bungee.api.ChatColor.YELLOW).bold(false)
                .event(getLocationComponent(location));
        player.spigot().sendMessage(hover.create());
    }
}

