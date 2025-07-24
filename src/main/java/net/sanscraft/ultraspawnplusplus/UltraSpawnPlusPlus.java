package net.sanscraft.ultraspawnplusplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UltraSpawnPlusPlus extends JavaPlugin implements Listener {
    // --- Spawn on join/respawn ---
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("ultraspawn.admin") || player.hasPermission("ultraspawn.nojoinspawn")) return;
        Location spawn = getSpawnLocation();
        if (spawn != null) {
            player.teleport(spawn);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Always respawn at spawn regardless of admin/nojoinspawn permissions
        Location spawn = getSpawnLocation();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }
    private boolean hubEnabled;
    private String hubServer;
    private FileConfiguration config;
    private final Map<UUID, BukkitRunnable> teleportTasks = new HashMap<>();
    private final Map<UUID, Location> startLocations = new HashMap<>();
    private boolean useBossbar;
    private boolean useActionbar;
    private String bossbarTitle;
    private String bossbarColor;
    private String bossbarStyle;
    private String actionbarMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        useBossbar = config.getBoolean("display.bossbar", false);
        useActionbar = config.getBoolean("display.actionbar", false);
        bossbarTitle = config.getString("display.bossbar-title", "✨ Teleporting in %seconds% second(s)... ✨");
        bossbarColor = config.getString("display.bossbar-color", "BLUE");
        bossbarStyle = config.getString("display.bossbar-style", "SOLID");
        actionbarMessage = config.getString("display.actionbar-message", "✨ Teleporting in %seconds% second(s)... ✨");
        hubEnabled = config.getBoolean("hub.enabled", false);
        hubServer = config.getString("hub.server", "lobby");
        // Register tab completer for all commands
        getCommand("spawn").setTabCompleter(new UltraSpawnTabCompleter());
        getCommand("ultraspawnreload").setTabCompleter(new UltraSpawnTabCompleter());
        getCommand("ultraspawnconfig").setTabCompleter(new UltraSpawnTabCompleter());
        getCommand("hub").setTabCompleter(new UltraSpawnTabCompleter());
        getServer().getPluginManager().registerEvents(this, this);
        // Register BungeeCord plugin messaging channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        // /setspawn command
        if (cmd.equals("setspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("ultraspawn.admin")) {
                player.sendMessage("You do not have permission to use /setspawn.");
                return true;
            }
            Location loc = player.getLocation();
            config.set("spawn.world", loc.getWorld().getName());
            config.set("spawn.x", loc.getX());
            config.set("spawn.y", loc.getY());
            config.set("spawn.z", loc.getZ());
            config.set("spawn.yaw", loc.getYaw());
            config.set("spawn.pitch", loc.getPitch());
            saveConfig();
            player.sendMessage("Spawn location set to your current position.");
            return true;
        }
        // /spawn command
        if (cmd.equals("spawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("ultraspawn.spawn")) {
                player.sendMessage(config.getString("messages.permission-message", "You do not have permission."));
                return true;
            }
            if (player.hasPermission("ultraspawn.bypasscooldown")) {
                // Instantly teleport
                player.sendMessage(config.getString("messages.finish", "Teleporting now!"));
                Location spawn = getSpawnLocation();
                if (spawn != null) player.teleport(spawn);
                return true;
            }
            if (teleportTasks.containsKey(player.getUniqueId())) {
                player.sendMessage("Teleport already in progress!");
                return true;
            }
            // Find custom cooldowns
            int minTicks = Integer.MAX_VALUE;
            for (int i = 1; i <= 1200; i++) { // up to 60 seconds
                if (player.hasPermission("ultraspawn.customcooldown.ticks." + i)) {
                    minTicks = Math.min(minTicks, i);
                }
            }
            for (int i = 1; i <= 60; i++) {
                if (player.hasPermission("ultraspawn.customcooldown.seconds." + i)) {
                    minTicks = Math.min(minTicks, i * 20);
                }
            }
            int delayTicks;
            if (minTicks != Integer.MAX_VALUE) {
                delayTicks = minTicks;
            } else {
                delayTicks = config.getInt("delay", 5) * 20;
            }
            int delaySeconds = delayTicks / 20;
            String startMsg = config.getString("messages.start", "Teleporting in %delay% seconds...").replace("%delay%", String.valueOf(delaySeconds));
            player.sendMessage(startMsg);
            Location startLoc = player.getLocation().getBlock().getLocation();
            startLocations.put(player.getUniqueId(), startLoc);
            final String countdownSound = config.getString("sound.countdown", "block.note_block.pling");
            final float countdownVolume = (float) config.getDouble("sound.countdown-volume", 1.0);
            final float minPitch = (float) config.getDouble("sound.countdown-pitch-min", 0.5);
            final float maxPitch = (float) config.getDouble("sound.countdown-pitch-max", 2.0);
            final String finishSound = config.getString("sound.finish", "block.beacon.activate");
            final float finishVolume = (float) config.getDouble("sound.finish-volume", 1.0);
            final float finishPitch = (float) config.getDouble("sound.finish-pitch", 1.0);
            BukkitRunnable task = new BukkitRunnable() {
                int seconds = delaySeconds;
                @Override
                public void run() {
                    if (seconds <= 0) {
                        teleportTasks.remove(player.getUniqueId());
                        startLocations.remove(player.getUniqueId());
                        player.sendMessage(config.getString("messages.finish", "Teleporting now!"));
                        Location spawn = getSpawnLocation();
                        if (spawn != null) {
                            player.teleport(spawn);
                            // Play finish sound at the spawn location after teleport
                            player.playSound(spawn, finishSound, finishVolume, finishPitch);
                        }
                        cancel();
                        return;
                    }
                    String msg = config.getString("messages.countdown", "Teleporting in %seconds% second(s)...").replace("%seconds%", String.valueOf(seconds));
                    player.sendMessage(msg);
                    if (useActionbar) {
                        String abMsg = actionbarMessage.replace("%seconds%", String.valueOf(seconds));
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(abMsg));
                    }
                    // Bossbar support (Spigot API 1.9+)
                    if (useBossbar) {
                        String barTitle = bossbarTitle.replace("%seconds%", String.valueOf(seconds));
                        org.bukkit.boss.BarColor color;
                        org.bukkit.boss.BarStyle style;
                        try {
                            color = org.bukkit.boss.BarColor.valueOf(bossbarColor.toUpperCase());
                        } catch (Exception e) {
                            color = org.bukkit.boss.BarColor.BLUE;
                        }
                        try {
                            style = org.bukkit.boss.BarStyle.valueOf(bossbarStyle.toUpperCase());
                        } catch (Exception e) {
                            style = org.bukkit.boss.BarStyle.SOLID;
                        }
                        org.bukkit.boss.BossBar bar = player.getMetadata("ultraspawn_bossbar").stream().findFirst().map(m -> (org.bukkit.boss.BossBar) m.value()).orElse(null);
                        if (bar == null) {
                            bar = Bukkit.createBossBar(barTitle, color, style);
                            bar.setProgress(1.0);
                            bar.addPlayer(player);
                            player.setMetadata("ultraspawn_bossbar", new org.bukkit.metadata.FixedMetadataValue(UltraSpawnPlusPlus.this, bar));
                        } else {
                            bar.setTitle(barTitle);
                            bar.setColor(color);
                            bar.setStyle(style);
                            bar.setProgress(Math.max(0.0, (double) seconds / delaySeconds));
                        }
                    }
                    // Play configurable countdown sound with changing pitch
                    float pitch = minPitch + (maxPitch - minPitch) * (delaySeconds - seconds) / Math.max(1, delaySeconds);
                    player.playSound(player.getLocation(), countdownSound, countdownVolume, pitch);
                    seconds--;
                }
            };
            teleportTasks.put(player.getUniqueId(), task);
            task.runTaskTimer(this, 0L, 20L);
            return true;
        }
        // /ultraspawnreload command
        if (cmd.equals("ultraspawnreload")) {
            if (!sender.hasPermission("ultraspawn.admin")) {
                sender.sendMessage("You do not have permission to reload the config.");
                return true;
            }
            reloadConfig();
            config = getConfig();
            useBossbar = config.getBoolean("display.bossbar", false);
            useActionbar = config.getBoolean("display.actionbar", false);
            bossbarTitle = config.getString("display.bossbar-title", "✨ Teleporting in %seconds% second(s)... ✨");
            bossbarColor = config.getString("display.bossbar-color", "BLUE");
            bossbarStyle = config.getString("display.bossbar-style", "SOLID");
            actionbarMessage = config.getString("display.actionbar-message", "✨ Teleporting in %seconds% second(s)... ✨");
            hubEnabled = config.getBoolean("hub.enabled", false);
            hubServer = config.getString("hub.server", "lobby");
            sender.sendMessage("UltraSpawnPlusPlus config reloaded.");
            return true;
        }
        // /ultraspawnconfig <key> <value>
        if (cmd.equals("ultraspawnconfig")) {
            if (!sender.hasPermission("ultraspawn.admin")) {
                sender.sendMessage("You do not have permission to edit the config.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /ultraspawnconfig <key> <value>");
                return true;
            }
            String key = args[0];
            String value = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            // Only allow editing known keys for safety
            if (key.equals("delay")) {
                try {
                    int delay = Integer.parseInt(value);
                    config.set("delay", delay);
                    saveConfig();
                    sender.sendMessage("Set delay to " + delay);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number for delay.");
                }
                return true;
            }
            if (key.startsWith("display.")) {
                config.set(key, value);
                saveConfig();
                sender.sendMessage("Set " + key + " to " + value);
                return true;
            }
            if (key.equals("hub.enabled")) {
                boolean enabled = value.equalsIgnoreCase("true") || value.equals("1") || value.equalsIgnoreCase("yes");
                config.set("hub.enabled", enabled);
                saveConfig();
                sender.sendMessage("Set hub.enabled to " + enabled);
                return true;
            }
            if (key.equals("hub.server")) {
                config.set("hub.server", value);
                saveConfig();
                sender.sendMessage("Set hub.server to " + value);
                return true;
            }
            sender.sendMessage("Unknown config key: " + key);
            return true;
        }
        // /hub command
        if (cmd.equals("hub")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }
            if (!hubEnabled) {
                sender.sendMessage("/hub is not enabled on this server.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("ultraspawn.hub")) {
                player.sendMessage("You do not have permission to use /hub.");
                return true;
            }
            // Send player to proxy server (Velocity/BungeeCord compatible)
            player.sendMessage("Sending you to " + hubServer + "...");
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(b);
            try {
                out.writeUTF("Connect");
                out.writeUTF(hubServer);
            } catch (Exception ex) {
                player.sendMessage("Failed to send you to the hub server.");
                return true;
            }
            player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!teleportTasks.containsKey(player.getUniqueId())) return;
        Location from = event.getFrom().getBlock().getLocation();
        Location startLoc = startLocations.get(player.getUniqueId());
        if (!from.equals(startLoc)) {
            cancelTeleport(player, config.getString("messages.cancel_move", "Teleport cancelled: you moved!"));
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!teleportTasks.containsKey(player.getUniqueId())) return;
        cancelTeleport(player, config.getString("messages.cancel_damage", "Teleport cancelled: you took damage!"));
    }

    private void cancelTeleport(Player player, String message) {
        BukkitRunnable task = teleportTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
        startLocations.remove(player.getUniqueId());
        // Remove bossbar if present
        if (useBossbar) {
            player.getMetadata("ultraspawn_bossbar").stream().findFirst().ifPresent(m -> {
                org.bukkit.boss.BossBar bar = (org.bukkit.boss.BossBar) m.value();
                bar.removeAll();
            });
            player.removeMetadata("ultraspawn_bossbar", this);
        }
        player.sendMessage(message);
    }

    private Location getSpawnLocation() {
        String worldName = config.getString("spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = config.getDouble("spawn.x", 0.5);
        double y = config.getDouble("spawn.y", 64);
        double z = config.getDouble("spawn.z", 0.5);
        float yaw = (float) config.getDouble("spawn.yaw", 0);
        float pitch = (float) config.getDouble("spawn.pitch", 0);
        return new Location(world, x, y, z, yaw, pitch);
    }
}
