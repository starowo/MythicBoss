package icu.xiyoumc.mmboss.command;

import icu.xiyoumc.mmboss.MMBoss;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

/*
 * /mmboss reload 重载插件
 * /mmboss list 查看所有Boss
 * /mmboss debug announce <mm生物名> <玩家名> 首杀公告
 * /mmboss debug spawn <boss配置id> 生成Boss
 */
public class MMBossCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!sender.isOp()) {
            sender.sendMessage("§c你没有权限");
            return true;
        }
        if (strings.length == 0) {
            sender.sendMessage("§c/mmboss reload 重载插件");
            sender.sendMessage("§c/mmboss list 查看所有Boss");
            sender.sendMessage("§c/mmboss debug announce <mm生物名> <玩家名> 首杀公告");
            sender.sendMessage("§c/mmboss debug spawn <boss配置id> 生成Boss");
            return true;
        }
        if (strings[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§a重载中...");
            MMBoss.getInstance().reload();
            sender.sendMessage("§a重载完成");
            return true;
        }
        if (strings[0].equalsIgnoreCase("list")) {
            sender.sendMessage("§a所有Boss:");
            MMBoss.getInstance().getBossManager().bossList.forEach(boss -> {
                sender.sendMessage("§a" + boss.sectionName + " -> " + boss.name);
            });
            return true;
        }
        if (strings[0].equalsIgnoreCase("debug")) {
            if (strings.length == 1) {
                sender.sendMessage("§c/mmboss debug announce <mm生物名> <玩家名> 首杀公告");
                sender.sendMessage("§c/mmboss debug spawn <boss配置id> 生成Boss");
                return true;
            }
            if (strings[1].equalsIgnoreCase("announce")) {
                if (strings.length != 4) {
                    sender.sendMessage("§c/mmboss debug announce <mm生物名> <玩家名> 首杀公告");
                    return true;
                }
                MMBoss.getInstance().getBossManager().bossList.forEach(boss -> {
                    if (boss.sectionName.equalsIgnoreCase(strings[2])) {
                        MMBoss.getInstance().bossManager.announceFirstKill(boss.sectionName, boss.name, (Player) sender);
                    }
                });
                return true;
            }
            if (strings[1].equalsIgnoreCase("spawn")) {
                if (strings.length != 3) {
                    sender.sendMessage("§c/mmboss debug spawn <boss配置id> 生成Boss");
                    return true;
                }
                MMBoss.getInstance().getBossManager().bossList.forEach(boss -> {
                    if (boss.sectionName.equalsIgnoreCase(strings[2])) {
                        BukkitAPIHelper api = MythicMobs.inst().getAPIHelper();
                        try {
                            api.spawnMythicMob(boss.name, ((Player) sender).getLocation());
                        } catch (InvalidMobTypeException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> result = new java.util.ArrayList<>();
        if (!commandSender.isOp()) {
            return result;
        }
        if (strings.length == 1) {
            result = java.util.Arrays.asList("reload", "list", "debug");
        }
        if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("debug")) {
                result = java.util.Arrays.asList("announce", "spawn");
            }
        }
        if (strings.length == 3) {
            if (strings[0].equalsIgnoreCase("debug")) {
                if (strings[1].equalsIgnoreCase("announce")) {
                    List<String> finalResult = result;
                    MMBoss.getInstance().getBossManager().bossList.forEach(boss -> {
                        finalResult.add(boss.sectionName);
                    });
                }
                if (strings[1].equalsIgnoreCase("spawn")) {
                    List<String> finalResult1 = result;
                    MMBoss.getInstance().getBossManager().bossList.forEach(boss -> {
                        finalResult1.add(boss.sectionName);
                    });
                }
            }
        }
        return getUsableSubCommands(result, strings[strings.length - 1]);
    }

    private List<String> getUsableSubCommands(List<String> subCommands, String arg) {
        List<String> result = new java.util.ArrayList<>();
        for (String subCommand : subCommands) {
            if (subCommand.startsWith(arg)) {
                result.add(subCommand);
            }
        }
        return result;
    }

}
