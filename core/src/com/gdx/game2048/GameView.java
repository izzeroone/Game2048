package com.gdx.game2048;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

//draw game
public class GameView extends ApplicationAdapter {

    //Animation constant
    static final int BASE_ANIMATION_TINE = 100;
    private static final String TAG =  GameView.class.getSimpleName();
    private static final float MERGING_ACCELERATION = -0.5f;
    private static final float INITIAL_VELOCITY = (1 - MERGING_ACCELERATION) / 4;
    //number of diffirent type of celll
    public final int numCellTypes = 21;
    //out game xD
    public MainGame game;
    //Internal variables
    //Drawing paint
    //private final Paint paint = new Paint();
    public boolean restartButtonEnabled = false;
    //Drawing location
    //Grid location
    public Rectangle gridRect = new Rectangle();
    //icon size for new game and undo button
    public int iconSize;
    //restart and undo button base on the same lin Y
    public int sYIcons;
    //new button
    public int sXNewGame;
    //undo button
    public int sXUndo;
    //check button
    public Rectangle checkRect = new Rectangle();
    //Home button
    public int sXHome;
    public int sYHome;
    //timer
    public Rectangle percentRect = new Rectangle();
    //Misc
    boolean refreshLastTime = true;
    //Bit map of the cell
    //private BitmapDrawable[] bitmapCell = new BitmapDrawable[numCellTypes];
    private Texture[] bitmapCell = new Texture[numCellTypes];
    //layout
    private int cellSize;
    private float textSize;
    private float cellTextSize;
    private int gridWidth;
    private int textPaddingSize;
    private int iconPaddingSize;
    //text size;
    private float instructionTextSize;
    private float subInstructionTextSize;
    //Asset
    private Texture icActionRefresh;
    private Texture icActionUndo;
    private Texture icActionHome;
    private Texture backgroundRectangle;
    private Texture lightUpRectangle;
    private Texture iconLeftRectangle;
    private Texture iconLeftRectangleDisable;
    private Texture iconRightRectangle;
    private Texture iconRectangle;
    private Texture background = null;
    //text position
    private int sYInstruction;
    private int sYSubInstruction;
    private int eYAll;
    private int sYScore;
    //string
    private String instruction;
    private String subInstruction;
    //Timing
    private long lastFPSTime = System.currentTimeMillis();
    //Batch for draw
    SpriteBatch batch;
    //Music
    private Music mainTheme;

    public GameView() {

    }

    @Override
    public void create() {
        super.create();

        //Resources resources = context.getResources();
        //Loading resources
        batch = new SpriteBatch();
        game = new MainGame(this);
        game.newGame();
        game.gameStart();
        try {
            bitmapCell[1] = new Texture("circle1.png");
            bitmapCell[2] = new Texture("circle2.png");
            bitmapCell[3] = new Texture("circle3.png");
            bitmapCell[4] = new Texture("circle4.png");
            bitmapCell[5] = new Texture("circle5.png");
            bitmapCell[6] = new Texture("circle6.png");
            bitmapCell[7] = new Texture("circle7.png");
            bitmapCell[8] = new Texture("circle8.png");
            bitmapCell[9] = new Texture("circle9.png");
            mainTheme = Gdx.audio.newMusic(Gdx.files.internal("music/maintheme.mp3"));

        } catch (Exception e) {

        }

        //play music
        mainTheme.setLooping(true);
        mainTheme.play();

    }

    @Override
    public void resize(int width, int height) {
        //super.resize(width, height);
        makeLayout(width, height);
    }

