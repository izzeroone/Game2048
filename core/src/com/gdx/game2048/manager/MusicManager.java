package com.gdx.game2048.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;

public class MusicManager {
    private static MusicManager instance;
    private HashMap<String, Music> listMusic = new HashMap<>();

    private boolean muteMusic;
    private boolean muteSound;

    private static String muteAsText;
    private HashMap<String, Sound> listSound = new HashMap<>();


    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
            instance.loadResource();
            instance.muteMusic = GameSetting.getInstance().getMuteMusic();
            instance.muteSound = GameSetting.getInstance().getMuteSound();

        }
        return instance;
    }

    public boolean isMuteMusic() {
        return muteMusic;
    }

    public boolean isMuteSound() {
        return muteSound;
    }

    public String getMuteMusicAsText() {
        if (muteMusic)
            return "OFF";
        return "ON";
    }

    public String getMuteSoundAsText() {
        if (muteSound)
            return "OFF";
        return "ON";
    }

    private void loadResource() {
        listMusic.put("game_background", Gdx.audio.newMusic(Gdx.files.internal("music/game_background.mp3")));
        listMusic.put("menu_background", Gdx.audio.newMusic(Gdx.files.internal("music/menu_backgroundd.mp3")));
        for (Music music : listMusic.values()) {
            if (music != null) {
                music.setLooping(true);
            }
        }


        listSound.put("menu_change",Gdx.audio.newSound(Gdx.files.internal("music/menu_change.mp3")));
        listSound.put("win",Gdx.audio.newSound(Gdx.files.internal("music/win.mp3")));
        listSound.put("lose",Gdx.audio.newSound(Gdx.files.internal("music/lose.mp3")));
        listSound.put("change",Gdx.audio.newSound(Gdx.files.internal("music/change.mp3")));
        listSound.put("menu_select",Gdx.audio.newSound(Gdx.files.internal("music/menu_select.mp3")));
        listSound.put("merge_tile",Gdx.audio.newSound(Gdx.files.internal("music/merge_tile.mp3")));

    }

    public void playSound(String name) {
        if (!muteSound) {
            if (listSound.get(name) != null) {
                Sound sound = listSound.get(name);
                try {
                    sound.play();
                } catch (UnsatisfiedLinkError error) {
                    System.out.println("UnsatisfiedLinkError");
                }
            }
        }
    }

    public void playMusic(String name) {
        if (!muteMusic) {
            if (listMusic.get(name) != null) {
                if (listMusic.get(name).isPlaying()) {
                    return;
                }
                for (Music music : listMusic.values()) {
                    if (music != null && music.isPlaying()) {
                        music.stop();
                    }
                }
                listMusic.get(name).play();
            }
        }
    }

    public void muteMusic(boolean b) {
        muteMusic = b;
        GameSetting.getInstance().changeSetting();

        if (muteMusic)
            for (Music music :
                    listMusic.values()) {
                if (music != null && music.isPlaying()) {
                    music.stop();
                }
            }
    }

    public void muteSound(boolean b) {
        muteSound = b;
        GameSetting.getInstance().changeSetting();
    }
}
