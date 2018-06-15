package com.gdx.game2048.logic;

//Our game


import com.badlogic.gdx.Game;
import com.gdx.game2048.manager.MusicManager;
import com.gdx.game2048.model.animation.AnimationCell;
import com.gdx.game2048.model.data.Tile;
import com.gdx.game2048.model.animation.AnimationGrid;
import com.gdx.game2048.model.animation.AnimationType;
import com.gdx.game2048.model.data.Cell;
import com.gdx.game2048.model.data.GameState;
import com.gdx.game2048.model.data.Grid;
import com.gdx.game2048.screen.GameScreen;

import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

public class GameLogic {
    //timer and its update
    public static final long MOVE_ANIMATION_TIME = GameScreen.BASE_ANIMATION_TIME;
    public static final long SPAWN_ANIMATION_TIME = GameScreen.BASE_ANIMATION_TIME;
    public static final long NOTIFICATION_DELAY_TIME = MOVE_ANIMATION_TIME + SPAWN_ANIMATION_TIME;
    public static final long NOTIFICATION_ANIMATION_TIME = GameScreen.BASE_ANIMATION_TIME * 5;
    private static final int STARTED_CELL = 2;
    private static final String HIGH_SCORE = "high score";
    //Maximum number of mive to make winning state
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
    private long bufferScore;

    //auto play
    private boolean autoPlay = false;
    //auto play
    Thread autoPlayThread;
    //auto play style = {random or minimax}
    int autoPlayStyle;

    public void setAutoPlayStyle(int autoPlayStyle) {
        this.autoPlayStyle = autoPlayStyle;
    }

    //game input
    Thread computerThread;

    public GameLogic() {
    }

    public GameLogic(GameScreen screen){
        mGameScreen = screen;
    }

    public GameLogic(int numCellX, int numCellY, GameScreen mGameScreen) {
        this.numCellX = numCellX;
        this.numCellY = numCellY;
        this.mGameScreen = mGameScreen;
    }



    public void newGame(final boolean autoPlay){
        //Create auto play thread
        this.autoPlay = autoPlay;
        if (autoPlay) {

            autoPlayThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        while (!autoPlayThread.interrupted()) {
                            autoPlay();
                        }
                    } catch (InterruptedException e) {
                        autoPlayThread = null;
                        System.out.println("InterruptedException");
                    }
                }
            });
        }

        //Create computer play thread
        computerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (!computerThread.interrupted()) {
                    computerMove();
                }
            } catch (InterruptedException e) {
                computerThread = null;
                System.out.println("InterruptedException");
            }
        }});

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
        grid.score = 0;
        //add start title
        addStartTiles();
        //show the winGrid
        gameState = GameState.READY;
        //set up computer thread
        //cancel all animation and add spawn animation
        animationGrid.cancelAnimations();
        spawnGridAnimation();
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
            //GameActivity.timerRunnable.onResume();
            //add spawn animation to all cell
            animationGrid.cancelAnimations();
            spawnGridAnimation();
            //get the hint
            //play sound
            //refresh view
            grid.playerTurn = true;
            //Start the auto thread and computer thread
            if (this.autoPlay) {
                autoPlayThread.start();
            }
            computerThread.start();
            mGameScreen.refreshLastTime = true;
            mGameScreen.resyncTime();

        } else{
            //the game already start, make same notification
        }
    }


    private void addStartTiles(){
        for(int xx = 0; xx < STARTED_CELL; xx++){
            //make random cell emply
            addRandomTile();
        }
    }

    private void addRandomTile(){
        if (grid.isCellsAvailable()) {
            Cell cell = grid.randomAvailableCell();
            grid.addTile(cell.getX(), cell.getY());
            animationGrid.startAnimation(cell.getX(), cell.getY(), AnimationType.SPAWN,
                    SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);
        }
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
    public synchronized void move(int direction){

//        SoundPoolManager.getInstance().playSound(R.raw.step);
        //cancel all animation
        animationGrid.cancelAnimations();
        if(!isActive()){
            return;
        }
        if (!grid.playerTurn){
            return;
        }

        //save current grid to buffer
        prepareUndoState();

        boolean moved = grid.move(direction, animationGrid);

        if(moved){
            //todo: music
            MusicManager.getInstance().playSound("merge_tile");
            //some cell has moved
            //save Undostate and check for Win Lose
            grid.playerTurn = false;
            saveUndoState();
            //Update score
            score = grid.score;
            checkWin();
            notify();
        }

        checkLose();
        mGameScreen.resyncTime();
    }

    public synchronized void computerMove() throws InterruptedException {
        final GameLogic self = this;
        while (!computerThread.isInterrupted()){
            while (this.grid.playerTurn || !isActive() && !computerThread.isInterrupted()){
                wait();
            };
            addRandomTile();
            Thread.sleep(GameScreen.BASE_ANIMATION_TIME);

            self.grid.playerTurn = true;
            notify();
        }
    }

    private void checkLose() {
        if(!grid.movesAvailable()){
            gameState = GameState.LOST;
            endGame();
        }
    }

    private  void checkWin(){
        if(grid.isWin()){
            gameState = GameState.WIN;
            endGame();
        }
    }




    public void endGame() {
        //GameActivity.timerRunnable.onPause();
        animationGrid.startAnimation(-1, -1, AnimationType.FADE_GLOBAL, NOTIFICATION_ANIMATION_TIME, NOTIFICATION_DELAY_TIME, null);
        //Stop auto thread and computer thread
        if(autoPlay){
            if (autoPlayThread.isAlive())
                autoPlayThread.interrupt();
        }
        if (computerThread.isAlive())
            computerThread.interrupt();
    }

    public synchronized void autoPlay() throws InterruptedException {
        GameAI gameAI = new GameAI(this.grid);
        while (!autoPlayThread.isInterrupted()){
            SearchResult best = gameAI.getBest();

            while (!grid.playerTurn || !isActive() && !autoPlayThread.isInterrupted()){
                synchronized (this) {
                    this.wait();
                }
            }
            if(autoPlayStyle == 1){
                int randomDirection = ThreadLocalRandom.current().nextInt(0, 3 + 1);
                this.move(randomDirection);
            } else {
                System.out.printf("Eval : %f \n", gameAI.eval());
                this.move(best.getMove());
            }

            notify();
        }


    }

    public void stopThread() {
        endGame();
        autoPlayThread.interrupt();
        computerThread.interrupt();
    }
}
