package com.gdx.game2048.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import javafx.util.Pair;

import java.util.*;

public class ScoreManager {
    private static ScoreManager instance;
    Preferences prefs;

    private LinkedList<Pair<String, Integer>> listScore = new LinkedList<>();
    private Integer highestScore;

    public Integer getHighestScore() {
        return highestScore;
    }

    public ScoreManager() {
        //get a preferences instance
        prefs = Gdx.app.getPreferences("My Preferences");

        loadList();
    }

    private void loadList() {
        listScore.clear();
        System.out.println("load list");
        //get data from preferences
        for (int i = 0; i < 7; i++) {
            String date = prefs.getString("date" + String.valueOf(i), null);
            int score = prefs.getInteger("score" + String.valueOf(i), 0);
            if (date != null) {
                listScore.add(new Pair<>(date, score));
            }
        }

        highestScore = prefs.getInteger("highestScore", 0);
    }

    public static ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();

        }
        return instance;
    }

    public void addNewScore(int score) {
        // check highest score
        for (Pair<String, Integer> pair: listScore) {
            if (pair.getValue() > highestScore) {
                highestScore = pair.getValue();
            }
        }
        if (highestScore < score)
            highestScore = score;

        prefs.putInteger("highestScore", highestScore);

        // put score
        prefs.putInteger("score" + 0, score);
        prefs.putString("date" + 0, TimeUtils.getCurrentTimeAsUserDateTimeStr());

        int i = 1;
        for (Pair<String,Integer> pair : listScore) {
            prefs.putInteger("score" + i, pair.getValue());
            prefs.putString("date" + i, pair.getKey());
            i++;
        }

        //persist preferences
        prefs.flush();

        loadList();
    }

    public LinkedList<Pair<String, Integer>> getListScore() {
        return listScore;
    }
}
