package com.foreverzer0.chunkloaders;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for that blocks are destroyed from explosions, and ensures both the nether star is
 * dropped and the chunk loader is unregistered.
 */
public class EntityExplodeHandler extends HandlerBase {

    public EntityExplodeHandler(ChunkLoadersPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {

        if (!plugin.canExplosionDestroy()) {
            e.blockList().removeIf(b -> plugin.isChunkLoader(b.getLocation()));
            return;
        }

        for (Block block : e.blockList()) {

            if (block.getType() == plugin.getBlockMaterial() && plugin.isChunkLoader(block.getLocation())) {

                Player owner = plugin.getOwner(block);
                if (plugin.registerLoader(block)) {
                    var stack = new ItemStack(plugin.getItemMaterial(), 1);
                    block.getWorld().dropItem(block.getLocation(), stack);
                    if (plugin.getConfig().getBoolean("alert-owner-on-explosion"))
                        sendLocationMessage(owner, "An explosion has destroyed your chunk loader!", block.getLocation());
                }
            }
        }
    }
}
