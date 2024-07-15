package cn.paper_card.player_skull_drop;

import cn.paper_card.mc_command.NewMcCommand;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class SkullCommand extends NewMcCommand.HasSub {

    private final @NotNull PlayerSkullDrop plugin;

    private final @NotNull Permission permission;


    protected SkullCommand(@NotNull PlayerSkullDrop plugin) {
        super("player-skull-drop");
        this.plugin = plugin;

        final PluginCommand command = plugin.getCommand(this.getLabel());
        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);

        this.permission = Objects.requireNonNull(plugin.getServer().getPluginManager().getPermission(this.getLabel() + "." + "command"));

        this.addSub(new Get());
        this.addSub(new Reload());
    }

    @Override
    protected boolean canExecute(@NotNull CommandSender commandSender) {
        return commandSender.hasPermission(this.permission);
    }

    class Get extends NewMcCommand {

        private final @NotNull Permission permission;

        protected Get() {
            super("get");
            this.permission = plugin.addPermission(SkullCommand.this.permission.getName() + "." + this.getLabel());
        }

        @Override
        protected boolean canExecute(@NotNull CommandSender commandSender) {
            return commandSender.hasPermission(this.permission);
        }

        @Override
        public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            final String argPlayer = strings.length > 0 ? strings[0] : null;

            if (argPlayer == null) {
                plugin.sendError(commandSender, "你必须指定参数：玩家名");
                return true;
            }

            if (!(commandSender instanceof final Player player)) {
                plugin.sendError(commandSender, "该命令只能由玩家来执行！");
                return true;
            }

            OfflinePlayer offlinePlayer = null;
            for (OfflinePlayer o : plugin.getServer().getOfflinePlayers()) {
                if (argPlayer.equals(o.getName())) {
                    offlinePlayer = o;
                    break;
                }
            }

            if (offlinePlayer == null) {
                plugin.sendError(commandSender, "找不到该玩家：" + argPlayer);
                return true;
            }

            final ItemStack playerHead = plugin.getPlayerHead(offlinePlayer);
            final PlayerInventory inventory = player.getInventory();
            final ItemStack itemInMainHand = inventory.getItemInMainHand();

            if (itemInMainHand.getType() != Material.AIR) {
                plugin.sendError(commandSender, "请保持空手，获取的头颅将会覆盖物品栏");
                return true;
            }
            inventory.setItemInMainHand(playerHead);
            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            if (strings.length == 1) {
                final String argPlayer = strings[0];
                final LinkedList<String> list = new LinkedList<>();
                if (argPlayer.isEmpty()) {
                    list.add("<玩家名>");
                } else {
                    for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
                        final String name = offlinePlayer.getName();
                        if (name == null) continue;
                        if (name.startsWith(argPlayer)) list.add(name);
                    }
                }
                return list;
            }

            return null;
        }


    }

    class Reload extends NewMcCommand {

        private final @NotNull Permission permission;

        protected Reload() {
            super("reload");
            this.permission = plugin.addPermission(SkullCommand.this.permission.getName() + "." + this.getLabel());
        }

        @Override
        protected boolean canExecute(@NotNull CommandSender commandSender) {
            return commandSender.hasPermission(this.permission);
        }


        @Override
        public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

            plugin.getConfigManager().reload();
            plugin.sendInfo(commandSender, "已重载配置");

            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            return null;
        }
    }
}
