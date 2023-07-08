package icu.xiyoumc.mmboss.killreward;

import icu.xiyoumc.mmboss.MMBoss;
import icu.xiyoumc.mmboss.util.CommandsUtil;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class KillRewardManager {

    public HashMap<String, SingleReward> rewardHashMap = new HashMap<>();

    public void mobKilled(Player player, ActiveMob mob) {
        String name = mob.getType().getInternalName();
        for (SingleReward reward : rewardHashMap.values()) {
            if (reward.name.contains(name)) {
                reward.addKill(player);
            }
        }
    }

    public void loadRewardsAndDataFromFiles() {
        File folder = new File(MMBoss.getInstance().getDataFolder(), "KillReward");
        if (!folder.exists()) {
            MMBoss.getInstance().saveResource("KillReward/Example.yml", false);
        }
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                ConfigurationSection section = YamlConfiguration.loadConfiguration(file);
                loadRewards(section);
            }
        }
        loadData(MMBoss.getInstance().dataManager.data);
    }

    public void loadRewards(ConfigurationSection section) {
        for (String name : section.getKeys(false)) {
            loadReward(section.getConfigurationSection(name));
        }
    }

    public void loadReward(ConfigurationSection section) {
        rewardHashMap.put(section.getName(), new SingleReward(section));
    }

    public void loadData(ConfigurationSection section) {
        ConfigurationSection rewardSection = section.getConfigurationSection("KillCount");
        if (rewardSection == null) {
            rewardSection = new MemoryConfiguration();
        }
        for (String name : rewardSection.getKeys(false)) {
            ConfigurationSection singleRewardSection = rewardSection.getConfigurationSection(name);
            if (singleRewardSection == null) {
                singleRewardSection = new MemoryConfiguration();
            }
            if (rewardHashMap.containsKey(name)) {
                SingleReward reward = rewardHashMap.get(name);
                for (String uuid : singleRewardSection.getKeys(false)) {
                    reward.killCount.put(UUID.fromString(uuid), singleRewardSection.getInt(uuid));
                }
            }
        }
    }

    public void save(ConfigurationSection section) {
        ConfigurationSection rewardSection = section.createSection("KillCount");
        for (String name : rewardHashMap.keySet()) {
            ConfigurationSection singleRewardSection = rewardSection.createSection(name);
            for (Map.Entry<UUID, Integer> entry : rewardHashMap.get(name).killCount.entrySet()) {
                singleRewardSection.set(entry.getKey().toString(), entry.getValue());
            }
        }
        section.set("KillCount", rewardSection);
    }

    public static class SingleReward {

        public Set<String> name;
        public HashMap<UUID, Integer> killCount;
        public List<PermissionRewardEntry> rewards;

        // constructor by ConfigurationSection
        public SingleReward(ConfigurationSection section) {
            name = new HashSet<>();
            if (section.getString("Target") != null) {
                name.add(section.getString("Target"));
            } else {
                name.addAll(section.getStringList("Target"));
            }
            this.killCount = new HashMap<>();
            this.rewards = new ArrayList<>();
            ConfigurationSection rewardSection = section.getConfigurationSection("Reward");
            for (String requirement : rewardSection.getKeys(false)) {
                rewards.add(new PermissionRewardEntry(rewardSection.getConfigurationSection(requirement)));
            }
        }

        public void addKill(Player player) {
            UUID uuid = player.getUniqueId();
            if (killCount.containsKey(uuid)) {
                killCount.put(uuid, killCount.get(uuid) + 1);
            } else {
                killCount.put(uuid, 1);
            }
            int count = killCount.get(uuid);
            for (PermissionRewardEntry entry : rewards) {
                if (count == entry.requirement) {
                    entry.giveReward(player);
                }
            }
        }

    }

    public static class PermissionRewardEntry {
        public int requirement;
        public List<PermissionReward> rewards;

        // constructor by ConfigurationSection
        public PermissionRewardEntry(ConfigurationSection section) {
            requirement = Integer.parseInt(section.getName());
            rewards = new ArrayList<>();
            for (String permission : section.getKeys(false)) {
                ConfigurationSection permissionSection = section.getConfigurationSection(permission);
                rewards.add(new PermissionReward(permission, permissionSection.getStringList("Commands"), permissionSection.getStringList("Message")));
            }
        }

        public void giveReward(Player player) {
            // check permission reversely
            for (int i = rewards.size() - 1; i >= 0; i--) {
                PermissionReward reward = rewards.get(i);
                if (player.hasPermission(reward.permission) || reward.permission.equals("Default")) {
                    HashMap<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", player.getName());
                    CommandsUtil.proceedCommands(reward.commands, player, placeholders);
                    for (String message : CommandsUtil.setPlaceholders(reward.messages, player, placeholders)) {
                        player.sendMessage(message);
                    }
                    break;
                }
            }
        }
    }

    public static class PermissionReward {

        public String permission;
        public List<String> commands;
        public List<String> messages;

        // constructor
        public PermissionReward(String permission, List<String> commands, List<String> messages) {
            this.permission = permission;
            this.commands = commands;
            this.messages = messages;
        }

    }
}
