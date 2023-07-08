package icu.xiyoumc.mmboss.papi;

import icu.xiyoumc.mmboss.MMBoss;
import icu.xiyoumc.mmboss.boss.BossManager;
import icu.xiyoumc.mmboss.killreward.KillRewardManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIExtension extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "mmboss";
    }

    @Override
    public @NotNull String getAuthor() {
        return "LunarSilhouette";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    /*
     * %mmboss_firstkill_mm生物id% -> 首杀者
     * %mmboss_killreward_count_配置里的id% -> 已击杀数量
     * %mmboss_killreward_target_配置里的id% -> 获得下一个击杀奖励的数量
     */
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");
        if (args.length == 0) {
            return "";
        }
        if (args[1].equalsIgnoreCase("firstkill")) {
            if (args.length != 2) {
                return "";
            }
            String bossName = args[1];
            BossManager manager = MMBoss.getInstance().bossManager;
            if (manager.firstKill.containsKey(bossName)) {
                OfflinePlayer killer = Bukkit.getOfflinePlayer(manager.firstKill.get(bossName));
                if (killer != null) {
                    if (killer.getPlayer() != null) {
                        return killer.getPlayer().getDisplayName();
                    }
                    return killer.getName();
                }
            }
            return "";
        }
        if (args[1].equalsIgnoreCase("killreward")) {
            if (args.length != 3) {
                return "";
            }
            if (args[2].equalsIgnoreCase("count")) {
                String bossName = args[2];
                KillRewardManager manager = MMBoss.getInstance().killRewardManager;
                KillRewardManager.SingleReward reward = manager.rewardHashMap.get(bossName);
                if (reward == null) {
                    return "-";
                }
                return "" + reward.killCount.getOrDefault(player.getUniqueId(), 0);
            }
            if (args[2].equalsIgnoreCase("target")) {
                String bossName = args[2];
                KillRewardManager manager = MMBoss.getInstance().killRewardManager;
                KillRewardManager.SingleReward reward = manager.rewardHashMap.get(bossName);
                if (reward == null) {
                    return "-";
                }
                int count = reward.killCount.getOrDefault(player.getUniqueId(), 0);
                for (int i = 0; i < reward.rewards.size(); i++) {
                    if (count < reward.rewards.get(i).requirement) {
                        if (player.getPlayer() != null) {
                            Player p = player.getPlayer();
                            for (KillRewardManager.PermissionReward r : reward.rewards.get(i).rewards) {
                                if (p.hasPermission(r.permission)) {
                                    return "" + reward.rewards.get(i).requirement;
                                }
                            }
                            continue;
                        }
                        return "" + reward.rewards.get(i).requirement;
                    }
                }
                return "-";
            }
        }
        return "";
    }
}
