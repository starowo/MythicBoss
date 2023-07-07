package icu.xiyoumc.mmboss.boss;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class BossConfig {

    public enum AnnounceType {
        Participant,
        ALL,
        None
    }

    public AnnounceType announceType;
    public String announceMessage;
    public List<String> detailMessage;
    public int showTop;
    public String rankEntry;

    public BossConfig(ConfigurationSection section) {
        announceType = AnnounceType.valueOf(section.getString("BossDamageRank", "Participant"));
        announceMessage = section.getString("BossDamageMessage", "%boss_name% &f&l已被击败！%details%");
        detailMessage = section.getStringList("BossDamageDetails");
        showTop = section.getInt("ShowTop", 10);
        rankEntry = section.getString("RankEntry", "&e%player% &6造成了 &e%damage% &6伤害");
    }

    public void announceBossKilled(ActiveMob mob, Player player, List<Map.Entry<Player, Double>> damageRank) {
        double totalDamage = 0;
        for (Map.Entry<Player, Double> entry : damageRank) {
            totalDamage += entry.getValue();
        }
        ComponentBuilder builder = new ComponentBuilder("");
        String[] msgs = announceMessage.replaceAll("%boss_name%", mob.getDisplayName()).split("%details%");
        ComponentBuilder detailsBuilder = new ComponentBuilder("");
        for (int i = 1; i < detailMessage.size(); i++) {
            String msg = detailMessage.get(i);
            if (msg.contains("%rank%")) {
                String[] fix = msg.split("%rank%");
                String prefix = ChatColor.translateAlternateColorCodes('&', fix[0]);
                String suffix = fix.length > 1 ? ChatColor.translateAlternateColorCodes('&', fix[1]) : "";
                for (int j = 0; j < showTop && j < damageRank.size(); j++) {
                    Map.Entry<Player, Double> entry = damageRank.get(j);
                    String rate = String.format("%.2f", entry.getValue() / totalDamage * 100);
                    String rank = rankEntry.replaceAll("%player%", entry.getKey().getName()).replaceAll("%damage%", String.valueOf(entry.getValue().intValue())).replaceAll("%rank%", String.valueOf(j + 1)).replaceAll("%rate%", rate);
                    detailsBuilder.append(prefix + PlaceholderAPI.setPlaceholders(player, rank) + suffix);
                    if (j != showTop - 1 && j != damageRank.size() - 1) {
                        detailsBuilder.append(PlaceholderAPI.setPlaceholders(player, detailMessage.get(0)));
                    }
                }
            } else {
                if (msg.contains("%rank_self%")) {
                    int selfRank = 0;
                    int selfDamage = 0;
                    String selfRate = "0%";
                    for (int j = 0; j < damageRank.size(); j++) {
                        Map.Entry<Player, Double> entry = damageRank.get(j);
                        if (entry.getKey().getUniqueId().equals(player.getUniqueId())) {
                            selfRank = j + 1;
                            selfDamage = entry.getValue().intValue();
                            selfRate = String.format("%.2f", entry.getValue() / totalDamage * 100);
                            break;
                        }
                    }
                    if (selfRank == 0) {
                        continue;
                    }
                    String entryMsg = rankEntry.replaceAll("%player%", player.getName()).replaceAll("%damage%", String.valueOf(selfDamage)).replaceAll("%rank%", String.valueOf(selfRank)).replaceAll("%rate%", selfRate);
                    msg = msg.replaceAll("%rank_self%", entryMsg);
                }
                detailsBuilder.append(PlaceholderAPI.setPlaceholders(player, msg));
            }
            if (i != detailMessage.size() - 1) {
                detailsBuilder.append("\n");
            }
        }
        BaseComponent[] details = detailsBuilder.create();
        for (int i = 0; i < msgs.length; i++) {
            builder.append(PlaceholderAPI.setPlaceholders(player, msgs[i]));
            if (i != msgs.length - 1 || announceMessage.endsWith("%details%")) {
                builder.append(PlaceholderAPI.setPlaceholders(player, detailMessage.get(0)));
                builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, details));
            }
        }
        BaseComponent[] components = builder.create();
        player.spigot().sendMessage(components);
    }

}
