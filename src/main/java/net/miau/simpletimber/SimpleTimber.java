package net.miau.simpletimber;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Random;

public final class SimpleTimber extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        Bukkit.getPluginManager().registerEvents(this, this);
        new Metrics(this, 17386);

    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || e.getBlock().getLocation().getWorld() == null) return;
        ItemStack handStack = player.getInventory().getItemInMainHand();
        if (handStack.getType().toString().contains("_AXE")) {
            Block block = e.getBlock();
            if (block.getType().toString().contains("STRIPPED")) return;
            if ((block.getType().toString().contains("LOG") || block.getType().toString().contains("_STEM")) && block.getLocation().getWorld() != null && !player.isSneaking()) {
                cutDownTree(block.getLocation(), handStack, block.getType());
            }
        }
    }

    public void cutDownTree(Location location, ItemStack hand, Material type) {
        ArrayList<Block> blocks = new ArrayList<>();
        while (true) {
            Location loc = location.add(0.0D, 1.0D, 0.0D);
            Block block = loc.getBlock();
            if (block.getType() == type) {
                blocks.add(block);
            } else {
                break;
            }
        }
        for (Block block : blocks) {
            if (block.breakNaturally(hand)) {
                if (hand.getEnchantments().containsKey(Enchantment.DURABILITY)) {
                    int chance = 1 + hand.getEnchantments().get(Enchantment.DURABILITY);
                    if (new Random().nextInt(0, chance) == 0) {
                        hand.setDurability((short) (hand.getDurability() + 1));
                        if (hand.getType().getMaxDurability() <= hand.getDurability()) {
                            break;
                        }
                    }
                } else {
                    hand.setDurability((short) (hand.getDurability() + 1));
                }
                if (hand.getType().getMaxDurability() == hand.getDurability()) {
                    hand.setType(Material.AIR);
                    break;
                }
            }
        }
    }
}
