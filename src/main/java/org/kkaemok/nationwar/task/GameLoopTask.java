package org.kkaemok.nationwar.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.kkaemok.nationwar.Nationwar;
import org.kkaemok.nationwar.data.Statue;
import org.kkaemok.nationwar.utils.GuildUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameLoopTask extends BukkitRunnable {
    private final Nationwar plugin;
    private final Map<String, Long> lastInvasionAlert = new HashMap<>(); // "길드-신상"별 알림 쿨타임

    public GameLoopTask(Nationwar plugin) { this.plugin = plugin; }

    @Override
    public void run() {
        double range = plugin.getConfig().getDouble("statue-settings.glow-range", 200.0);

        for (Statue statue : plugin.getStatueManager().getAllStatues()) {
            Shulker shulker = statue.getShulker();
            if (shulker == null) continue;

            double currentHp = shulker.getHealth();
            double maxHp = Objects.requireNonNull(shulker.getAttribute(Attribute.MAX_HEALTH)).getValue();
            statue.getBossBar().setTitle(statue.getName() + " [체력:" + (int)currentHp + "]");
            statue.getBossBar().setProgress(Math.max(0, Math.min(1, currentHp / maxHp)));

            String ownerGuild = statue.getOwnerGuild();

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(statue.getLocation().getWorld()) && p.getLocation().distance(statue.getLocation()) <= range) {
                    statue.getBossBar().addPlayer(p);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false));

                    // [추가] 적대 길드원 침입 알림
                    String playerGuild = GuildUtils.getPlayerGuild(p);
                    if (ownerGuild != null && !ownerGuild.equalsIgnoreCase("무소속") && !playerGuild.equals(ownerGuild)) {

                        String alertKey = ownerGuild + "-" + statue.getId() + "-" + p.getName();
                        long now = System.currentTimeMillis();

                        // 침입 알림 쿨타임 (20초)
                        if (!lastInvasionAlert.containsKey(alertKey) || (now - lastInvasionAlert.get(alertKey) >= 20000)) {
                            Component alertMsg = Component.text(playerGuild + " 길드의 " + p.getName() + "이(가) " + statue.getName() + " 신상 주위 200블럭 이내에 들어왔습니다!!")
                                    .color(NamedTextColor.YELLOW);

                            Bukkit.getOnlinePlayers().stream()
                                    .filter(m -> GuildUtils.getPlayerGuild(m).equals(ownerGuild))
                                    .forEach(m -> m.sendMessage(alertMsg));

                            lastInvasionAlert.put(alertKey, now);
                        }
                    }
                } else {
                    statue.getBossBar().removePlayer(p);
                }
            }
        }
    }
}