package org.kkaemok.nationwar.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(Component.text("[+] ").color(NamedTextColor.GREEN).append(Component.text(e.getPlayer().getName())));
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.quitMessage(Component.text("[-] ").color(NamedTextColor.RED).append(Component.text(e.getPlayer().getName())));
    }
}