package icu.xiyoumc.mmboss.util;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class CommandsUtil {

    public static List<String> setPlaceholders(List<String> commands, Player player, HashMap<String, String> placeholders) {
        List<String> newCommands = Lists.newArrayList();
        for (String command : commands) {
            for (String placeholder : placeholders.keySet()) {
                command = command.replace("%" + placeholder + "%", placeholders.get(placeholder));
            }
            command = PlaceholderAPI.setPlaceholders(player, command);
            newCommands.add(command.replaceAll("&", ChatColor.COLOR_CHAR + ""));
        }
        return newCommands;
    }

    public static void proceedCommands(List<String> commands, Player player, HashMap<String, String> placeholders) {
        commands = setPlaceholders(commands, player, placeholders);
        for (String command : commands) {
            CommandSender sender = player;
            // 前缀[Console]为控制台执行指令，无前缀或前缀[Player]为玩家执行指令
            if (command.startsWith("[Console]")) {
                sender = Bukkit.getConsoleSender();
                command = command.replace("[Console]", "");
            } else if (command.startsWith("[Player]")) {
                command = command.replace("[Player]", "");
            }
            if (sender instanceof Player) {
                boolean isOp = sender.isOp();
                sender.setOp(true);
                try {
                    Bukkit.dispatchCommand(sender, command);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sender.setOp(isOp);
                }
            } else {
                try {
                    Bukkit.dispatchCommand(sender, command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
