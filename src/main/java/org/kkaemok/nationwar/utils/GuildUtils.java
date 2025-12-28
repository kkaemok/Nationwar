package org.kkaemok.nationwar.utils;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;

public class GuildUtils {
    public static String getPlayerGuild(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        if (user == null) return "무소속";

        // default 그룹을 제외한 첫 번째 그룹명 찾기
        return user.getNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> ((InheritanceNode) node).getGroupName())
                .filter(group -> !group.equalsIgnoreCase("default"))
                .findFirst()
                .orElse("무소속");
    }
}