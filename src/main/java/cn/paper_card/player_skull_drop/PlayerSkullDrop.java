package cn.paper_card.player_skull_drop;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;

public final class PlayerSkullDrop extends JavaPlugin {


    private final @NotNull TextComponent prefix;

    private final @NotNull ConfigManager configManager;

    public PlayerSkullDrop() {
        this.prefix = Component.text()
                .append(Component.text("[").color(NamedTextColor.DARK_AQUA))
                .append(Component.text(this.getName()).color(NamedTextColor.GOLD))
                .append(Component.text("]").color(NamedTextColor.DARK_AQUA))
                .build();

        this.configManager = new ConfigManager(this);
    }

    @Override
    public void onEnable() {
        // 保持配置
        this.configManager.getAll();
        this.configManager.save();

        this.getServer().getPluginManager().registerEvents(new TheListener(), this);

        new SkullCommand(this);

        this.sendInfo(this.getServer().getConsoleSender(), "插件已启用");
    }

    @Override
    public void onDisable() {
        // 保存配置
        this.configManager.save();
    }

    @NotNull ConfigManager getConfigManager() {
        return this.configManager;
    }

    /**
     * 获取指定玩家的头颅，测试通过
     *
     * @param player 指定玩家
     * @return 头颅物品
     */
    @NotNull ItemStack getPlayerHead(@NotNull OfflinePlayer player) {

        final ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);

        final SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        skullMeta.setOwningPlayer(player);

        final PlayerProfile playerProfile = player.getPlayerProfile();
        skullMeta.setPlayerProfile(playerProfile);

        // 显示名称
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }


    @NotNull Permission addPermission(@NotNull String name) {
        final Permission permission = new Permission(name);
        this.getServer().getPluginManager().addPermission(permission);
        return permission;
    }

    void sendError(@NotNull CommandSender sender, @NotNull String error) {
        sender.sendMessage(Component.text()
                .append(this.prefix)
                .appendSpace()
                .append(Component.text(error).color(NamedTextColor.RED))
                .build()
        );
    }

    void sendInfo(@NotNull CommandSender sender, @NotNull String info) {
        sender.sendMessage(Component.text()
                .append(this.prefix)
                .appendSpace()
                .append(Component.text(info).color(NamedTextColor.GREEN))
                .build()
        );
    }


    private class TheListener implements Listener {

        private final PlayerLastDamager playerLastDamager;

        TheListener() {
            this.playerLastDamager = new PlayerLastDamager();
        }

        /**
         * 生成随机数，范围[0,100）
         * @return double
         */
        private double randomDouble100() {
            return new Random(System.currentTimeMillis()).nextDouble() * 100.0;
        }

        private boolean randomDropByCreeper() {
            final int probability = configManager.getProbabilityByCreeper();
            return (this.randomDouble100() < probability);
        }

        private boolean randomDropByPlayer() {
            final int probability = configManager.getProbabilityByPlayer();
            return (this.randomDouble100() < probability);
        }

        @EventHandler
        public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
            final Entity entity = event.getEntity();
            if (entity.getType() != EntityType.PLAYER) return; // 不是玩家被伤害
            // 记录玩家被伤害的事件，以便在玩家死亡时确认凶手
            this.playerLastDamager.set((Player) entity, event.getDamager());
        }

        @EventHandler
        public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
            final Entity entity = event.getEntity();
            if (entity.getType() != EntityType.PLAYER) return; // 不是玩家被伤害
            this.playerLastDamager.set((Player) entity, null); // 重置
        }

        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) {
            final Player playerDeath = event.getPlayer(); // 死亡玩家

            final Entity lastDamager = this.playerLastDamager.get(playerDeath); // 杀死玩家的实体，可能是null

            final Player killer = playerDeath.getKiller(); // 可能是null

            if (killer != null && killer != playerDeath) {
                this.onPlayerKillPlayer(killer, playerDeath, event); // 被其它玩家杀死
            }

            if (lastDamager instanceof final Creeper creeperKiller) {  // 被闪电苦力怕炸死
                if (creeperKiller.isPowered()) {
                    this.onPlayerKillByPoweredCreeper(event, playerDeath);
                }
            }

            this.playerLastDamager.set(playerDeath, null); // 重置
        }

        private void onPlayerKillByPoweredCreeper(@NotNull PlayerDeathEvent event, @NotNull Player player) {
            if (this.randomDropByCreeper()) {
                event.getDrops().add(getPlayerHead(player));
                player.chat("被闪电苦力怕炸掉了头颅！");
            }
        }

        private void onPlayerKillPlayer(@NotNull Player playerKiller, @NotNull Player playerDeath, @NotNull PlayerDeathEvent event) {
            if (this.randomDropByPlayer()) {
                event.getDrops().add(getPlayerHead(playerDeath));
                playerDeath.chat("被" + playerKiller.getName() + "艹掉了头颅！");
            }
        }
    }

    private static class PlayerLastDamager {

        private final @NotNull HashMap<Player, Entity> map;

        PlayerLastDamager() {
            this.map = new HashMap<>();
        }

        void set(@NotNull Player player, @Nullable Entity entity) {
            this.map.put(player, entity);
        }


        @Nullable Entity get(@NotNull Player player) {
            return this.map.get(player);
        }
    }
}
