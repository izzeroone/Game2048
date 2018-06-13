package com.gdx.game2048.screen;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.gdx.game2048.manager.MusicManager;
import com.gdx.game2048.manager.ScreenManager;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ScoreScreen extends AbstractScreen {

    //Tag for debug
    private static final String TAG = ScoreScreen.class.getSimpleName();

    //Animation constant
    public static final int BASE_ANIMATION_TIME = 100;
    private static final float MERGING_ACCEL = -0.5f;
    private static final float INITIAL_VELO = (1 - MERGING_ACCEL) / 4;

    //Step 1: create Skin, TextureAtlas, Button
    //Button

    private Skin gameSkin;

    private TextureAtlas gameAtlas;

    private Image imageLevel;

    private int iconPaddingSize;

    //Text
    FreeTypeFontGenerator fontGenerator;
    FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;
    BitmapFont normalTextFont;
    BitmapFont selectedTextFont;
    TextButton.TextButtonStyle normalTextButtonStyle;
    TextButton.TextButtonStyle selectedTextButtonStyle;

    private LinkedHashMap<String, TextButton> lines = new LinkedHashMap<>();

    LinkedHashMap<Integer, Pair<String,String>> levelInfo;
    int curLevel = 3;

    LinkedHashMap<Integer, String> aiInfo;
    int curAI = 1;

    //Timing for draw
    private long lastFPSTime = System.currentTimeMillis();
    public boolean refreshLastTime = false;

    //Batch for drawing;
    private SpriteBatch batch;

    @Override
    public void buildStage() {
        batch = new SpriteBatch();

        //Loading asset
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ClearSans-Bold.ttf"));
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        //Step 2: TextureAtlas with atlas path
        gameAtlas = new TextureAtlas("themes/score.atlas");

        //Step 3: create skin
        gameSkin = new Skin(gameAtlas);

        //setup button
        createButton();

        //Step 6: add to stage
        this.addActor(imageLevel);

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
        iconPaddingSize = 50;
        float imageSize = 240;

        // Step 7 : set position
        int buttonPaddingMidX = width/4;
        float rootLine = height* 0.54f;
        float linePadding = height* 0.1f;

        imageLevel.setSize(imageSize, imageSize);
        imageLevel.setPosition(width*0.5f , height*0.78f, Align.center);

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

        imageLevel = new Image(gameSkin.getDrawable("high_score"));

        fontParameter.size = Gdx.graphics.getWidth() / 11;
        fontParameter.color = Color.valueOf("#efb75d");
        normalTextFont = fontGenerator.generateFont(fontParameter);
        fontParameter.borderColor = Color.valueOf("#855d4f");
        fontParameter.borderWidth = 1;
        selectedTextFont = fontGenerator.generateFont(fontParameter);

        normalTextButtonStyle = new TextButton.TextButtonStyle();
        normalTextButtonStyle.font = normalTextFont;

        selectedTextButtonStyle = new TextButton.TextButtonStyle();
        selectedTextButtonStyle.font = selectedTextFont;

//        lines.put("level", new TextButton(levelInfo.get(curLevel).getKey(), normalTextButtonStyle));
//        lines.put("startGame", new TextButton("Start Game", normalTextButtonStyle));
//        lines.put("ai", new TextButton(aiInfo.get(curAI), normalTextButtonStyle));
//        lines.put("startAI", new TextButton("Play With Help" , normalTextButtonStyle));
//        lines.put("music", new TextButton("Music: ", normalTextButtonStyle));
        

    }

    private void handleInput(){
        //Don't handle input while there is an active animation

        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
            System.out.println("Move up");


        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
            System.out.println("Move down");

        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            System.out.println("Move left");

        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            System.out.println("Move right");

        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            System.out.println("Enter || Space");

        }
    }

}