    @Override
    public void render() {
        super.render();
        //Reset the transparency of the screen
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        handleInput();
        batch.begin();

//        drawNewGameButton(false);
//        drawHomeButton();
//
//        drawUndoButton();
//        drawScoreText();
//        drawTimePercent();
//
//        if (!game.isActive() && !game.animationGrid.isAnimationActive()) {
//            drawNewGameButton(true);
//        }


        drawCells();


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


    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void dispose() {
        super.dispose();
    }





    private void drawDrawable(Texture img, int left, int top, int right, int bottom) {
        //convert top-left to bottom-left;
        int ctop = Gdx.graphics.getHeight() - top;
        //Draw to canvas with bound
        batch.draw(img, left, ctop, right -left, bottom - top);
    }

    private void drawDrawable(Texture img, float left, float top, float right, float bottom) {
        //Draw to canvas with bound
        int ctop = (int) (Gdx.graphics.getHeight() - top);
        batch.draw(img, left, ctop, right -left, bottom - top);
    }

    private void drawDrawable(Texture img, Rectangle rect) {
        //Draw to canvas with bound
        int ctop = (int) (Gdx.graphics.getHeight() - rect.y);
        batch.draw(img, rect.x, ctop, rect.width, rect.height);
    }

    private void drawCellText(int value) {
        //Draw the text number inside the cell

    }
    private void drawScoreText() {
        //Drawing the score text: Ver 2
//        paint.setTextAlign(Paint.Align.CENTER);
//        paint.setColor(getResources().getColor(R.color.text_black));
//        paint.setTextSize(instructionTextSize / 1.5f);
//        canvas.drawText(String.valueOf(game.score), (gridRect.left + gridRect.right) / 2, sYScore + paint.getTextSize(), paint);
    }

    private void drawTimePercent() {
        int percentRight = (int) (percentRect.x + game.percent * percentRect.getWidth());

        drawDrawable(backgroundRectangle, percentRect);
        //Outputting scores box
        drawDrawable(lightUpRectangle, percentRect.x, percentRect.y, percentRight, percentRect.y + percentRect.height);

    }


    private void drawNewGameButton(boolean lightUp) {

        if (lightUp) {
            drawDrawable(lightUpRectangle,
                    sXNewGame,
                    sYIcons,
                    sXNewGame + iconSize,
                    sYIcons + iconSize
            );
        } else {
            drawDrawable(
                    iconRectangle,
                    sXNewGame,
                    sYIcons, sXNewGame + iconSize,
                    sYIcons + iconSize
            );
        }

        drawDrawable(icActionRefresh,
                sXNewGame + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXNewGame + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize
        );
    }

    private void drawUndoButton() {

        if(game.canUndo){
            drawDrawable(
                    iconLeftRectangle,
                    sXUndo,
                    sYIcons, sXUndo + (int) (iconSize * 1.2),
                    sYIcons + iconSize
            );
        } else{
            drawDrawable(
                    iconLeftRectangleDisable,
                    sXUndo,
                    sYIcons, sXUndo + (int) (iconSize * 1.2),
                    sYIcons + iconSize);
        }



        drawDrawable(icActionUndo,
                sXUndo + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXUndo + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize
        );
    }

    private void drawHomeButton() {


        drawDrawable(icActionHome,
                sXHome + iconPaddingSize / 2,
                sYHome + iconPaddingSize / 2,
                sXHome + iconSize - iconPaddingSize / 2,
                sYHome + iconSize - iconPaddingSize / 2
        );
    }





    private void drawCells() {
        // Outputting the individual cells
        for (int xx = 0; xx < game.numCellX; xx++) {
            for (int yy = 0; yy < game.numCellY; yy++) {
                int sX = (int) (gridRect.x + gridWidth + (cellSize + gridWidth) * xx);
                int eX = sX + cellSize;
                int sY = (int) (gridRect.y + gridWidth + (cellSize + gridWidth) * yy);
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
                        int sweepAngle;
                        int startAngle;
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
                                int dX = (int) ((currentX - previousX) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                                int dY = (int) ((currentY - previousY) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                                drawDrawable(bitmapCell[tempIndex], sX + dX, sY + dY, eX + dX, eY + dY);
                                break;
                            case SPAWN:
                                percentDone = aCell.getPercentageDone();
                                textScaleSize = (float) (percentDone);

                                cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                                drawDrawable(bitmapCell[index], sX + cellScaleSize, sY +cellScaleSize, eX - cellScaleSize, eY - cellScaleSize);
                                break;
                            case MERGE:
                                percentDone = aCell.getPercentageDone();
                                textScaleSize = (float) (1 + INITIAL_VELOCITY * percentDone
                                        + MERGING_ACCELERATION * percentDone * percentDone / 2);

                                cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                                drawDrawable(bitmapCell[index], sX + cellScaleSize, sY +cellScaleSize, eX - cellScaleSize, eY - cellScaleSize);
                                break;
                            case FADE_GLOBAL:
                                //bitmapCell[index].setAlpha((int)(((aCell.getPercentageDone()) * 255) >= 255 ? 255 : (aCell.getPercentageDone()) * 255));
                                drawDrawable(bitmapCell[index], sX, sY, eX, eY);
                                break;
                        }
                        animated = true;
                    }

                    //No active animations? Just draw the cell
                    if (!animated) {
                        drawDrawable(bitmapCell[index], sX, sY, eX, eY);
                    }
                }
            }
        }
    }





