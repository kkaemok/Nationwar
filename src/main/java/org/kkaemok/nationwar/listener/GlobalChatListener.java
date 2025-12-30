package org.kkaemok.nationwar.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kkaemok.nationwar.Nationwar;

public class GlobalChatListener implements Listener {
    private final Nationwar plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // HEX 색상(&#rrggbb)까지 지원하는 레거시 시리얼라이저 미리 생성
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public GlobalChatListener(Nationwar plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlobalChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // 국가 채팅 모드인 경우 스킵
        if (plugin.getGuildChatManager().isGuildChatMode(player.getUniqueId())) return;

        // LuckPerms 데이터 가져오기
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());

        String prefixStr = (user != null && user.getCachedData().getMetaData().getPrefix() != null)
                ? user.getCachedData().getMetaData().getPrefix() : "";

        String suffixStr = (user != null && user.getCachedData().getMetaData().getSuffix() != null)
                ? user.getCachedData().getMetaData().getSuffix() : "";

        // [핵심] 스마트 파싱 (Gradient, Hex, Legacy 모두 지원)
        Component prefixComp = parseColor(prefixStr);
        Component suffixComp = parseColor(suffixStr);

        // 최종 메시지 조립: [접두사]이름[접미사]: [채팅]
        Component finalMsg = Component.empty()
                .append(prefixComp)
                .append(Component.text(player.getName()))
                .append(suffixComp)
                .append(mm.deserialize("<white>: </white>")) // 구분자
                .append(event.message());

        event.renderer((source, sourceDisplayName, message, viewer) -> finalMsg);
    }

    /**
     * 문자열에 따라 MiniMessage 또는 Legacy(&) 방식을 자동으로 선택하여 변환합니다.
     */
    private Component parseColor(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        // 1. MiniMessage 태그(<gradient, <#, <color 등)가 포함된 경우 MiniMessage로 파싱
        // (단순히 <만 체크하면 이모티콘 등과 겹칠 수 있으니 태그 형식을 대략 체크)
        if (text.contains("<") && text.contains(">")) {
            try {
                return mm.deserialize(text);
            } catch (Exception ignored) {
                // 파싱 실패 시 레거시로 처리
            }
        }

        // 2. 그 외의 경우 (기존 &a, &#ffffff 등) Legacy 방식으로 파싱
        return legacySerializer.deserialize(text);
    }
}