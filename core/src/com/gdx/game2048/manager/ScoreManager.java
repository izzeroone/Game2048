package com.gdx.game2048.manager;

public class ScoreManager {
    private static ScoreManager instance;

    public static ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();

        }
        return instance;
    }


}
