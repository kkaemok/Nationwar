package org.kkaemok.nationwar.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Shulker;

import java.util.UUID;

public class Statue {
    private final int id;
    private String name;
    private final Location location;
    private UUID shulkerUUID;
    private UUID displayUUID;
    private String ownerGuild;
    private transient BossBar bossBar;

    public Statue(int id, String name, Location location, UUID shulkerUUID, UUID displayUUID) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.shulkerUUID = shulkerUUID;
        this.displayUUID = displayUUID;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public UUID getShulkerUUID() { return shulkerUUID; }
    public UUID getDisplayUUID() { return displayUUID; }
    public String getOwnerGuild() { return ownerGuild; }
    public void setOwnerGuild(String ownerGuild) { this.ownerGuild = ownerGuild; }
    public BossBar getBossBar() { return bossBar; }
    public void setBossBar(BossBar bossBar) { this.bossBar = bossBar; }

    public Shulker getShulker() {
        if (Bukkit.getEntity(shulkerUUID) instanceof Shulker s) return s;
        return null;
    }

    public BlockDisplay getDisplay() {
        if (Bukkit.getEntity(displayUUID) instanceof BlockDisplay d) return d;
        return null;
    }
}