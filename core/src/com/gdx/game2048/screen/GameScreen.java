package com.gdx.game2048.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gdx.game2048.logic.GameLogic;
import com.gdx.game2048.model.animation.AnimationCell;
import com.gdx.game2048.model.animation.AnimationType;
import com.gdx.game2048.model.data.Tile;

import java.util.ArrayList;

public class GameScreen extends AbstractScreen {
    //Game logic
    GameLogic game;

    //Tag for debug
    private static final String TAG = GameScreen.class.getSimpleName();

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

    //Step 1: create Skin, TextureAtlas, Button

    //Button
    public Skin gameSkin;
    public TextureAtlas gameAtlas;
    public Button homeButton;
    public Button restartButton;
    public Button backButton;
    public Button startButton;
    public boolean restartButtonEnabled = false;
    public int iconSize;
    public int iconPaddingSize;

    //Score
    public Rectangle scoreRect = new Rectangle();
    public BitmapFont scoreFont;
    public String gameScore;
    public TextButton scoreDisplay;
    public FreeTypeFontGenerator fontGenerator;
    public FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;


    //Timing for draw
    private long lastFPSTime = System.currentTimeMillis();
    public boolean refreshLastTime = false;

    //Music
    public Music mainTheme;

    //Batch for drawing;
    private SpriteBatch batch;

    //Threading
    Thread autoPlay;

    public GameScreen() {
        game = new GameLogic(this);
    }

    public GameScreen(int numCellX, int numCellY) {
        this.game = new GameLogic(numCellX, numCellY, this);
    }

    public GameScreen(int numCellX, int numCellY, boolean auto) {
        this.game = new GameLogic(numCellX, numCellY, this);
        final GameLogic thatGame = this.game;
        if(auto){
            autoPlay = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        thatGame.autoPlay();
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }

                }
            });
        }
    }

    @Override
    public void buildStage() {
        batch = new SpriteBatch();

        //Start game
        game.newGame();
        game.gameStart();
        if(autoPlay != null){
            autoPlay.start();
        }
        //Loading asset
        mainTheme = Gdx.audio.newMusic(Gdx.files.internal("music/maintheme.mp3"));
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ClearSans-Bold.ttf"));
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        gameAtlas = new TextureAtlas("themes/default.atlas");
        gameSkin = new Skin(gameAtlas);


        //add view to object manager
        createButton();
        this.addActor(restartButton);
        this.addActor(backButton);
        this.addActor(homeButton);

        this.addActor(scoreDisplay);


        Gdx.input.setInputProcessor(this);

        //Playing music
        mainTheme.play();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        handleInput();
        batch.begin();
        //Draw cell shape
        drawScore();
        drawCells();
        batch.end();
        //Refresh the screen if there is still an animation running
        if (game.animationGrid.isAnimationActive()) {
            update();
            //Refresh one last time on game end.
        } else if (!game.isActive() && refreshLastTime) {
            refreshLastTime = false;
        }

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.makeLayout(width, height);
    }


    private void drawScore() {
        gameScore = String.valueOf(game.score);
        scoreDisplay.setText(gameScore);
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
        iconSize = cellSize;
        iconPaddingSize = cellPadding;

        double halfNumSquaresX = game.numCellX / 2d;
        double halfNumSquaresY = game.numCellY / 2d;

        gridRect.x = (int) (screenMidX - (cellSize + cellPadding) * halfNumSquaresX - cellPadding / 2);
        gridRect.width = (int) (screenMidX + (cellSize + cellPadding) * halfNumSquaresX + cellPadding / 2 - gridRect.x);
        gridRect.y = (int) (boardMidY - (cellSize + cellPadding) * halfNumSquaresY - cellPadding / 2);
        gridRect.height = (int) (boardMidY + (cellSize + cellPadding) * halfNumSquaresY + cellPadding / 2 - gridRect.y);


        backButton.setPosition(screenMidX - iconSize * 3 / 2 - iconPaddingSize, height - iconPaddingSize, Align.left);
        backButton.setSize(iconSize, iconSize);

        homeButton.setPosition(screenMidX - iconSize / 2, height - iconPaddingSize, Align.left);
        homeButton.setSize(iconSize, iconSize);

        restartButton.setPosition(screenMidX + iconSize / 2 + iconPaddingSize, height - iconPaddingSize, Align.left);
        restartButton.setSize(iconSize, iconSize);


        fontParameter.size = Gdx.graphics.getWidth() / 5;
        fontParameter.color = Color.DARK_GRAY;
        scoreFont = fontGenerator.generateFont(fontParameter);
        scoreDisplay.setPosition(screenMidX, gridRect.y / 2, Align.center);
    }

    public void resyncTime() {
        lastFPSTime = System.currentTimeMillis();
    }

    private void createButton() {
        homeButton = new Button(gameSkin.getDrawable("ic_home"));
        restartButton = new Button(gameSkin.getDrawable("ic_restart"));
        backButton = new Button(gameSkin.getDrawable("ic_back"));
        backButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.revertUndoState();
            }
        });

        fontParameter.size = Gdx.graphics.getWidth() / 5;
        fontParameter.color = Color.DARK_GRAY;
        scoreFont = fontGenerator.generateFont(fontParameter);
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = scoreFont;
        scoreDisplay = new TextButton("0", textButtonStyle);
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
            System.out.println("Move down");
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
