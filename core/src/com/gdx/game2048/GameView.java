package com.gdx.game2048;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.awt.*;
import java.util.ArrayList;


public class GameView extends ApplicationAdapter {
    //Game logic
    MainGame game;

    //Tag for debug
    private static final String TAG = GameView.class.getSimpleName();

    //Animation constant
    public static final int BASE_ANIMATION_TIME = 100;
    private static final float MERGING_ACCEL = -0.5f;
    private static final float INITIAL_VELO = (1 - MERGING_ACCEL) / 4;

    //Cell render
    public final int numCellTypes = 21;
    public Rectangle gridRect = new Rectangle();
    //Queue to draw cell text
    private int cellSize;
    private int cellPadding;

    //Button
    public Skin gameSkin;
    public Stage stage;
    public TextureAtlas gameAtlas;
    public Button homeButton;
    public boolean restartButtonEnabled = false;
    public int iconSize;
    public int iconPaddingSize;
    public int sYIcons;
    public int sXNewGame;
    public int sXUndo;
    public int sXHome;
    public int sYHome;

    //Text
    public int textPaddingSize;
    public int instructionTextSize;
    public int subInstructionTextSize;
    public int sYInstruction;
    public int sYSubInstruction;
    public int eYAll;
    public int sYScore;
    public FreeTypeFontGenerator fontGenerator;
    public FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;


    //Timing for draw
    private long lastFPSTime = System.currentTimeMillis();
    public boolean refreshLastTime = false;

    //Music
    public Music mainTheme;

    //Batch for drawing;
    private SpriteBatch batch;

