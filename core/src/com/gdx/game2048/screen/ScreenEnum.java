package com.gdx.game2048.screen;

public enum ScreenEnum {

    MAIN_MENU {
        public AbstractScreen getScreen(Object... params) {
            return new MenuScreen();
        }
    },
    SCORE {
        public AbstractScreen getScreen(Object... params) {
            return new ScoreScreen();
        }
    },
    SETTING {
        public AbstractScreen getScreen(Object... params) {
            return new SettingScreen();
        }
    },
    GAME{
        @Override
        public AbstractScreen getScreen(Object... params) {
            return new GameScreen((Integer) params[0], ((Integer) params[1]), (Boolean) params[2], (Integer) params[3]);
        }
    };

    public abstract AbstractScreen getScreen(Object... params);
}