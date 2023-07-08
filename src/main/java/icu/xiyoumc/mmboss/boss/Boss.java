package icu.xiyoumc.mmboss.boss;

import icu.xiyoumc.mmboss.util.CommandsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Boss {

    public String sectionName;
    public String name; // mm internal name
    public ISpawnScheduler scheduler;
    public Location spawnLocation;
    public int range;
    public List<RewardEntry> rankReward;
    public List<RewardEntry> damageReward;

    // ConfigurationSection constructor
    public Boss(ConfigurationSection section) {
        sectionName = section.getName();
        name = section.getString("Mob");
        if (section.contains("Spawn")) {
            switch (section.getString("Spawn.Type").toUpperCase(Locale.ROOT)) {
                case "CD":
                    scheduler = new CoolDownScheduler(section.getString("Spawn.Time"));
                    break;
                case "TIME":
                    String[] time = section.getString("Spawn.Time").split(",");
                    scheduler = new TimeScheduler(time[0], time[1]);
                    break;
            }
            String world = section.getString("Spawn.World");
            List<Double> xyz = section.getDoubleList("Spawn.Position");
            spawnLocation = new Location(Bukkit.getWorld(world), xyz.get(0), xyz.get(1), xyz.get(2));
            range = section.getInt("Spawn.Range");
        }
        if (section.contains("DamageReward")) {
            ConfigurationSection rewardSection = section.getConfigurationSection("DamageReward");
            if (rewardSection.contains("Rank")) {
                rankReward = RewardEntry.getRewardList(rewardSection.getConfigurationSection("Rank"));
            }
            if (rewardSection.contains("Damage")) {
                damageReward = RewardEntry.getRewardList(rewardSection.getConfigurationSection("Damage"));
            }
        }
    }

    public void giveReward(Player player, int damage, int rank) {
        if (damageReward != null) {
            for (RewardEntry entry : damageReward) {
                if (damage >= entry.requirement) {
                    entry.giveReward(player);
                    break;
                }
            }
        }
        if (rankReward != null) {
            for (RewardEntry entry : rankReward) {
                if (rank <= entry.requirement) {
                    entry.giveReward(player);
                    break;
                }
            }
        }
    }

    public static class RewardEntry {
        public int requirement;
        public List<String> commands;
        public List<String> messages;

        // constructor
        public RewardEntry(int requirement, List<String> commands, List<String> messages) {
            this.requirement = requirement;
            this.commands = commands;
            this.messages = messages;
        }

        public void giveReward(Player player) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("player", player.getName());
            CommandsUtil.proceedCommands(commands, player, placeholders);
            for (String message : CommandsUtil.setPlaceholders(messages, player, placeholders)) {
                player.sendMessage(message);
            }
        }

        public static List<RewardEntry> getRewardList(ConfigurationSection section) {
            List<RewardEntry> rewardList = new ArrayList<>();
            for (String key : section.getKeys(false)) {
                int requirement = Integer.parseInt(key);
                List<String> commands = section.getStringList(key + ".Commands");
                List<String> messages = section.getStringList(key + ".Message");
                rewardList.add(new RewardEntry(requirement, commands, messages));
            }
            return rewardList;
        }
    }
}
