package com.gdx.game2048.screen;

public enum ScreenEnum {

    MAIN_MENU {
        public AbstractScreen getScreen(Object... params) {
            return new MenuScreen();
        }
    },
    GAME{
        @Override
        public AbstractScreen getScreen(Object... params) {
            return new GameScreen((Integer) params[0], ((Integer) params[1]), (Boolean) params[2]);
        }
    };

    public abstract AbstractScreen getScreen(Object... params);
}