package net.miau.simpletimber;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;

public final class SimpleTimber extends JavaPlugin implements Listener {

    private boolean needsPermission = false;
    private boolean cancelIfSneaking = true;

    @Override
    public void onEnable() {
        getConfig().options().header("Permission: simpletimber.use");
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (!getConfig().contains("needsPermission")) {
            getConfig().set("needsPermission", this.needsPermission);
            saveConfig();
        }
        this.needsPermission = getConfig().getBoolean("needsPermission");
        if (!getConfig().contains("cancelIfSneaking")) {
            getConfig().set("cancelIfSneaking", this.cancelIfSneaking);
            saveConfig();
        }
        this.cancelIfSneaking = getConfig().getBoolean("cancelIfSneaking");

        Bukkit.getPluginManager().registerEvents(this, this);
        new Metrics(this, 17386);

        try {
            URL url = new URL("https://api.github.com/repos/innocentmiau/SimpleTimber/releases/latest");
            String s = stream(url);
            String version = s.substring(s.indexOf("\"tag_name\":\"") + 13, s.indexOf("\"target_commitish\"") - 2);
            if (!version.equals(this.getDescription().getVersion())) {
                getLogger().info("---[SimpleTimber]---");
                getLogger().info("[>] There is a new update available.");
                getLogger().info("[>] current version: " + this.getDescription().getVersion());
                getLogger().info("[>] latest version: " + version);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void onDisable() {
    }

    public String stream(URL url) throws IOException {
        try (InputStream input = url.openStream()) {
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
            return json.toString();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()
                || e.getPlayer().getGameMode() == GameMode.CREATIVE
                || e.getBlock().getLocation().getWorld() == null) return;
        Player player = e.getPlayer();
        if (this.needsPermission && !player.hasPermission("simpletimber.use")) return;
        ItemStack handStack = player.getInventory().getItemInMainHand();
        if (!handStack.getType().toString().contains("_AXE")) return;
        Block block = e.getBlock();
        if (block.getType().toString().contains("STRIPPED")) return;
        if ((block.getType().toString().contains("LOG") || block.getType().toString().contains("_STEM")) && block.getLocation().getWorld() != null && !player.isSneaking()) {
            cutDownTree(block.getLocation(), handStack, block.getType());
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
            }
        }
        if (hand.getType().getMaxDurability() == hand.getDurability()) {
            hand.setType(Material.getMaterial("AIR"));
        }
    }
}
