package net.sanscraft.ultraspawnplusplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UltraSpawnTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        if (cmd.equals("spawn")) {
            return Collections.emptyList();
        }
        if (cmd.equals("ultraspawnreload")) {
            return Collections.emptyList();
        }
        if (cmd.equals("ultraspawnconfig")) {
            if (args.length == 1) {
                // Suggest all editable config keys
                return Arrays.asList(
                    "delay",
                    "display.bossbar",
                    "display.actionbar",
                    "display.bossbar-title",
                    "display.bossbar-color",
                    "display.bossbar-style",
                    "display.actionbar-message",
                    "hub.enabled",
                    "hub.server",
                    "spawn.world",
                    "spawn.x",
                    "spawn.y",
                    "spawn.z",
                    "spawn.yaw",
                    "spawn.pitch"
                );
            }
            return Collections.emptyList();
        }
        if (cmd.equals("hub")) {
            return Collections.emptyList();
        }
        return new ArrayList<>();
    }
}
