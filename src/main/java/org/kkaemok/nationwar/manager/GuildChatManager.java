package org.kkaemok.nationwar.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GuildChatManager {
    private final Set<UUID> guildChatPlayers = new HashSet<>();

    public boolean toggleChatMode(UUID uuid) {
        if (guildChatPlayers.contains(uuid)) {
            guildChatPlayers.remove(uuid);
            return false;
        } else {
            guildChatPlayers.add(uuid);
            return true;
        }
    }

    public boolean isGuildChatMode(UUID uuid) {
        return guildChatPlayers.contains(uuid);
    }
}