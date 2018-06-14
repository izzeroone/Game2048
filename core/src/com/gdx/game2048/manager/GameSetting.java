package com.gdx.game2048.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameSetting {
    private static GameSetting instance;
    Preferences prefs;

    private boolean tileStyle; // true = Circle, false = square
    private boolean cheating;

    public String getCheatingAsText() {
        if (cheating)
            return "YES";
        else
            return "NO";
    }

    public String getTileStyleAsText(){
        if (tileStyle)
            return "Circle";
        else
            return "Square";
    }

    public GameSetting() {
        //get a preferences instance
        prefs = Gdx.app.getPreferences("My Preferences");

        loadData();
    }

    private void loadData() {
        tileStyle = prefs.getBoolean("tileStyle", false);
        cheating = prefs.getBoolean("cheating", false);
    }

    public void changeSetting() {
        prefs.putBoolean("tileStyle", tileStyle);
        prefs.putBoolean("cheating", cheating);
        prefs.putBoolean("muteMusic", MusicManager.getInstance().isMuteMusic());
        prefs.putBoolean("muteSound", MusicManager.getInstance().isMuteSound());

        prefs.flush();
    }

    public static GameSetting getInstance() {
        if (instance == null) {
            instance = new GameSetting();

        }
        return instance;
    }


    public boolean getTileStyle() {
        return tileStyle;
    }

    public void setTileStyle(boolean tileStyle) {
        this.tileStyle = tileStyle;
        changeSetting();
    }


    public boolean getCheating() {
        return cheating;
    }

    public void setCheating(boolean cheating) {
        this.cheating = cheating;
        changeSetting();
    }

    public boolean getMuteMusic() {
        return prefs.getBoolean("muteMusic", false);
    }
    public boolean getMuteSound() {
        return prefs.getBoolean("muteSound", false);
    }

}