    private void update() {
        long currentTime = System.currentTimeMillis();
        game.animationGrid.updateAll(currentTime - lastFPSTime);
        lastFPSTime = currentTime;
    }

    private void makeLayout(int width, int height){
        int staticCellSize = (5 * width / 9) / 4;
        cellSize = (5 * width / 9) / game.numCellY;
        //padding width
        gridWidth = cellSize * 2 / 3;
        int screenMidX = width / 2;
        int screenMidY = height / 2;
        int boardMidY = screenMidY + cellSize/2;
        iconSize = (int) ((5 * width / 9) / 5 * 1.5);

        //Grid dimension
        double halfNumSquaresX = game.numCellX / 2d;
        double halfNumSquaresY = game.numCellY / 2d;

        gridRect.x = (int) (screenMidX - (cellSize + gridWidth) * halfNumSquaresX - gridWidth / 2);
        gridRect.width = (int) (screenMidX + (cellSize + gridWidth) * halfNumSquaresX + gridWidth / 2) - gridRect.x;

        gridRect.y = (int) (boardMidY - (cellSize + gridWidth) * halfNumSquaresY - gridWidth / 2);
        gridRect.height = (int) (boardMidY + (cellSize + gridWidth) * halfNumSquaresY + gridWidth / 2) - gridRect.y;


        // Text Dimensions
        textSize = cellSize;


        instructionTextSize = width / 7.f;
        subInstructionTextSize = instructionTextSize / 2.3f;

        cellTextSize = textSize * 1.3f;
        textPaddingSize = (int) (subInstructionTextSize / 3);
        iconPaddingSize = iconSize / 4;


        //static variables
        sYInstruction = (int) (gridRect.x - staticCellSize * 1.6);
        sYSubInstruction = (int) (gridRect.x - staticCellSize * 0.5);
        sYScore = sXHome + iconPaddingSize / 2;

        //sYIcons = gridRect.bottom + cellSize / 2;
        sYIcons = height - iconSize - 10;
        sXNewGame = screenMidX - iconSize / 2;
        sXUndo = 0;
        sXHome = 0;
        sYHome = 0;

        percentRect.x = gridRect.x;
        percentRect.width = gridRect.width - gridRect.x;
        percentRect.y = gridRect.y + textPaddingSize;
        percentRect.height =textPaddingSize;

        resyncTime();
    }


    public void resyncTime() {
        lastFPSTime = System.currentTimeMillis();
    }

    public int[] clickedCell(int x, int y) {
        // Outputting the game grid
        int[] position = new int[2];
        for (int xx = 0; xx < game.numCellX; xx++) {
            for (int yy = 0; yy < game.numCellY; yy++) {
                int sX = (int) (gridRect.x + gridWidth + (cellSize + gridWidth) * xx);
                int eX = sX + cellSize;
                int sY = (int) (gridRect.y + gridWidth + (cellSize + gridWidth) * yy);
                int eY = sY + cellSize;
                if (x >= sX && x <= eX && y >= sY && y <= eY){
                    position[0] = xx;
                    position[1] = yy;
                    return position;
                }

            }
        }
        return null;
    }

    private void handleInput(){
        //Don't handle input while there is an active animation
        if(game.animationGrid.isAnimationActive()){
            return;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
            System.out.println("Move down");
            game.move(2);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
            System.out.println("Move up");
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