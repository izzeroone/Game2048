package com.gdx.game2048.logic;

import com.gdx.game2048.model.data.Grid;

class SearchResult{
    private int direction;
    private double score;
    private int position;
    private int cutoffs;


    public SearchResult() {

    }
    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCutoffs() {
        return cutoffs;
    }

    public void setCutoffs(int cutoffs) {
        this.cutoffs = cutoffs;
    }
}
public class GameAI {
    private static final int MIN_SEARCH_TIME = 100;
    private Grid field;
    public GameAI() {

    }

    public GameAI(Grid gameGrid) {
        this.field = gameGrid;
    }

    public double eval(){
        int emptyCells = this.field.getAvailableCells().size();

        double smoothWeight = 0.1,
                //monoWeight   = 0.0,
                //islandWeight = 0.0,
                mono2Weight  = 1.0,
                emptyWeight  = 2.7,
                maxWeight    = 1.0;

        return this.field.smoothness() * smoothWeight
                //+ this.grid.monotonicity() * monoWeight
                //- this.grid.islands() * islandWeight
                + this.field.monotonicity() * mono2Weight
                + Math.log(emptyCells) * emptyWeight
                + this.field.maxValue() * maxWeight;
    }

    public SearchResult search(int depth, double alpha, double beta, int positions, int cutoffs) {
        double bestScore;
        int bestMove = -1;
        SearchResult result = new SearchResult();

        //If player turn
        bestScore = alpha;
        for (int direction = 0; direction < 4; direction++) {
            Grid newGrid = this.field.clone();
            //Neu co di chuyen
            if (newGrid.move(direction)) {
                System.out.println(translate[direction]);
                newGrid.printCurrentField();
                positions++;
                if (newGrid.isWin()) {
                    result.setDirection(direction);
                    result.setScore(10000);
                    result.setPosition(positions);
                    result.setCutoffs(cutoffs);
                    return result;
                }

                GameAI newAi = new GameAI(this.field.clone());
                if (depth == 0) {
                    result.setDirection(direction);
                    result.setScore(newAi.eval());
                    return result;
                }
                else {
                    result = newAi.search(depth-1, bestScore, beta, positions, cutoffs);
                    if (result.getScore() > 9900) { // win
                        result.setScore(result.getScore() - 1); // to slightly penalize higher depth from win
                    }

                    positions = result.getPosition();
                    cutoffs = result.getCutoffs();
                }

            }

            if (result.getScore() > bestScore) {
                bestScore = result.getScore();
                bestMove = direction;
            }
            if (bestScore > beta) {
                cutoffs++;
                result.setDirection(bestMove);
                result.setScore(beta);
                result.setPosition(positions);
                result.setCutoffs(cutoffs);
            }
        }

        result.setDirection(bestMove);
        result.setScore(bestScore);
        result.setPosition(positions);
        result.setCutoffs(cutoffs);
        return result;
    }

    public SearchResult getBest(){
        return this.iteratorDeep();
    }

    public SearchResult iteratorDeep(){

        long start = System.currentTimeMillis();
        int depth = 0;
        SearchResult best = new SearchResult();
        do {
            SearchResult newBest = this.search(depth, -10000, 10000, 0 ,0);
            if (newBest.getDirection() == -1) {
                break;
            } else {
                best = newBest;
            }
            depth++;
        } while ( System.currentTimeMillis() - start < MIN_SEARCH_TIME);
        return best;
   }

   public String[] translate = new String[]{"Up", "Down", "Left", "Right"};

}
