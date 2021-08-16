package com.foreverzer0.chunkloaders;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractHandler extends HandlerBase {

    public PlayerInteractHandler(ChunkLoadersPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {

        // Is the player holding a nether star?
        var stack = e.getItem();
        if (stack == null || stack.getType() != plugin.getItemMaterial())
            return;

        // Is the player right-clicking on a lodestone?
        var block = e.getClickedBlock();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || block == null || block.getType() != plugin.getBlockMaterial())
            return;

        // Is this chunk already force loaded?
        if (block.getChunk().getPluginChunkTickets().contains(plugin)) {
            e.getPlayer().sendMessage(ChatColor.GRAY + "This chunk is already force loaded!");
            return;
        }

        // Create the chunk loader and update player inventory
        if (plugin.registerChunkLoader(e.getPlayer(), block)) {
            e.setUseItemInHand(Event.Result.ALLOW);

            plugin.playSoundEffect(block);
            sendLocationMessage(e.getPlayer(), "Chunk loader created!", block.getLocation());

            // Drop the nether star at block location, unless player is in creative mode
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
                stack.setAmount(stack.getAmount() - 1);
        }
    }
}
