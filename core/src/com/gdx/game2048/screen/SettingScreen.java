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
    TextButton.TextButtonStyle normalTextButtonStyle;
    TextButton.TextButtonStyle selectedTextButtonStyle;

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

        //Step 4: create button

        //Step 5: add event

        fontParameter.size = Gdx.graphics.getWidth() / 11;
        fontParameter.color = Color.valueOf("#efb75d");
        normalTextFont = fontGenerator.generateFont(fontParameter);

        fontParameter.borderColor = Color.valueOf("#855d4f");
        fontParameter.borderWidth = 1;
        selectedTextFont = fontGenerator.generateFont(fontParameter);

        fontParameter.borderColor = Color.valueOf("#855d4f");
        fontParameter.size = Gdx.graphics.getWidth() / 8;
        BitmapFont titleTextFont = georiaFontGenerator.generateFont(fontParameter);

        normalTextButtonStyle = new TextButton.TextButtonStyle();
        normalTextButtonStyle.font = normalTextFont;

        selectedTextButtonStyle = new TextButton.TextButtonStyle();
        selectedTextButtonStyle.font = selectedTextFont;

        TextButton.TextButtonStyle titleTextButtonStyle = new TextButton.TextButtonStyle();
        titleTextButtonStyle.font = titleTextFont;

        lines.put("setting", new TextButton("Game Setting", titleTextButtonStyle));

        lines.put("music", new TextButton("Music: " + MusicManager.getInstance().getMuteAsText(), normalTextButtonStyle));
        lines.put("tileStyle", new TextButton("Tile Style: " + GameSetting.getInstance().getTileStyleAsText(), normalTextButtonStyle));
        lines.put("cheating", new TextButton("Cheating: " + GameSetting.getInstance().getCheatingAsText(), normalTextButtonStyle));

        lines.get("music").addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                musicStateChange();
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

        curSelectedState = 2;
        changeSelectState();
    }

    private void cheatingChange() {
        GameSetting.getInstance().setCheating(! GameSetting.getInstance().getCheating());
        lines.get("cheating").setText("Cheating: " + GameSetting.getInstance().getCheatingAsText());
    }

    private void tileStyleChange() {
        GameSetting.getInstance().setTileStyle(! GameSetting.getInstance().getTileStyle());
        lines.get("tileStyle").setText("Tile Style: " + GameSetting.getInstance().getTileStyleAsText());
    }

    private void musicStateChange() {
        MusicManager.getInstance().mute(! MusicManager.getInstance().isMute());
        lines.get("music").setText("Music: " + MusicManager.getInstance().getMuteAsText());
    }

    private int curSelectedState;

    private void UpLine() {
        if (curSelectedState > 1) {
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
            curSelectedState = 1;
        }
        changeSelectState();
    }

    private void changeSelectState() {
        for (int i = 0; i < lines.size(); i++) {
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
            UpLine();

        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
            System.out.println("Move down");
            DownLine();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            System.out.println("Move left");
            if (curSelectedState == 2)
                musicStateChange();

        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            System.out.println("Move right");
            if (curSelectedState == 2)
                musicStateChange();

        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            System.out.println("Enter || Space");
            if (curSelectedState == 2)
                musicStateChange();
        }
    }

}