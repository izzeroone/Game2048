package com.gdx.game2048.logic;

//Our game


import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.async.AsyncTask;
import com.gdx.game2048.model.animation.AnimationCell;
import com.gdx.game2048.model.data.Tile;
import com.gdx.game2048.model.animation.AnimationGrid;
import com.gdx.game2048.model.animation.AnimationType;
import com.gdx.game2048.model.data.Cell;
import com.gdx.game2048.model.data.GameState;
import com.gdx.game2048.model.data.Grid;
import com.gdx.game2048.screen.GameScreen;

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
    public void move(int direction){

//        SoundPoolManager.getInstance().playSound(R.raw.step);
        //cancel all animation
//        animationGrid.cancelAnimations();
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
            //some cell has moved
            //save Undostate and check for Win Lose
            grid.playerTurn = false;
            saveUndoState();
            runAsyn();
            //Update score
            score = grid.score;
            checkWin();
            checkLose();
            // computer move() -- > playerTurn = false
        }

        mGameScreen.resyncTime();
    }

    private AsyncExecutor asyncExecutor = new AsyncExecutor(10);

    private AsyncResult<Void> task;

    private void runAsyn() {
        //create our async task that runs our async method
        task = asyncExecutor.submit(new AsyncTask<Void>() {
            public Void call() {
                computerMove();
                return null;
            }
        });
    }

    private OnCompleteMoveListener listener;

    interface OnCompleteMoveListener{
        public void OnCompleteMove();
    }

    public void computerMove(){
        addRandomTile();
        grid.playerTurn = true;
        listener.OnCompleteMove();
    }

    private void checkLose() {
        if(false){
            gameState = GameState.LOST;
            endGame();
        }
    }

    private  void checkWin(){
        if(isWin()){
            gameState = GameState.WIN;
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

    public void autoPlay() {
        GameAI gameAI = new GameAI(this.grid);
        listener = new OnCompleteMoveListener() {
            @Override
            public void OnCompleteMove() {
                SearchResult best = gameAI.getBest();
                System.out.printf("Eval : %f \n", gameAI.eval());
                GameLogic.this.move(best.getMove());
            }
        };

        listener.OnCompleteMove();
    }




}
