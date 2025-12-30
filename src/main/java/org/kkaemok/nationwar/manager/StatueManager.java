package org.kkaemok.nationwar.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.kkaemok.nationwar.Nationwar;
import org.kkaemok.nationwar.data.Statue;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StatueManager {
    private final Nationwar plugin;
    private final Map<Integer, Statue> statues = new HashMap<>();
    private final File file;
    private int nextId = 1;

    public StatueManager(Nationwar plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "statues.yml");
        loadStatues();
    }

    public Statue createStatue(String name, Location loc, double health) {
        // [수정] 신호기 구조물 설치 (1단계: 3x3 다이아몬드 블록 + 중심 신호기)
        Location center = loc.getBlock().getLocation();

        // 1. 하단 3x3 다이아몬드 블록 설치 (y-1 레이어)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                center.clone().add(x, -1, z).getBlock().setType(Material.DIAMOND_BLOCK);
            }
        }

        // 2. 중심 위치에 신호기 설치 (y 레이어)
        center.getBlock().setType(Material.BEACON);

        // 3. 엔티티 소환 위치 (신호기 정중앙 위)
        Location spawnLoc = center.clone().add(0.5, 0, 0.5);

        Shulker shulker = (Shulker) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.SHULKER);
        shulker.setAI(false);
        shulker.setCustomNameVisible(true);
        shulker.customName(Component.text(name));

        AttributeInstance scaleAttr = shulker.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(3.0);

        AttributeInstance maxHealthAttr = shulker.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(health);
            shulker.setHealth(health);
        }

        BlockDisplay display = (BlockDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
        display.setBlock(Material.WHITE_CONCRETE.createBlockData());

        float scale = 3.0625f;
        Transformation transformation = new Transformation(
                new Vector3f(-0.5f * scale, 0f, -0.5f * scale),
                new AxisAngle4f(),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f()
        );
        display.setTransformation(transformation);

        Statue statue = new Statue(nextId++, name, spawnLoc, shulker.getUniqueId(), display.getUniqueId());
        statue.setBossBar(Bukkit.createBossBar(name + " 신상", BarColor.WHITE, BarStyle.SOLID));

        statues.put(statue.getId(), statue);
        saveStatues();
        return statue;
    }

    public void removeStatue(int id) {
        Statue statue = statues.remove(id);
        if (statue != null) {
            // [추가] 신상 삭제 시 신호기 구조물도 제거 (공기로 변경)
            Location loc = statue.getLocation().getBlock().getLocation();
            loc.getBlock().setType(Material.AIR); // 신호기 제거
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    loc.clone().add(x, -1, z).getBlock().setType(Material.AIR); // 다이아몬드 블록 제거
                }
            }

            if (statue.getShulker() != null) statue.getShulker().remove();
            if (statue.getDisplay() != null) statue.getDisplay().remove();
            if (statue.getBossBar() != null) statue.getBossBar().removeAll();
            saveStatues();
        }
    }

    public Statue getStatue(int id) { return statues.get(id); }
    public Statue getStatueByEntityId(UUID uuid) {
        return statues.values().stream().filter(s -> s.getShulkerUUID().equals(uuid)).findFirst().orElse(null);
    }
    public Collection<Statue> getAllStatues() { return statues.values(); }

    public void saveStatues() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("nextId", nextId);
        for (Statue s : statues.values()) {
            String path = "statues." + s.getId();
            config.set(path + ".name", s.getName());
            config.set(path + ".location", s.getLocation());
            config.set(path + ".shulkerUUID", s.getShulkerUUID().toString());
            config.set(path + ".displayUUID", s.getDisplayUUID().toString());
            config.set(path + ".ownerGuild", s.getOwnerGuild());
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadStatues() {
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        nextId = config.getInt("nextId", 1);
        if (config.getConfigurationSection("statues") == null) return;

        for (String key : config.getConfigurationSection("statues").getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                String path = "statues." + key;
                Statue statue = new Statue(id,
                        config.getString(path + ".name"),
                        config.getLocation(path + ".location"),
                        UUID.fromString(config.getString(path + ".shulkerUUID")),
                        UUID.fromString(config.getString(path + ".displayUUID")));

                statue.setOwnerGuild(config.getString(path + ".ownerGuild"));
                statue.setBossBar(Bukkit.createBossBar(statue.getName() + " 신상", BarColor.WHITE, BarStyle.SOLID));
                statues.put(id, statue);
            } catch (Exception ignored) {}
        }
    }

    public void removeAllBossBars() {
        statues.values().forEach(s -> { if (s.getBossBar() != null) s.getBossBar().removeAll(); });
    }

    public void checkWinCondition() {
        if (statues.isEmpty()) return;
        List<Statue> all = new ArrayList<>(statues.values());
        String firstOwner = all.get(0).getOwnerGuild();
        if (firstOwner == null || firstOwner.equalsIgnoreCase("무소속")) return;

        if (all.stream().allMatch(s -> firstOwner.equals(s.getOwnerGuild()))) {
            Bukkit.getServer().showTitle(net.kyori.adventure.title.Title.title(
                    Component.text("우승!!").color(NamedTextColor.GOLD),
                    Component.text(firstOwner + " 길드가 모든 신상을 점령했습니다.").color(NamedTextColor.YELLOW)
            ));
        }
    }
}