package com.gdx.game2048.screen;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.gdx.game2048.manager.GameSetting;
import com.gdx.game2048.manager.MusicManager;
import com.gdx.game2048.manager.ScreenManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SettingScreen extends AbstractScreen {

    //Tag for debug
    private static final String TAG = SettingScreen.class.getSimpleName();

    //Step 1: create Skin, TextureAtlas, Button
    //Button
    private Skin gameSkin;

    private TextureAtlas gameAtlas;

    //Text
    FreeTypeFontGenerator fontGenerator;
    FreeTypeFontGenerator georiaFontGenerator;

    FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;
    BitmapFont normalTextFont;
    BitmapFont selectedTextFont;
    BitmapFont titleTextFont;
    TextButton.TextButtonStyle normalTextButtonStyle;
    TextButton.TextButtonStyle selectedTextButtonStyle;
    TextButton.TextButtonStyle titleTextButtonStyle;

    private LinkedHashMap<String, TextButton> lines = new LinkedHashMap<>();

    private long lastFPSTime = System.currentTimeMillis();
    public boolean refreshLastTime = false;

    //Batch for drawing;
    private SpriteBatch batch;

    @Override
    public void buildStage() {
        batch = new SpriteBatch();

        //Loading asset
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ClearSans-Bold.ttf"));
        georiaFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/GeorgiaPro-BlackIt.ttf"));

        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        //Step 2: TextureAtlas with atlas path
        gameAtlas = new TextureAtlas("themes/menu.atlas");

        //Step 3: create skin
        gameSkin = new Skin(gameAtlas);

        //setup button
        createButton();

        //Step 6: add to stage

        for (TextButton line :
                lines.values()) {
            this.addActor(line);
        }

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        handleInput();
        //Reset the transparency of the screen
//        Gdx.gl.glClearColor(1, 1, 1, 1);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        makeLayout(width, height);
        super.resize(width, height);
    }

    private void makeLayout(int width, int height) {

        int screenMidX = width / 2;
        int screenMidY = height / 2;
        int iconSize_Width = 40;
        int iconSize_Height = 40;
        float imageSize = 240;

        // Step 7 : set position
        int buttonPaddingMidX = width/4;
        float rootLine = height* 0.8f;
        float linePadding = height* 0.1f;

        for (int i = 0; i < lines.size(); i++) {
            getByIndex(i).setPosition(screenMidX, rootLine - linePadding*i, Align.center);
        }
    }

    public TextButton getByIndex(int index){
        return (new ArrayList<TextButton>(lines.values())).get(index);
    }

    public void resyncTime() {
        lastFPSTime = System.currentTimeMillis();
    }


    private void createButton() {

        fontParameter.size = Gdx.graphics.getWidth() / 13;
        fontParameter.color = Color.valueOf("#efb75d");
        normalTextFont = fontGenerator.generateFont(fontParameter);

        fontParameter.borderColor = Color.valueOf("#855d4f");
        fontParameter.borderWidth = 1;
        selectedTextFont = fontGenerator.generateFont(fontParameter);

        fontParameter.size = Gdx.graphics.getWidth() / 10;
        fontParameter.color = Color.valueOf("#efb75d");
        titleTextFont = georiaFontGenerator.generateFont(fontParameter);

        normalTextButtonStyle = new TextButton.TextButtonStyle();
        normalTextButtonStyle.font = normalTextFont;

        selectedTextButtonStyle = new TextButton.TextButtonStyle();
        selectedTextButtonStyle.font = selectedTextFont;

        titleTextButtonStyle = new TextButton.TextButtonStyle();
        titleTextButtonStyle.font = titleTextFont;

        lines.put("setting", new TextButton("Game Setting", titleTextButtonStyle));

        lines.put("music", new TextButton("Background Music: " + MusicManager.getInstance().getMuteMusicAsText(), normalTextButtonStyle));
        lines.put("sound", new TextButton("Sound: " + MusicManager.getInstance().getMuteSoundAsText(), normalTextButtonStyle));
        lines.put("tileStyle", new TextButton("Tile Style: " + GameSetting.getInstance().getTileStyleAsText(), normalTextButtonStyle));
        lines.put("cheating", new TextButton("Cheating: " + GameSetting.getInstance().getCheatingAsText(), normalTextButtonStyle));
        lines.put("about", new TextButton("About us", normalTextButtonStyle));
        lines.put("back", new TextButton("BACK", normalTextButtonStyle));

        lines.get("music").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                musicStateChange();
            }
        });

        lines.get("sound").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                soundStateChange();
            }
        });

        lines.get("tileStyle").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                tileStyleChange();
            }
        });

        lines.get("cheating").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                cheatingChange();
            }
        });

        lines.get("about").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                startAbout();
            }
        });

        lines.get("back").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                startMain();
            }
        });

        curSelectedState = 2;
        changeSelectState();
    }

    private void startAbout() {
        ScreenManager.getInstance().showScreen(ScreenEnum.ABOUT);
    }

    private void startMain() {
        ScreenManager.getInstance().showScreen(ScreenEnum.MAIN_MENU);
    }

    private void cheatingChange() {
        MusicManager.getInstance().playSound("change");

        GameSetting.getInstance().setCheating(! GameSetting.getInstance().getCheating());
        lines.get("cheating").setText("Cheating: " + GameSetting.getInstance().getCheatingAsText());
    }

    private void tileStyleChange() {
        MusicManager.getInstance().playSound("change");

        GameSetting.getInstance().setTileStyle(! GameSetting.getInstance().getTileStyle());
        lines.get("tileStyle").setText("Tile Style: " + GameSetting.getInstance().getTileStyleAsText());
    }

    private void musicStateChange() {
        MusicManager.getInstance().playSound("change");

        MusicManager.getInstance().muteMusic(! MusicManager.getInstance().isMuteMusic());
        lines.get("music").setText("Background Music: " + MusicManager.getInstance().getMuteMusicAsText());

        if (!MusicManager.getInstance().isMuteMusic()) {
            MusicManager.getInstance().playMusic("menu_background");
        }
    }

    private void soundStateChange() {
        MusicManager.getInstance().playSound("change");

        MusicManager.getInstance().muteSound(! MusicManager.getInstance().isMuteSound());
        lines.get("sound").setText("Sound: " + MusicManager.getInstance().getMuteSoundAsText());
    }

    private int curSelectedState;

    private void UpLine() {
        if (curSelectedState > 2) {
            curSelectedState--;
        }
        else {
            curSelectedState = lines.size();
        }
        changeSelectState();
    }

    private void DownLine() {
        if (curSelectedState < lines.size()) {
            curSelectedState++;
        }
        else {
            curSelectedState = 2;
        }
        changeSelectState();
    }

    private void changeSelectState() {
        for (int i = 1; i < lines.size(); i++) {
            TextButton t = getByIndex(i);
            t.setStyle(normalTextButtonStyle);

            if (curSelectedState == i + 1) {
                t.setStyle(selectedTextButtonStyle);
            }
        }
    }

    private void handleInput(){
        //Don't handle input while there is an active animation

        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
            System.out.println("Move up");
            MusicManager.getInstance().playSound("menu_change");
            UpLine();

        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
            System.out.println("Move down");
            MusicManager.getInstance().playSound("menu_change");
            DownLine();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            System.out.println("Move left");
            if (curSelectedState == 2)
                musicStateChange();
            else if (curSelectedState == 3)
                soundStateChange();
            else if (curSelectedState == 4)
                tileStyleChange();
            else if (curSelectedState == 5)
                cheatingChange();

        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            System.out.println("Move right");
            if (curSelectedState == 2)
                musicStateChange();
            else if (curSelectedState == 3)
                soundStateChange();
            else if (curSelectedState == 4)
                tileStyleChange();
            else if (curSelectedState == 5)
                cheatingChange();

        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            System.out.println("Enter || Space");
            if (curSelectedState == 2)
                musicStateChange();
            else if (curSelectedState == 3)
                soundStateChange();
            else if (curSelectedState == 4)
                tileStyleChange();
            else if (curSelectedState == 5)
                cheatingChange();
            else if (curSelectedState == 6)
                startAbout();
            else
                startMain();
        }
    }
}