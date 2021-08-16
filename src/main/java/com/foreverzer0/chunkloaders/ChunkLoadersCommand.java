package com.foreverzer0.chunkloaders;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChunkLoadersCommand implements TabExecutor {

    private static final String[] COMMANDS = { "list", "reload" };

    private final ChunkLoadersPlugin plugin;

    public ChunkLoadersCommand(ChunkLoadersPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean listCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {

            // Player using command without arguments
            if (args.length == 0 && player.hasPermission("chunkloaders.list")) {
                listLoaders(player, player);
                return true;
            }
            // Player using command while supplying argument
            else if (args.length == 1) {

                if (!player.hasPermission("chunkloaders.list.others")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to list other users!");
                } else {
                    listLoaders(args[0], player);
                }
                return true;
            }
            return false;
        }
        // A non-player (i.e. console) issuing command
        else {
            if (args.length < 1) {
                sender.sendMessage("Must specify which player to query.");
                return false;
            }
            listLoaders(args[0], sender);
            return true;
        }
    }

    private boolean reloadCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.onDisable();
        plugin.onEnable();
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0) {
            return switch (args[0]) {
                case "list" -> listCommand(sender, command, label, args);
                case "reload" -> reloadCommand(sender, command, label, args);
                default -> false;
            };
        }

        return listCommand(sender, command, label, args);
    }

    private void listLoaders(String playerName, CommandSender receiver) {

        if (playerName == null || playerName.isEmpty()) {
            receiver.sendMessage(ChatColor.RED + "No player by that name found!");
            return;
        }

        ArrayList<ChunkLoader> list = plugin.getPlayerLoaders(playerName);
        listMessage(list, playerName, receiver);
    }

    private void listLoaders(Player player, CommandSender receiver) {

        if (player == null) {
            receiver.sendMessage(ChatColor.RED + "No player by that name found!");
            return;
        }

        ArrayList<ChunkLoader> list = plugin.getPlayerLoaders(player.getUniqueId());
        listMessage(list, player.getName(), receiver);
    }

    private void listMessage(ArrayList<ChunkLoader> list, String playerName, CommandSender receiver) {

        int n = list.size();
        if (n == 0) {
            receiver.sendMessage(ChatColor.GRAY + "No chunk loaders found for user " + ChatColor.AQUA + playerName);
            return;
        }

        String word = ChatColor.GRAY + (n > 1 ? " chunk loaders" : " chunk loader");
        receiver.sendMessage(ChatColor.AQUA + playerName + ChatColor.GRAY + " owns " + ChatColor.WHITE + n + word);

        for (ChunkLoader loader : list) {
            receiver.sendMessage(ChatColor.GOLD + "  World:" + ChatColor.GRAY + loader.getWorldName() +
                    " " + ChatColor.GOLD + "x:" + ChatColor.GRAY + loader.getLocation().getBlockX() +
                    " " + ChatColor.GOLD + "y:" + ChatColor.GRAY + loader.getLocation().getBlockY() +
                    " " + ChatColor.GOLD + "z:" + ChatColor.GRAY + loader.getLocation().getBlockZ());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 0) {
            return Arrays.stream(COMMANDS).toList();
        }

        return switch (args[0]) {
            case "reload" -> new ArrayList<>();
            default -> null;
        };
    }
}
