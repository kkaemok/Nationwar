package org.kkaemok.nationwar.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kkaemok.nationwar.Nationwar;
import org.kkaemok.nationwar.data.Statue;

public class StatueCommand implements CommandExecutor {
    private final Nationwar plugin;

    public StatueCommand(Nationwar plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player) || !player.isOp()) return true;

        if (label.equalsIgnoreCase("신상만들기")) {
            if (args.length == 0) return false;
            plugin.getStatueManager().createStatue(args[0], player.getLocation(), 10000);
            player.sendMessage(Component.text("신상 '" + args[0] + "' 생성 완료").color(NamedTextColor.GREEN));
            return true;
        }

        if (label.equalsIgnoreCase("신상리스트")) {
            if (plugin.getStatueManager().getAllStatues().isEmpty()) {
                player.sendMessage(Component.text("신상이 없습니다.").color(NamedTextColor.YELLOW));
                return true;
            }
            player.sendMessage(Component.text("-------------신상 리스트------------").color(NamedTextColor.GOLD));
            for (Statue s : plugin.getStatueManager().getAllStatues()) {
                // 좌표 정보를 가져와서 정수로 변환
                int x = s.getLocation().getBlockX();
                int y = s.getLocation().getBlockY();
                int z = s.getLocation().getBlockZ();

                // 요청하신 형식: 신상 이름 (위치: x, y, z)(id:1)
                player.sendMessage(Component.text(String.format("%s (위치: %d, %d, %d)(id:%d)",
                        s.getName(), x, y, z, s.getId())).color(NamedTextColor.WHITE));
            }
            return true;
        }

        if (label.equalsIgnoreCase("신상삭제")) {
            if (args.length == 0) {
                player.sendMessage(Component.text("사용법: /신상삭제 <ID/이름>").color(NamedTextColor.RED));
                return true;
            }
            Statue target;
            try {
                target = plugin.getStatueManager().getStatue(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                target = plugin.getStatueManager().getAllStatues().stream()
                        .filter(s -> s.getName().equals(args[0])).findFirst().orElse(null);
            }

            if (target != null) {
                plugin.getStatueManager().removeStatue(target.getId());
                player.sendMessage(Component.text("신상을 삭제했습니다.").color(NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("해당 신상을 찾을 수 없습니다.").color(NamedTextColor.RED));
            }
            return true;
        }

        if (label.equalsIgnoreCase("신상리로드")) {
            plugin.reloadConfig();
            player.sendMessage(Component.text("설정을 리로드했습니다.").color(NamedTextColor.GREEN));
            return true;
        }
        return false;
    }
}