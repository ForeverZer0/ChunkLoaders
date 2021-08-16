package com.foreverzer0.chunkloaders;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakHandler extends HandlerBase {

    public BlockBreakHandler(ChunkLoadersPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {

        if (e.getBlock().getType() == plugin.getBlockMaterial() && e.isDropItems()) {
            if (plugin.unregisterLoader(e.getPlayer(), e.getBlock())) {
                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    var stack = new ItemStack(plugin.getItemMaterial(), 1);
                    e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), stack);
                    sendLocationMessage(e.getPlayer(), "Chunk loader broken", e.getBlock().getLocation());
                }
            }
        }

    }
}
