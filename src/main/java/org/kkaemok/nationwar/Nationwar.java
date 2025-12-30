package org.kkaemok.nationwar;

import org.bukkit.plugin.java.JavaPlugin;
import org.kkaemok.nationwar.command.StatueCommand;
import org.kkaemok.nationwar.command.GuildChatCommand;
import org.kkaemok.nationwar.listener.ChatListener;
import org.kkaemok.nationwar.listener.GlobalChatListener; // 추가
import org.kkaemok.nationwar.listener.PlayerListener;
import org.kkaemok.nationwar.listener.StatueListener;
import org.kkaemok.nationwar.manager.StatueManager;
import org.kkaemok.nationwar.manager.GuildChatManager;
import org.kkaemok.nationwar.task.GameLoopTask;

public class Nationwar extends JavaPlugin {

    private static Nationwar instance;
    private StatueManager statueManager;
    private GuildChatManager guildChatManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.statueManager = new StatueManager(this);
        this.guildChatManager = new GuildChatManager();

        StatueCommand statueCommand = new StatueCommand(this);

        if (getCommand("신상만들기") != null) getCommand("신상만들기").setExecutor(statueCommand);
        if (getCommand("신상리스트") != null) getCommand("신상리스트").setExecutor(statueCommand);
        if (getCommand("신상삭제") != null) getCommand("신상삭제").setExecutor(statueCommand);
        if (getCommand("신상리로드") != null) getCommand("신상리로드").setExecutor(statueCommand);

        if (getCommand("국가채팅") != null) getCommand("국가채팅").setExecutor(new GuildChatCommand(this));

        getServer().getPluginManager().registerEvents(new StatueListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // [추가] 일반 채팅 포맷 변경 리스너 등록
        getServer().getPluginManager().registerEvents(new GlobalChatListener(this), this);

        new GameLoopTask(this).runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (statueManager != null) {
            statueManager.saveStatues();
            statueManager.removeAllBossBars();
        }
    }

    public static Nationwar getInstance() { return instance; }
    public StatueManager getStatueManager() { return statueManager; }
    public GuildChatManager getGuildChatManager() { return guildChatManager; }
}