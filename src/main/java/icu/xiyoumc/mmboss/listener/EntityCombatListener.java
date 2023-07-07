package icu.xiyoumc.mmboss.listener;

import icu.xiyoumc.mmboss.MMBoss;
import icu.xiyoumc.mmboss.boss.Boss;
import icu.xiyoumc.mmboss.boss.BossManager;
import icu.xiyoumc.mmboss.killreward.KillRewardManager;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityCombatListener implements Listener {

    private Entity getRealDamager(Entity damager) {
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Entity) {
                return (Entity) projectile.getShooter();
            }
        }
        return damager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = getRealDamager(event.getDamager());
        if(!(damager instanceof Player)) {
            return;
        }
        Player player = (Player) damager;
        BossManager manager = MMBoss.getInstance().bossManager;
        Boss boss = manager.getBossFromEntity(entity);
        if (boss == null) {
            return;
        }
        if (boss.rankReward != null || boss.damageReward != null) {
            double damage = event.getFinalDamage();
            HashMap<Player, Double> damageMap;
            if (manager.bossDamage.containsKey(entity.getUniqueId())) {
                damageMap = manager.bossDamage.get(entity.getUniqueId());
            } else {
                damageMap = new HashMap<>();
            }
            if (damageMap.containsKey(player)) {
                damageMap.put(player, damageMap.get(player) + damage);
            } else {
                damageMap.put(player, damage);
            }
            manager.bossDamage.put(entity.getUniqueId(), damageMap);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        BossManager manager = MMBoss.getInstance().bossManager;
        BukkitAPIHelper api = MythicMobs.inst().getAPIHelper();
        ActiveMob mob = api.getMythicMobInstance(entity);
        if (mob == null) {
            return;
        }
        Player player = event.getEntity().getKiller();
        KillRewardManager killRewardManager = MMBoss.getInstance().killRewardManager;
        killRewardManager.mobKilled(player, mob);
        if (manager.announceMobs.contains(mob.getType().getInternalName()) && !manager.firstKill.containsKey(mob.getType().getInternalName())) {
            if (player != null) {
                manager.firstKill.put(mob.getType().getInternalName(), player.getUniqueId());
                manager.announceFirstKill(mob, player);
            }
        }
        Boss boss = manager.getBossFromEntity(entity);
        if (boss == null) {
            return;
        }
        if (boss.rankReward != null || boss.damageReward != null) {
            HashMap<Player, Double> damageMap = manager.bossDamage.get(entity.getUniqueId());
            manager.announceBossKilled(mob, damageMap);
            if (damageMap == null) {
                return;
            }
            List<Map.Entry<Player, Double>> rank = damageMap.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).collect(Collectors.toList());
            for (int i = 0; i < rank.size(); i++) {
                Player p = rank.get(i).getKey();
                double damage = rank.get(i).getValue();
                boss.giveReward(p, (int) damage, i + 1);
            }
            manager.bossDamage.remove(entity.getUniqueId());
        }
    }

}
