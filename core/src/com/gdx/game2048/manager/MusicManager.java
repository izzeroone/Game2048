package com.gdx.game2048.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import java.util.HashMap;

public class MusicManager {
    private static MusicManager instance;
    private Music mainTheme;
    private boolean mute;

    private static String muteAsText;
    private HashMap<String, Music> listMusic = new HashMap<>();


    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
            instance.loadResource();
            instance.mute = true;
        }
        return instance;
    }

    public boolean isMute() {
        return mute;
    }

    public String getMuteAsText() {
        if (mute)
            return "OFF";
        return "ON";
    }

    private void loadResource() {
        mainTheme = Gdx.audio.newMusic(Gdx.files.internal("music/maintheme.mp3"));

        listMusic.put("menu_change", Gdx.audio.newMusic(Gdx.files.internal("music/menu_change.mp3")));
        listMusic.put("menu_select", Gdx.audio.newMusic(Gdx.files.internal("music/menu_select.mp3")));
    }

    public void playMain() {
        if (! mute) {
            mainTheme.play();
        }
    }

    public void mute(boolean b) {
        mute = b;


    }
}
