package icu.xiyoumc.mmboss.boss;

import icu.xiyoumc.mmboss.MMBoss;
import icu.xiyoumc.mmboss.util.CommandsUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BossManager {

    public enum AnnounceType {
        TITLE, CHAT, COMMAND
    }

    public List<String> announceMobs = new ArrayList<>();
    public HashMap<String, UUID> firstKill = new HashMap<>();
    public AnnounceType defaultAnnounceType = AnnounceType.TITLE;
    public String defaultAnnounce = "&c[&6FirstKill&c] &e%player% &6全服首次击杀了 &e%mob% &6!";
    public HashMap<String, AnnounceType> specialAnnounceType = new HashMap<>();
    public HashMap<String, String> specialAnnounce = new HashMap<>();

    public List<Boss> bossList = new ArrayList<>();
    public HashMap<UUID, HashMap<Player, Double>> bossDamage = new HashMap<>();

    public BossConfig bossConfig;

    public BukkitTask spawnTask;

    public BossManager() {
        spawnTask = Bukkit.getScheduler().runTaskTimer(MMBoss.getInstance(), () -> {
            for (Boss boss : bossList) {
                if (boss.scheduler != null) {
                    BukkitAPIHelper api = MythicMobs.inst().getAPIHelper();
                    List<Entity> entities = boss.spawnLocation.getWorld().getNearbyEntities(boss.spawnLocation, boss.range, boss.range, boss.range)
                            .stream().filter(entity -> api.isMythicMob(entity) && entity.getLocation().distance(boss.spawnLocation) <= boss.range).collect(Collectors.toList());
                    if (entities.size() == 0) {
                        if (!boss.scheduler.isRunning()) {
                            boss.scheduler.start();
                        }
                    }
                    for (Entity entity : entities) {
                        if (api.getMythicMobInstance(entity).getType().getInternalName().equals(boss.name)) {
                            boss.scheduler.reset();
                            break;
                        }
                        if (entity == entities.get(entities.size() - 1)) {
                            if (!boss.scheduler.isRunning()) {
                                boss.scheduler.start();
                            }
                        }
                    }
                    if (boss.scheduler.shouldSpawn()) {
                        try {
                            api.spawnMythicMob(boss.name, boss.spawnLocation);
                            boss.scheduler.reset();
                        } catch (InvalidMobTypeException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 0, 200);
    }

    public void announceBossKilled(ActiveMob mob, HashMap<Player, Double> damage) {
        if (damage == null) {
            return;
        }
        if (bossConfig.announceType == BossConfig.AnnounceType.None) {
            return;
        }
        List<Map.Entry<Player, Double>> damageRank = damage.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).collect(Collectors.toList());
        if (bossConfig.announceType == BossConfig.AnnounceType.Participant) {
            for (Player player : damage.keySet()) {
                bossConfig.announceBossKilled(mob, player, damageRank);
            }
        } else if (bossConfig.announceType == BossConfig.AnnounceType.ALL) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                bossConfig.announceBossKilled(mob, player, damageRank);
            }
        }
    }

    public void announceFirstKill(String name, String internalName, Player player) {
        HashMap<String, String> placeholders = new HashMap<>();
        List<String> message = new ArrayList<>();
        AnnounceType type = defaultAnnounceType;
        if (specialAnnounceType.containsKey(internalName)) {
            type = specialAnnounceType.get(internalName);
        }
        if (specialAnnounce.containsKey(internalName)) {
            message.add(specialAnnounce.get(internalName).replace("%player%", player.getName()).replace("%mob%", name));
        } else {
            message.add(defaultAnnounce.replace("%player%", player.getName()).replace("%mob%", name));
        }
        placeholders.put("player", player.getName());
        placeholders.put("mob", name);
        Collection<? extends Player> allPlayers = Bukkit.getOnlinePlayers();
        if (type == AnnounceType.TITLE) {
            message = CommandsUtil.setPlaceholders(message, player, placeholders);
            for (Player p : allPlayers) {
                String[] titles = message.get(0).split("§:");
                String title = titles[0];
                String subtitle = titles.length > 1 ? titles[1] : "";
                p.sendTitle(title, subtitle, 10, 70, 20);
            }
        } else if (type == AnnounceType.CHAT) {
            message = CommandsUtil.setPlaceholders(message, player, placeholders);
            for (Player p : allPlayers) {
                for (String s : message) {
                    p.sendMessage(s);
                }
            }
        } else if (type == AnnounceType.COMMAND) {
            CommandsUtil.proceedCommands(message, player, placeholders);
        }
    }
    public void announceFirstKill(ActiveMob mob, Player player) {
        String name = mob.getDisplayName();
        String internalName = mob.getType().getInternalName();
        HashMap<String, String> placeholders = new HashMap<>();
        List<String> message = new ArrayList<>();
        AnnounceType type = defaultAnnounceType;
        if (specialAnnounceType.containsKey(internalName)) {
            type = specialAnnounceType.get(internalName);
        }
        if (specialAnnounce.containsKey(internalName)) {
            message.add(specialAnnounce.get(internalName).replace("%player%", player.getName()).replace("%mob%", name));
        } else {
            message.add(defaultAnnounce.replace("%player%", player.getName()).replace("%mob%", name));
        }
        placeholders.put("player", player.getName());
        placeholders.put("mob", name);
        Collection<? extends Player> allPlayers = Bukkit.getOnlinePlayers();
        if (type == AnnounceType.TITLE) {
            message = CommandsUtil.setPlaceholders(message, player, placeholders);
            for (Player p : allPlayers) {
                String[] titles = message.get(0).split("§:");
                String title = titles[0];
                String subtitle = titles.length > 1 ? titles[1] : "";
                p.sendTitle(title, subtitle, 10, 70, 20);
            }
        } else if (type == AnnounceType.CHAT) {
            message = CommandsUtil.setPlaceholders(message, player, placeholders);
            for (Player p : allPlayers) {
                for (String s : message) {
                    p.sendMessage(s);
                }
            }
        } else if (type == AnnounceType.COMMAND) {
            CommandsUtil.proceedCommands(message, player, placeholders);
        }
    }

    public Boss getBossFromEntity(Entity entity) {
        BukkitAPIHelper api = MythicMobs.inst().getAPIHelper();
        ActiveMob mob = api.getMythicMobInstance(entity);
        if (mob == null) {
            return null;
        }
        for (Boss boss : bossList) {
            if (boss.name.equals(mob.getType().getInternalName())) {
                return boss;
            }
        }
        return null;
    }

    public void loadBossesAndDataFromFiles() {
        bossList.clear();
        announceMobs.clear();
        specialAnnounce.clear();
        specialAnnounceType.clear();
        bossDamage.clear();
        bossConfig = new BossConfig(MMBoss.getInstance().getConfig());
        File firstKillFile = new File(MMBoss.getInstance().getDataFolder(), "FirstKill.yml");
        if (!firstKillFile.exists()) {
            MMBoss.getInstance().saveResource("FirstKill.yml", false);
        }
        ConfigurationSection firstKillSection = YamlConfiguration.loadConfiguration(firstKillFile);
        loadFirstKill(firstKillSection);
        File folder = new File(MMBoss.getInstance().getDataFolder(), "Boss");
        if (!folder.exists()) {
            MMBoss.getInstance().saveResource("Boss/ExampleBoss.yml", false);
        }
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                ConfigurationSection section = YamlConfiguration.loadConfiguration(file);
                loadBosses(section);
            }
        }
        loadData(MMBoss.getInstance().dataManager.data);
    }

    public void loadBosses(ConfigurationSection section) {
        for (String name : section.getKeys(false)) {
            ConfigurationSection singleSection = section.getConfigurationSection(name);
            Boss boss = new Boss(singleSection);
            String mmName = boss.name;
            if (MythicMobs.inst().getAPIHelper().getMythicMob(mmName) == null) {
                MMBoss.getInstance().getLogger().warning("无法加载Boss配置 " + section.getName() + "，因为MythicMobs中不存在名为 " + mmName + " 的怪物。");
                continue;
            }
            bossList.add(boss);
        }
    }

    public void loadFirstKill(ConfigurationSection section) {
        List<String> list = section.getStringList("AnnounceMobs");
        if (list != null) {
            announceMobs = list;
        }
        for (String name : section.getKeys(false)) {
            if (name.equalsIgnoreCase("AnnounceMobs")) {
                continue;
            }
            ConfigurationSection singleSection = section.getConfigurationSection(name);
            if (name.equalsIgnoreCase("Default")) {
                defaultAnnounceType = AnnounceType.valueOf(singleSection.getString("AnnounceType", "TITLE").toUpperCase());
                defaultAnnounce = singleSection.getString("AnnounceMessage", "&c[&6FirstKill&c] &e%player% &6全服首次击杀了 &e%mob% &6!");
            } else {
                specialAnnounceType.put(name, AnnounceType.valueOf(singleSection.getString("AnnounceType", "TITLE").toUpperCase()));
                specialAnnounce.put(name, singleSection.getString("AnnounceMessage", "&c[&6FirstKill&c] &e%player% &6全服首次击杀了 &e%mob% &6!"));
            }
        }
    }

    public void loadData(ConfigurationSection section) {
        ConfigurationSection firstKillSection = section.getConfigurationSection("FirstKill");
        if (firstKillSection == null) {
            firstKillSection = section.createSection("FirstKill");
        }
        for (String name : firstKillSection.getKeys(false)) {
            firstKill.put(name, UUID.fromString(firstKillSection.getString(name)));
        }
    }

    public void save(ConfigurationSection section) {
        ConfigurationSection firstKillSection = section.getConfigurationSection("FirstKill");
        if (firstKillSection == null) {
            firstKillSection = section.createSection("FirstKill");
        }
        for (String name : firstKill.keySet()) {
            firstKillSection.set(name, firstKill.get(name).toString());
        }
    }

}
