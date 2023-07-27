package cn.paper_card.player_skull_drop;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;

public final class PlayerSkullDrop extends JavaPlugin {

    private final static String PATH_PROBABILITY_PLAYER = "probability.player";
    private final static String PATH_PROBABILITY_CREEPER = "probability.creeper";

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new TheListener(), this);

        this.setProbabilityByPlayer(this.getProbabilityByPlayer());
        this.setProbabilityByCreeper(this.getProbabilityByCreeper());
        this.saveConfig();
    }

    @Override
    public void onDisable() {
        this.saveConfig();
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
        skullMeta.setPlayerProfile(player.getPlayerProfile());

        // 显示名称
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

    int getProbabilityByPlayer() {
        return this.getConfig().getInt(PATH_PROBABILITY_PLAYER, 10);
    }

    void setProbabilityByPlayer(int value) {
        this.getConfig().set(PATH_PROBABILITY_PLAYER, value);
    }

    int getProbabilityByCreeper() {
        return this.getConfig().getInt(PATH_PROBABILITY_CREEPER, 10);
    }

    void setProbabilityByCreeper(int value) {
        this.getConfig().set(PATH_PROBABILITY_CREEPER, value);
    }


    private class TheListener implements Listener {

        private final PlayerLastDamager playerLastDamager;

        TheListener() {
            this.playerLastDamager = new PlayerLastDamager();
        }

        private double randomDouble100() {
            return new Random(System.currentTimeMillis()).nextDouble() * 100.0;
        }

        private boolean randomDropByCreeper() {
            return (this.randomDouble100() < getProbabilityByCreeper());
        }

        private boolean randomDropByPlayer() {
            return (this.randomDouble100() < getProbabilityByPlayer());
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
