package org.kkaemok.nationwar;

import org.bukkit.plugin.java.JavaPlugin;
import org.kkaemok.nationwar.command.StatueCommand;
import org.kkaemok.nationwar.listener.PlayerListener;
import org.kkaemok.nationwar.listener.StatueListener;
import org.kkaemok.nationwar.manager.StatueManager;
import org.kkaemok.nationwar.task.GameLoopTask;

public class Nationwar extends JavaPlugin {

    private static Nationwar instance;
    private StatueManager statueManager;

    @Override
    public void onEnable() {
        instance = this;

        // config.yml 파일이 리소스 폴더에 실제 존재해야 에러가 발생하지 않습니다.
        saveDefaultConfig();

        this.statueManager = new StatueManager(this);

        // 커맨드 실행기 객체 생성
        StatueCommand statueCommand = new StatueCommand(this);

        // 커맨드 등록 (plugin.yml과 일치해야 함)
        if (getCommand("신상만들기") != null) getCommand("신상만들기").setExecutor(statueCommand);
        if (getCommand("신상리스트") != null) getCommand("신상리스트").setExecutor(statueCommand);
        if (getCommand("신상삭제") != null) getCommand("신상삭제").setExecutor(statueCommand);
        if (getCommand("신상리로드") != null) getCommand("신상리로드").setExecutor(statueCommand);

        // [주의] plugin.yml에서 삭제한 "신상선택"은 여기서 등록하지 않습니다.

        // 리스너 등록
        getServer().getPluginManager().registerEvents(new StatueListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // 게임 루프 (1초 = 20틱)
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
}