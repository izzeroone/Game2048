package com.gdx.game2048.screen;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.gdx.game2048.manager.ScoreManager;
import com.gdx.game2048.manager.ScreenManager;
import javafx.util.Pair;

import java.util.*;

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

    private Image image;

    private int iconPaddingSize;

    //Text
    FreeTypeFontGenerator normalFontGenerator;
    FreeTypeFontGenerator georiaFontGenerator;

    FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;
    BitmapFont normalTextFont;
    BitmapFont scoreTextFont;
    BitmapFont highestScoreTextFont;
    TextButton.TextButtonStyle normalTextButtonStyle;
    TextButton.TextButtonStyle scoreTextButtonStyle;
    TextButton.TextButtonStyle highestTextButtonStyle;


    private LinkedList<TextButton> lines = new LinkedList<>();
    TextButton highestScore;

    //Timing for draw
    private long lastFPSTime = System.currentTimeMillis();
    public boolean refreshLastTime = false;

    //Batch for drawing;
    private SpriteBatch batch;

    @Override
    public void buildStage() {


        batch = new SpriteBatch();

        //Loading asset
        normalFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ClearSans-Bold.ttf"));
        georiaFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/GeorgiaPro-BlackIt.ttf"));
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        //Step 2: TextureAtlas with atlas path
        gameAtlas = new TextureAtlas("themes/score.atlas");

        //Step 3: create skin
        gameSkin = new Skin(gameAtlas);

        //setup button
        createView();

        //Step 6: add to stage
        this.addActor(image);

        for (TextButton line : lines) {
            this.addActor(line);
        }
        this.addActor(highestScore);


        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        handleInput();
        //Reset the transparency of the screen

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
        iconPaddingSize = 50;

        // Step 7 : set position
        float rootLine = height* 0.6f;
        float linePadding = height* 0.05f;

        image.setPosition(width*0.5f , height*0.78f, Align.center);

        highestScore.setPosition(width*0.65f, height*0.74f, Align.center);

        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setPosition(screenMidX, rootLine - linePadding*i, Align.center);
        }

    }


    public void resyncTime() {
        lastFPSTime = System.currentTimeMillis();
    }

    private void createView() {

        //Step 4: create view
        image = new Image(gameSkin.getDrawable("high_score"));

        fontParameter.size = Gdx.graphics.getWidth() / 14;
        fontParameter.color = Color.valueOf("#efb75d");
        normalTextFont = georiaFontGenerator.generateFont(fontParameter);

        fontParameter.size = Gdx.graphics.getWidth() / 18;
        scoreTextFont = normalFontGenerator.generateFont(fontParameter);

        fontParameter.size = Gdx.graphics.getWidth() / 6;
        fontParameter.color = Color.valueOf("#FFBD0B");
        fontParameter.borderColor = Color.valueOf("#000000");
        fontParameter.borderWidth = 1;
        highestScoreTextFont = georiaFontGenerator.generateFont(fontParameter);

        normalTextButtonStyle = new TextButton.TextButtonStyle();
        normalTextButtonStyle.font = normalTextFont;

        scoreTextButtonStyle = new TextButton.TextButtonStyle();
        scoreTextButtonStyle.font = scoreTextFont;

        highestTextButtonStyle = new TextButton.TextButtonStyle();
        highestTextButtonStyle.font = highestScoreTextFont;

        fontParameter.size = Gdx.graphics.getWidth() / 11;
        fontParameter.borderColor = Color.valueOf("#855d4f");
        fontParameter.borderWidth = 1;
        BitmapFont selectedTextFont = normalFontGenerator.generateFont(fontParameter);
        TextButton.TextButtonStyle selectedTextButtonStyle = new TextButton.TextButtonStyle();
        selectedTextButtonStyle.font = selectedTextFont;

        ScoreManager.getInstance().addNewScore(1700);
        // Lines text
        lines.add(new TextButton("         Time          Score", normalTextButtonStyle));

        List<String> lineStrs = new LinkedList<>();
        for (Pair<String, Integer> e: ScoreManager.getInstance().getListScore()) {
            if (e.getKey() != null) {
                lineStrs.add(e.getKey()+ "             " + e.getValue());
            }
        }
//        Collections.reverse(lineStrs);
        for (String lineStr : lineStrs) {
            lines.add(new TextButton(lineStr, scoreTextButtonStyle));
        }

        lines.add(new TextButton("", normalTextButtonStyle));

        TextButton backText = new TextButton("BACK", selectedTextButtonStyle);
        backText.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                ScreenManager.getInstance().showScreen(ScreenEnum.MAIN_MENU);
            }
        });
        lines.add(backText);


        highestScore = new TextButton(ScoreManager.getInstance().getHighestScore().toString(), highestTextButtonStyle);
    }

    private void handleInput(){
        //Don't handle input while there is an active animation

        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            System.out.println("Enter || Space");
            ScreenManager.getInstance().showScreen(ScreenEnum.MAIN_MENU);

        }
    }

}