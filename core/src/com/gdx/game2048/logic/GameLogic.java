package com.gdx.game2048.logic;

//Our game


import com.gdx.game2048.model.data.Tile;
import com.gdx.game2048.model.animation.AnimationGrid;
import com.gdx.game2048.model.animation.AnimationType;
import com.gdx.game2048.model.data.Cell;
import com.gdx.game2048.model.data.GameState;
import com.gdx.game2048.model.data.Grid;
import com.gdx.game2048.screen.GameScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLogic {
    //timer and its update
    private static final long MOVE_ANIMATION_TIME = GameScreen.BASE_ANIMATION_TIME;
    private static final long SPAWN_ANIMATION_TIME = GameScreen.BASE_ANIMATION_TIME;
    private static final long NOTIFICATION_DELAY_TIME = MOVE_ANIMATION_TIME + SPAWN_ANIMATION_TIME;
    private static final long NOTIFICATION_ANIMATION_TIME = GameScreen.BASE_ANIMATION_TIME * 5;
    private static final String HIGH_SCORE = "high score";
    //Maximum number of mive to make winning state
    public static long timer = 0;
    public int numCellX = 4;
    public int numCellY = 4;
    private GameScreen mGameScreen;

    public GameState gameState = GameState.NORMAL;
    public GameState lastGameState = GameState.NORMAL;
    public GameState bufferGameState = GameState.NORMAL;
    public Grid grid = null;
    public AnimationGrid animationGrid;
    public boolean canUndo;
    public long score = 0;
    public long lastScore = 0;
    public float percent = 0;
    private long bufferScore;
    private long startTime = 0;
    public static int maxTime = 60000;

    public GameLogic() {
    }

    public GameLogic(GameScreen screen){
        mGameScreen = screen;
        //avoid game over on the beginning
        startTime = System.currentTimeMillis();
        timer = 0;
    }

    public GameLogic(int numCellX, int numCellY, GameScreen mGameScreen) {
        this.numCellX = numCellX;
        this.numCellY = numCellY;
        this.mGameScreen = mGameScreen;
    }

    public void setSize(int numCellXX, int numCellYY, int time){
        numCellX = numCellXX;
        numCellY = numCellYY;
        maxTime = time;
    }

    public void newGame(){
        if(grid == null){
            //create new gird
            grid = new Grid(numCellX, numCellY);
        } else{
            //we already have our grid so save them
            //prepareUndoState();
            //saveUndoState();
            grid.clearGrid();
        }


        //create animation grid
        animationGrid = new AnimationGrid(numCellX, numCellY);
        //set up score
        score = 0;
        //add start title
        addStartTiles();
        //show the winGrid
        gameState = GameState.READY;
        //reset time
//        if (GameActivity.timerRunnable != null)
//            GameActivity.timerRunnable.onPause();
        //cancel all animation and add spawn animation
        animationGrid.cancelAnimations();
        spawnGridAnimation();
        percent = 0;
        mGameScreen.refreshLastTime = true;
        mGameScreen.resyncTime();
    }

    public void gameStart(){
        if(gameState == GameState.READY){
            //revert back to start gird
            gameState = GameState.NORMAL;
            //clear undo grid a avoid error
            grid.clearUndoGrid();
            canUndo = false;
            //reset score
            score = 0;
            //reset time
            timer = 0;
            //starting counting time
            startTime = System.currentTimeMillis();
            percent = 0;
            //GameActivity.timerRunnable.onResume();
            //add spawn animation to all cell
            animationGrid.cancelAnimations();
            spawnGridAnimation();
            //get the hint
            //play sound
            //refresh view
            mGameScreen.refreshLastTime = true;
            mGameScreen.resyncTime();

        } else{
            //the game already start, make same notification
        }
    }

    public void update() {
        if(gameState != GameState.NORMAL)
            return;

        timer = System.currentTimeMillis() - startTime;
        percent = 1.0f * timer / maxTime;
        if (timer > maxTime) {
            gameState = GameState.LOST;
//            MediaPlayerManager.getInstance().pause();
//            SoundPoolManager.getInstance().playSound(R.raw.you_lost);
//            MediaPlayerManager.getInstance().resume();
            endGame();
        }

    }

    private void addStartTiles(){
        for(int xx = 0; xx < numCellX; xx++){
            //make random cell emply
            int ignoreCellY = (int)(Math.random() * numCellY);
            for(int yy = 0; yy < numCellY; yy++){
                if(yy != ignoreCellY){
                    //add tile to cell
                    addTile(xx,yy);
                }
            }
        }
    }

    private void addRandomTile(){
        if (grid.isCellsAvailable()) {
            Cell cell = grid.randomAvailableCell();
            addTile(cell.getX(), cell.getY());
        }
    }

    private void addTile(int x, int y)
    {
        //ratio 0,7 for 1. 0,25 for 2, 0,05 for 3
        //check whether the cell is null
        if(grid.field[x][y] == null){
            int value = Math.random() <= 0.7 ? 1 : Math.random() <= 0.83 ? 2 : 3;
            Tile tile = new Tile(new Cell(x, y), value);
            spawnTile(tile);
        }
    }

    private void spawnTile(Tile tile){
        //insert to grid
        grid.insertTile(tile);
        //add animation
        animationGrid.startAnimation(tile.getX(), tile.getY(), AnimationType.SPAWN,
                SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);
    }

    private void spawnGridAnimation(){
        for (int xx = 0; xx < grid.field.length; xx++) {
            for (int yy = 0; yy < grid.field[0].length; yy++) {
                if(grid.field[xx][yy] != null){
                    animationGrid.startAnimation(xx, yy, AnimationType.SPAWN,
                            SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null); //Direction: -1 = EXPANDING
                }
            }
        }
    }

    private void clearMergedFrom(){
        //clear merge from to ready to merge
        for(Tile[] array : grid.field){
            for(Tile tile : array){
                //check whether tile null to avoid exception
                if(grid.isCellOccupied(tile)){
                    tile.setMergedFrom(null);
                }
            }
        }
    }

    private void moveTile(Tile tile, Cell cell){
        //move tile to another cell
        grid.field[tile.getX()][tile.getY()] = null;
        grid.field[cell.getX()][cell.getY()] = tile;
        tile.updatePosition(cell);
    }

    private void prepareUndoState() {
        grid.prepareSaveTiles();
        bufferScore = score;
        bufferGameState = gameState;
    }

    private void saveUndoState() {
        grid.saveTiles();
        canUndo = true;
        lastScore = bufferScore;
        lastGameState = bufferGameState;
    }

    public void revertUndoState(){
//        SoundPoolManager.getInstance().playSound(R.raw.undo);
        if(!isActive()){
            return;
        }
        if(canUndo){
            canUndo = false;
            animationGrid.cancelAnimations();
            grid.revertTiles();
            score = lastScore;
            gameState = lastGameState;
            mGameScreen.refreshLastTime = true;
        }
    }

    public  boolean isActive() {
        return !(gameState == GameState.WIN || gameState == GameState.LOST || gameState == GameState.READY);
    }

    //moving to direction all cell
    public void move(int direction){

//        SoundPoolManager.getInstance().playSound(R.raw.step);
        //cancel all animation
        animationGrid.cancelAnimations();
        if(!isActive()){
            return;
        }
        //save current grid to buffer
        prepareUndoState();

        //make travel loop varible
        Cell vector = getMovingVector(direction);
        List<Integer> travelX = makeTravelCellX(vector);
        List<Integer> travelY = makeTravelCellY(vector);

        boolean moved = false;
        //clear merge from
        clearMergedFrom();
        //loop all the cell in grid
        for(int xx : travelX){
            for(int yy : travelY){
                        if(moveAndCheck(xx, yy, direction) == true){
                            moved = true;
                        }
            }
        }

        if(moved){
            //some cell has moved
            //save Undostate and check for Win Lose
            saveUndoState();
            addRandomTile();
            checkWin();
            checkLose();
        }

        mGameScreen.resyncTime();
//        mGameView.invalidate();

    }

    //move spectific cell
    public void move(int xx, int yy, int direction){
//        mGameView.nextHint();
//        SoundPoolManager.getInstance().playSound(R.raw.step);
        animationGrid.cancelAnimations();
        // 0: up, 1: right, 2: down, 3: left
        if (!isActive()) {
            return;
        }
        prepareUndoState();
        clearMergedFrom();
        boolean moved = moveAndCheck(xx, yy, direction);


        if (moved) {
            saveUndoState();
            addRandomTile();
            checkWin();
            checkLose();
        }
        mGameScreen.resyncTime();
//        mGameView.invalidate();
    }

    private boolean moveAndCheck(int xx, int yy, int direction){
        boolean moved = false;
        //the the moving vector
        Cell vector = getMovingVector(direction);
        //get the content the current cell
        Cell cell = new Cell(xx, yy);
        Tile tile = grid.getCellContent(cell);
        //check whether the current tile is empty or not
        if(tile != null){
            //find the farthest cell in the direction
            Cell[] positions = findFarthestPosition(cell, vector);
            //get the second cell because the first one is itself
            Tile next = grid.getCellContent(positions[1]);
            //whether they have the same value and not merge with other cell
            if(next != null && next.getValue() == tile.getValue() && next.getMergedFrom() == null){
                //they have the same value
                //increment their value by 1
                Tile merge = new Tile(positions[1], tile.getValue() + 1);
                //set the 2 cells are merged
                Tile[] temp = {tile, next};
                merge.setMergedFrom(temp);
                //remove the first one (or moving cell) and insert the merge cell
                grid.insertTile(merge);
                grid.removeTile(tile);
                tile.updatePosition(positions[1]);
                //add moving and merge animation
                int[] extras = {xx, yy}; // the cell moving to
                animationGrid.startAnimation(merge.getX(), merge.getY(), AnimationType.MOVE,
                        MOVE_ANIMATION_TIME, 0, extras);
                //merge animation after the move animation complete
                animationGrid.startAnimation(merge.getX(), merge.getY(), AnimationType.MERGE,
                        SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);
                //update the score
                score += merge.getValue();
            } else {
                //just move the cell
                moveTile(tile, positions[0]);
                int[] extras = {xx, yy, 0};
                animationGrid.startAnimation(positions[0].getX(), positions[0].getY(), AnimationType.MOVE,
                        MOVE_ANIMATION_TIME, 0, extras);
            }

            if(!positionsEqual(cell, tile)){
                //same cell have move
                moved = true;
            }
        }
        return  moved;
    }

    private Cell getMovingVector(int direction){
        Cell[] map = {
                new Cell(0, -1), // up
                new Cell(1, 0),  // right
                new Cell(0, 1),  // down
                new Cell(-1, 0)  // left
        };
        return map[direction];
    }

    private List<Integer> makeTravelCellX(Cell vector) {
        List<Integer> traversals = new ArrayList<Integer>();

        for (int xx = 0; xx < numCellX; xx++) {
            traversals.add(xx);
        }
        if (vector.getX() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private List<Integer> makeTravelCellY(Cell vector) {
        List<Integer> traversals = new ArrayList<Integer>();

        for (int xx = 0; xx < numCellY; xx++) {
            traversals.add(xx);
        }
        if (vector.getY() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private Cell[] findFarthestPosition(Cell cell, Cell vector) {
        Cell previous;
        Cell nextCell = new Cell(cell.getX(), cell.getY());
        do {
            previous = nextCell;
            nextCell = new Cell(previous.getX() + vector.getX(),
                    previous.getY() + vector.getY());
        } while (grid.isCellWithinBounds(nextCell) && grid.isCellAvailable(nextCell));

        return new Cell[]{previous, nextCell};
    }


    private boolean positionsEqual(Cell first, Cell second) {
        return first.getX() == second.getX() && first.getY() == second.getY();
    }

    private void checkLose() {
        if(false){
            gameState = GameState.LOST;
//            MediaPlayerManager.getInstance().pause();
//            SoundPoolManager.getInstance().playSound(R.raw.you_lost);
//            MediaPlayerManager.getInstance().resume();
            endGame();
        }
    }

    private  void checkWin(){
        if(isWin()){
            gameState = GameState.WIN;
//            MediaPlayerManager.getInstance().pause();
//            SoundPoolManager.getInstance().playSound(R.raw.you_win);
//            MediaPlayerManager.getInstance().resume();
            endGame();
        }
    }

    private boolean isWin() {
        return false;
    }



    private void endGame() {
        //GameActivity.timerRunnable.onPause();
        animationGrid.startAnimation(-1, -1, AnimationType.FADE_GLOBAL, NOTIFICATION_ANIMATION_TIME, NOTIFICATION_DELAY_TIME, null);
    }




}
