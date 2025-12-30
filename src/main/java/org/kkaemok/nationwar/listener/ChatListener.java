package org.kkaemok.nationwar.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kkaemok.nationwar.Nationwar;
import org.kkaemok.nationwar.utils.GuildUtils;

public class ChatListener implements Listener {
    private final Nationwar plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // HEX 지원 시리얼라이저
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public ChatListener(Nationwar plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getGuildChatManager().isGuildChatMode(player.getUniqueId())) return;

        event.setCancelled(true);

        String guild = GuildUtils.getPlayerGuild(player);
        if (guild.equals("무소속")) {
            player.sendMessage(Component.text("소속된 국가가 없어 국가 채팅을 사용할 수 없습니다.").color(NamedTextColor.RED));
            return;
        }

        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());

        String prefixStr = (user != null && user.getCachedData().getMetaData().getPrefix() != null)
                ? user.getCachedData().getMetaData().getPrefix() : "";

        String suffixStr = (user != null && user.getCachedData().getMetaData().getSuffix() != null)
                ? user.getCachedData().getMetaData().getSuffix() : "";

        // 스마트 파싱 적용
        Component prefixComp = parseColor(prefixStr);
        Component suffixComp = parseColor(suffixStr);

        // 국가 채팅 메시지 조립
        Component chatMsg = Component.text("<국가채팅> ").color(NamedTextColor.AQUA)
                .append(prefixComp)
                .append(Component.text(player.getName()).color(NamedTextColor.WHITE))
                .append(suffixComp)
                .append(Component.text(": ").color(NamedTextColor.WHITE))
                .append(event.message());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (GuildUtils.getPlayerGuild(p).equals(guild)) {
                p.sendMessage(chatMsg);
            }
        }
        Bukkit.getConsoleSender().sendMessage(chatMsg);
    }

    private Component parseColor(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        if (text.contains("<") && text.contains(">")) {
            try {
                return mm.deserialize(text);
            } catch (Exception ignored) {
            }
        }
        return legacySerializer.deserialize(text);
    }
}