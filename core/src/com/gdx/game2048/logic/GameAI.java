package com.gdx.game2048.logic;

import com.gdx.game2048.model.data.Cell;
import com.gdx.game2048.model.data.Grid;
import com.gdx.game2048.model.data.Tile;
import com.gdx.game2048.screen.GameScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class SearchResult{
    private int move;

    public int getMove() {
        return move;
    }

    public void setMove(int move) {
        this.move = move;
    }

    private double score;
    private int position;
    private int cutoffs;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public SearchResult() {

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
    private static final int MIN_SEARCH_TIME = GameScreen.BASE_ANIMATION_TIME;
    private Grid field;
    public GameAI() {

    }

    public GameAI(Grid gameGrid) {
        this.field = gameGrid;
    }

    public double eval(){
        int emptyCells = this.field.getAvailableCells().size();

        double smoothWeight = 0.1,
                mono2Weight  = 1.0,
                emptyWeight  = 2.7,
                maxWeight    = 1.0;
//        System.out.printf("Mono : %d \n", this.field.monotonicity());
//        System.out.printf("Smoo : %d \n", this.field.smoothness());
//        System.out.printf("Emply : %d \n", emptyCells);
//        System.out.printf("Max : %d \n", this.field.maxValue());
        return this.field.smoothness() * smoothWeight
                + this.field.monotonicity() * mono2Weight
                + Math.log(emptyCells) * emptyWeight
                + this.field.maxValue() * maxWeight;
    }

    public SearchResult search(int depth, double alpha, int beta, int positions, int cutoffs) {
        double bestScore = -10000;
        int bestMove = -1;
        SearchResult result = new SearchResult();

        //If player turn
        if(field.playerTurn){
            bestScore = alpha;
            for (int direction = 0; direction < 4; direction++) {
                Grid newGrid = this.field.clone();
                //Neu co di chuyen
                if (newGrid.move(direction)) {
                    positions++;
                    if (newGrid.isWin()) {
                        result.setMove(direction);
                        result.setScore(10000);
                        result.setPosition(positions);
                        result.setCutoffs(cutoffs);
                        return result;
                    }

                    GameAI newAi = new GameAI(this.field.clone());
                    if (depth == 0) {
                        result.setMove(direction);
                        result.setScore(newAi.eval());
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
                if (result.getScore() > bestScore || bestScore == 0.0d) {
                    bestScore = result.getScore();
                    bestMove = direction;
                }
                if (bestScore > beta) {
                    cutoffs++;
                    result.setMove(bestMove);
                    result.setScore(beta);
                    result.setPosition(positions);
                    result.setCutoffs(cutoffs);
                    return result;
                }
            }
        } else {
            System.out.printf("Computer turn!");
            // computer's turn, we'll do heavy pruning to keep the branching factor low
            bestScore = beta;

            // try a 2 and 4 in each cell and measure how annoying it is
            // with metrics from eval
            class Candidate {
                public Candidate(Cell cell, int value) {
                    this.cell = cell;
                    this.value = value;
                }

                public Cell cell;
                public int value;
            }
            List<Candidate> candidates = new ArrayList<Candidate>();
            List<Cell> cells = this.field.getAvailableCells();
            HashMap<Integer, HashMap<Cell, Integer>> scores = new HashMap<Integer, HashMap<Cell, Integer>>();
//                    = { 2: [], 4: [] };
            for (int value : new int[]{1, 2}) {
                scores.put(value, new HashMap<Cell, Integer>());
                for(Cell i : cells){
                    Tile tile = new Tile(i, value );
                    this.field.insertTile(tile);
                    if(!scores.get(value).containsKey(i)) {
                        scores.get(value).put(i, 0);
                    }
                    scores.get(value).put(i, scores.get(value).get(i) - this.field.smoothness());
                    this.field.removeTile(tile);
                }
            }

            // now just pick out the most annoying moves number 2 or 4
            // Find the max score
            int maxScore = -10000;
            for(int value : new int[]{1, 2}) {
                for(HashMap.Entry<Cell, Integer> score : scores.get(value).entrySet()) {
                    if(score.getValue() > maxScore) {
                        maxScore = score.getValue();
                    }
                }
            }
            System.out.printf("Max score %d \n", maxScore);
            for(int value : new int[]{1, 2}) {
                for(HashMap.Entry<Cell, Integer> set : scores.get(value).entrySet()) {
                    if(set.getValue() == maxScore) {
                        candidates.add(new Candidate(set.getKey(), value));
                    }
                }
            }

            for (Candidate candidate : candidates) {
                Grid newGrid = this.field.clone();
                Tile tile = new Tile(candidate.cell, candidate.value);
                newGrid.insertTile(tile);
                newGrid.playerTurn = true;
                positions++;
                GameAI newAi = new GameAI(newGrid);
                result = newAi.search(depth, alpha, beta, positions, cutoffs);
                positions = result.getPosition();
                cutoffs = result.getCutoffs();

                if (result.getScore() < bestScore) {
                    bestScore = result.getScore();
                }

                if (bestScore < alpha) {
                    cutoffs++;
                    result.setMove(1);
                    result.setScore(alpha);
                    result.setPosition(positions);
                    result.setCutoffs(cutoffs);
                    return result;
                }
            }
        }

        result.setMove(bestMove);
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
            if (newBest.getMove() == -1) {
                break;
            } else {
                best = newBest;
            }
            depth++;
        } while ( System.currentTimeMillis() - start < MIN_SEARCH_TIME);
        return best;
   }

   public String[] translate = new String[]{"Up", "Right", "Down", "Left"};

}