    @Override
    public void create() {
        super.create();
        //Create new batch
        batch = new SpriteBatch();

        //Start game
        game = new MainGame(this);
        game.newGame();
        game.gameStart();

        //Loading asset
        mainTheme = Gdx.audio.newMusic(Gdx.files.internal("music/maintheme.mp3"));;
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ClearSans-Bold.ttf"));
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        gameAtlas = new TextureAtlas("themes/default.atlas");
        gameSkin = new Skin(gameAtlas);
        ImageButton.ImageButtonStyle homeButtonStyle = new ImageButton.ImageButtonStyle();
        homeButtonStyle.up = gameSkin.getDrawable("ic_home");
        homeButton = new ImageButton(homeButtonStyle);

        //Setup stage
        stage = new Stage(new ScreenViewport());
        stage.addActor(homeButton);
        homeButton.addListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.log("my app", "Pressed"); //** Usually used to start Game, etc. **//


                // TODO Auto-generated method stub



                return true;

            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.log("my app", "Rggggggeleased");

                ///and level

                dispose();

            }
        });
        Gdx.input.setInputProcessor(stage);

        //Playing music
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        makeLayout(width, height);
        //Calc pos and size
    }

    @Override
    public void render() {
        super.render();

        handleInput();
        //Reset the transparency of the screen
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        //Draw cell shape
        drawCells();
        batch.end();

        batch.begin();
        batch.end();

        stage.act();
        stage.draw();

        //Refresh the screen if there is still an animation running
        if (game.animationGrid.isAnimationActive()) {
            update();
            //Refresh one last time on game end.
        } else if (!game.isActive() && refreshLastTime) {
            refreshLastTime = false;
        }

        if(game.percent <= 1) {
            game.update();
        }

        //For now render cells only
    }


    //REF: https://stackoverflow.com/questions/24345754/shaperenderer-produces-pixelated-shapes-using-libgdx
    //Enable anti alias
    private void drawCell(int left, int bottom, int right, int top, int value) {

        gameSkin.getDrawable("cell" + value).draw(batch, left, bottom, right - left, top - bottom);

    }

    private void drawCell(float left, float bottom, float right, float top, int value) {
        drawCell((int)left, (int)bottom, (int)right, (int)top, value );
    }

    private void drawCells() {
        // Outputting the individual cells
        for (int xx = 0; xx < game.numCellX; xx++) {
            for (int yy = 0; yy < game.numCellY; yy++) {
                int sX = (int) (gridRect.x + cellPadding + (cellSize + cellPadding) * xx);
                int eX = sX + cellSize;
                int sY = (int) (gridRect.y + cellPadding + (cellSize + cellPadding) * yy);
                int eY = sY + cellSize;

                Tile currentTile = game.grid.getCellContent(xx, yy);
                if (currentTile != null) {
                    //Get and represent the value of the tile
                    int value = currentTile.getValue();
                    int index = value;

                    //Check for any active animations
                    ArrayList<AnimationCell> aArray = game.animationGrid.getAnimationCell(xx, yy);
                    boolean animated = false;
                    for (int i = aArray.size() - 1; i >= 0; i--) {
                        AnimationCell aCell = aArray.get(i);
                        //If this animation is not active, skip it
                        if (aCell.getAnimationType() == AnimationType.SPAWN) {
                            animated = true;
                        }
                        if (!aCell.isActive()) {
                            continue;
                        }
                        double percentDone;
                        float textScaleSize;
                        float cellScaleSize;
                        switch (aCell.getAnimationType()){
                            case MOVE:
                                percentDone = aCell.getPercentageDone();
                                int tempIndex = index;
                                if (aArray.size() >= 2) {
                                    tempIndex = tempIndex - 1;
                                }
                                int previousX = aCell.extras[0];
                                int previousY = aCell.extras[1];
                                int currentX = currentTile.getX();
                                int currentY = currentTile.getY();
                                int dX = (int) ((currentX - previousX) * (cellSize + cellPadding) * (percentDone - 1) * 1.0);
                                int dY = (int) ((currentY - previousY) * (cellSize + cellPadding) * (percentDone - 1) * 1.0);
                                //drawDrawable(bitmapCell[tempIndex], sX + dX, sY + dY, eX + dX, eY + dY);
                                drawCell(sX + dX, sY + dY, eX + dX, eY + dY, index);
                                break;
                            case SPAWN:
                                percentDone = aCell.getPercentageDone();
                                textScaleSize = (float) (percentDone);
                                cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                                //drawDrawable(bitmapCell[index], sX + cellScaleSize, sY +cellScaleSize, eX - cellScaleSize, eY - cellScaleSize);
                                drawCell(sX + cellScaleSize, sY + cellScaleSize, eX - cellScaleSize, eY - cellScaleSize, index);
                                break;
                            case MERGE:
                                percentDone = aCell.getPercentageDone();
                                textScaleSize = (float) (1 + INITIAL_VELO * percentDone
                                        + MERGING_ACCEL * percentDone * percentDone / 2);

                                cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                                //drawDrawable(bitmapCell[index], sX + cellScaleSize, sY +cellScaleSize, eX - cellScaleSize, eY - cellScaleSize);
                                drawCell(sX + cellScaleSize, sY +cellScaleSize, eX - cellScaleSize, eY - cellScaleSize, index);
                                break;
                            case FADE_GLOBAL:
                                //bitmapCell[index].setAlpha((int)(((aCell.getPercentageDone()) * 255) >= 255 ? 255 : (aCell.getPercentageDone()) * 255));
                                //drawDrawable(bitmapCell[index], sX, sY, eX, eY);
                                drawCell(sX, sY, eX, eY, index);
                                break;
                        }
                        animated = true;
                    }

                    //No active animations? Just draw the cell
                    if (!animated) {
                        //drawDrawable(bitmapCell[index], sX, sY, eX, eY);
                        drawCell(sX, sY, eX, eY, index);
                    }
                }
            }
        }
    }


    public void update(){
        long currentTime = System.currentTimeMillis();
        game.animationGrid.updateAll(currentTime - lastFPSTime);
        lastFPSTime = currentTime;
    }

    private void makeLayout(int width, int height) {
        cellSize = (5 * width / 9) / game.numCellY;
        cellPadding = cellSize * 2 / 3;

        int screenMidX = width / 2;
        int screenMidY = height / 2;
        int boardMidY = screenMidY + cellSize / 2;
        iconSize = (int) ((5 * width / 9) / 5 * 1.5);

        double halfNumSquaresX = game.numCellX / 2d;
        double halfNumSquaresY = game.numCellY / 2d;

        gridRect.x = (int) (screenMidX - (cellSize + cellPadding) * halfNumSquaresX - cellPadding / 2);
        gridRect.width = (int) (screenMidX + (cellSize + cellPadding) * halfNumSquaresX + cellPadding / 2 - gridRect.x);
        gridRect.y = (int) (boardMidY - (cellSize + cellPadding) * halfNumSquaresY - cellPadding / 2);
        gridRect.height = (int) (boardMidY + (cellSize + cellPadding) * halfNumSquaresY + cellPadding / 2 - gridRect.y);

    }

    public void resyncTime() {
        lastFPSTime = System.currentTimeMillis();
    }

    private void handleInput(){
        //Don't handle input while there is an active animation
        if(game.animationGrid.isAnimationActive()){
            return;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
            System.out.println("Move up");
            game.move(2);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
            System.out.println("Move ");
            game.move(0);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            System.out.println("Move left");
            game.move(3);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            System.out.println("Move right");
            game.move(1);
        }
    }



}