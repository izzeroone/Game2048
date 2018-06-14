package com.gdx.game2048;

import com.badlogic.gdx.Game;
import com.gdx.game2048.manager.ScreenManager;
import com.gdx.game2048.screen.ScreenEnum;

public class MainGame extends Game {
    @Override
    public void create() {
        ScreenManager.getInstance().initialize(this);
        ScreenManager.getInstance().showScreen(ScreenEnum.MAIN_MENU);
    }
}
