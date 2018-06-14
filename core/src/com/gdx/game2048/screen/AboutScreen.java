package com.gdx.game2048.screen;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
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
import com.gdx.game2048.manager.MyEntry;
import com.gdx.game2048.manager.ScoreManager;
import com.gdx.game2048.manager.ScreenManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AboutScreen extends AbstractScreen {
    //Tag for debug
    private static final String TAG = AboutScreen.class.getSimpleName();

    //Step 1: create Skin, TextureAtlas, Button
    //Button

    private Skin gameSkin;
    private TextureAtlas gameAtlas;
    private Image image;

    //Text
    FreeTypeFontGenerator normalFontGenerator;

    FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;
    BitmapFont normalTextFont;
    BitmapFont scoreTextFont;
    TextButton.TextButtonStyle normalTextButtonStyle;
    TextButton.TextButtonStyle scoreTextButtonStyle;


    private LinkedList<TextButton> lines = new LinkedList<>();

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
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        //Step 2: TextureAtlas with atlas path
        gameAtlas = new TextureAtlas("themes/about.atlas");

        //Step 3: create skin
        gameSkin = new Skin(gameAtlas);

        //setup button
        createView();

        //Step 6: add to stage
        this.addActor(image);

        for (TextButton line : lines) {
            this.addActor(line);
        }


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

        // Step 7 : set position
        float rootLine = height* 0.1f;
        float linePadding = height* 0.05f;

        image.setPosition(screenMidX , screenMidY*1.15f, Align.center);

        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setPosition(screenMidX, rootLine - linePadding*i, Align.center);
        }

    }


    public void resyncTime() {
        lastFPSTime = System.currentTimeMillis();
    }

    private void createView() {
        //Step 4: create view
        image = new Image(gameSkin.getDrawable("about"));

        fontParameter.size = Gdx.graphics.getWidth() / 14;
        fontParameter.color = Color.valueOf("#efb75d");
        normalTextFont = normalFontGenerator.generateFont(fontParameter);

        normalTextButtonStyle = new TextButton.TextButtonStyle();
        normalTextButtonStyle.font = normalTextFont;

        fontParameter.size = Gdx.graphics.getWidth() / 11;
        fontParameter.borderColor = Color.valueOf("#855d4f");
        fontParameter.borderWidth = 1;
        BitmapFont selectedTextFont = normalFontGenerator.generateFont(fontParameter);
        TextButton.TextButtonStyle selectedTextButtonStyle = new TextButton.TextButtonStyle();
        selectedTextButtonStyle.font = selectedTextFont;

        // Lines text
        TextButton backText = new TextButton("BACK", selectedTextButtonStyle);
        backText.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                ScreenManager.getInstance().showScreen(ScreenEnum.MAIN_MENU);
            }
        });
        lines.add(backText);
    }

    private void handleInput(){
        //Don't handle input while there is an active animation

        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            System.out.println("Enter || Space");
            ScreenManager.getInstance().showScreen(ScreenEnum.SETTING);

        }
    }

}