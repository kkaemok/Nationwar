package org.kkaemok.nationwar.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.kkaemok.nationwar.Nationwar;
import org.kkaemok.nationwar.data.Statue;
import org.kkaemok.nationwar.utils.GuildUtils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatueListener implements Listener {
    private final Nationwar plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<Integer, Long> attackAlertCooldowns = new HashMap<>(); // 신상별 공격알림 쿨타임

    public StatueListener(Nationwar plugin) { this.plugin = plugin; }

    @EventHandler
    public void onStatueDamage(EntityDamageByEntityEvent event) {
        Statue statue = plugin.getStatueManager().getStatueByEntityId(event.getEntity().getUniqueId());
        if (statue == null) return;

        int startHour = plugin.getConfig().getInt("statue-settings.start-hour", 19);
        int endHour = plugin.getConfig().getInt("statue-settings.end-hour", 21);

        // 한국 시간 기준 체크
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));
        int currentHour = now.getHour();

        if (currentHour < startHour || currentHour >= endHour) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player p) {
                long currentTime = System.currentTimeMillis();
                if (!cooldowns.containsKey(p.getUniqueId()) || (currentTime - cooldowns.get(p.getUniqueId()) >= 15000)) {
                    p.sendMessage(Component.text("지금은 신상 점령 시간이 아닙니다. (" + startHour + "시~" + endHour + "시)").color(NamedTextColor.RED));
                    cooldowns.put(p.getUniqueId(), currentTime);
                }
            }
            return; // 시간 외 공격이면 알림도 보내지 않음
        }

        // [추가] 신상 공격 알림 로직
        String ownerGuild = statue.getOwnerGuild();
        if (ownerGuild != null && !ownerGuild.equalsIgnoreCase("무소속")) {
            long nowMillis = System.currentTimeMillis();
            if (!attackAlertCooldowns.containsKey(statue.getId()) || (nowMillis - attackAlertCooldowns.get(statue.getId()) >= 10000)) {
                Component alertMsg = Component.text("[경고] ").color(NamedTextColor.DARK_RED)
                        .append(Component.text(statue.getName() + " 신상이 공격받고 있습니다!").color(NamedTextColor.RED));

                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> GuildUtils.getPlayerGuild(p).equals(ownerGuild))
                        .forEach(p -> p.sendMessage(alertMsg));

                attackAlertCooldowns.put(statue.getId(), nowMillis);
            }
        }
    }

    @EventHandler
    public void onStatueDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Shulker)) return;
        Statue statue = plugin.getStatueManager().getStatueByEntityId(event.getEntity().getUniqueId());
        if (statue == null) return;

        event.getDrops().clear();
        Player killer = event.getEntity().getKiller();
        String killerGuild = (killer != null) ? GuildUtils.getPlayerGuild(killer) : "Unknown";

        Bukkit.broadcast(Component.text(statue.getName() + " 신상이 " + killerGuild + " 길드에게 점령당했습니다!").color(NamedTextColor.RED));
        plugin.getStatueManager().removeStatue(statue.getId());

        Statue newStatue = plugin.getStatueManager().createStatue(killerGuild + "의 신상", statue.getLocation(), 5000);
        newStatue.setOwnerGuild(killerGuild);
        plugin.getStatueManager().saveStatues();
        plugin.getStatueManager().checkWinCondition();
    }
}