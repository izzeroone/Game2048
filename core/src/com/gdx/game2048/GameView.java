package com.gdx.game2048;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gdx.game2048.CellColor.CellsColor;
import com.gdx.game2048.Shape.CustomShapeRender;
import javafx.util.Pair;

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
    private ArrayList<Pair<Rectangle, Integer>> cellsText = new ArrayList<Pair<Rectangle, Integer>>();
    private CustomShapeRender shapeRender; // draw cell
    private int cellSize;
    private float cellTextSize;
    private int cellPadding;
    private CellsColor cellsColor = new CellsColor();

    //Button
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
    public float instructionTextSize;
    public float subInstructionTextSize;
    public int sYInstruction;
    public int sYSubInstruction;
    public int eYAll;
    public int sYScore;
    public BitmapFont font;
    public FreeTypeFontGenerator fontGenerator;
    public FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;


    //TODO: Hash map font bitmap font render

    //Texture
    private Texture icActionRefresh;
    private Texture icActionUndo;
    private Texture icActioHome;
    private Texture backgroundRectangle;
    private Texture lightUpRectangle;
    private Texture background;

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
        try {
            mainTheme = Gdx.audio.newMusic(Gdx.files.internal("music/maintheme.mp3"));
            shapeRender = new CustomShapeRender();
            fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ClearSans-Bold.ttf"));
            fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            fontParameter.size = 28;
            font = new BitmapFont(Gdx.files.internal("bitmapfont/Amble-Regular-26.fnt"));
            font = fontGenerator.generateFont(fontParameter);
            font.setColor(Color.WHITE);

        } catch (Exception e) {

        }

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
        shapeRender.begin(ShapeRenderer.ShapeType.Filled);
        shapeRender.setColor(0, 1, 0, 1);
        drawCells();
        shapeRender.end();
        batch.end();

        batch.begin();
        Rectangle rect;
        for (Pair<Rectangle, Integer> pair : cellsText) {
            rect = pair.getKey();
            font.draw(batch, pair.getValue().toString(), rect.x, rect.y);
        }
        cellsText.clear();
        batch.end();

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

    private void drawCell(int left, int bottom, int right, int top, int value) {
        shapeRender.setColor(cellsColor.getColor(value));
        shapeRender.circle((left + right) / 2, (bottom + top) / 2, (right - left) / 2) ;
        //shapeRender.roundedRect(left, bottom, right - left, top - bottom, (right - left) / 5);
        cellsText.add(new Pair<Rectangle, Integer>(new Rectangle((int)(left + font.getLineHeight() / 2), (int) (top -  font.getLineHeight() / 2)  , 0, 0), value));
    }

    private void drawCell(float left, float bottom, float right, float top, int value) {
        shapeRender.setColor(cellsColor.getColor(value));
        shapeRender.circle((left + right) / 2, (bottom + top) / 2, (right - left) / 2) ;
        //shapeRender.roundedRect(left, bottom, right - left, top - bottom    , (right - left) / 5);
        cellsText.add(new Pair<Rectangle, Integer>(new Rectangle((int)(left + font.getLineHeight() / 2), (int)(top - font.getLineHeight() / 2), 0 ,0), value));

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

        cellTextSize = cellSize * 1.3f;
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