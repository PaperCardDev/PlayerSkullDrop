package cn.paper_card.player_skull_drop;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

class ConfigManager {

    private final @NotNull PlayerSkullDrop plugin;

    ConfigManager(@NotNull PlayerSkullDrop plugin) {
        this.plugin = plugin;
    }

    int getProbabilityByPlayer() {
        final String path = "probability.player";
        final FileConfiguration c = this.plugin.getConfig();
        final int def = 10;

        if (!c.contains(path, true)) {
            c.set(path, def);
            c.setComments(path, Collections.singletonList("被其它玩家杀死掉落头颅的概率，默认10，含义百分之10"));
        }

        return c.getInt(path, def);
    }


    int getProbabilityByCreeper() {
        final String path = "probability.creeper";
        final FileConfiguration c = this.plugin.getConfig();
        final int def = 10;

        if (!c.contains(path, true)) {
            c.set(path, def);
            c.setComments(path, Collections.singletonList("被闪电苦力怕炸死掉落头颅的概率，默认10，含义百分之10"));
        }

        return c.getInt(path, def);
    }


    void getAll() {
        this.getProbabilityByPlayer();
        this.getProbabilityByCreeper();
    }

    void save() {
        this.plugin.saveConfig();
    }

    void reload() {
        this.plugin.reloadConfig();
    }
}
