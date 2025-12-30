package org.kkaemok.nationwar.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kkaemok.nationwar.Nationwar;

public class GuildChatCommand implements CommandExecutor {
    private final Nationwar plugin;

    public GuildChatCommand(Nationwar plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        boolean enabled = plugin.getGuildChatManager().toggleChatMode(player.getUniqueId());
        if (enabled) {
            player.sendMessage(Component.text("국가 채팅 모드가 활성화되었습니다.").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("국가 채팅 모드가 비활성화되었습니다.").color(NamedTextColor.RED));
        }
        return true;
    }
}