package com.gdx.game2048.model.data;

import com.gdx.game2048.manager.GameSetting;
import com.gdx.game2048.model.animation.AnimationGrid;
import com.gdx.game2048.model.animation.AnimationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gdx.game2048.logic.GameLogic.MOVE_ANIMATION_TIME;
import static com.gdx.game2048.logic.GameLogic.SPAWN_ANIMATION_TIME;


public class Grid {
    //field save the current state
    public final Tile[][] field;
    //undoField state before
    public final Tile[][] undoField;
    //to save current stage then assign to undoField to avoid mess thing up
    private final Tile[][] bufferField;
    public int score = 0;
    public boolean playerTurn = false;

    public Grid(int sizeX, int sizeY) {
        field = new Tile[sizeX][sizeY];
        undoField = new Tile[sizeX][sizeY];
        bufferField = new Tile[sizeX][sizeY];
        score = 0;
        clearGrid();
        clearUndoGrid();
    }

    public void addTile(int x, int y) {
        //ratio 0,8 for 1 and 0,2 for 2
        //check whether the cell is null
        if (field[x][y] == null) {
            int value = 1;
            if (GameSetting.getInstance().getCheating())
                value = Math.random() <= 0.9 ? 7 : 8;
            else
                value = Math.random() <= 0.9 ? 1 : 2;
            Tile tile = new Tile(new Cell(x, y), value);
            spawnTile(tile);
        }
    }

    public void spawnTile(Tile tile) {
        //insert to grid
        insertTile(tile);
    }

    public void clearMergedFrom() {
        //clear merge from to ready to merge
        for (Tile[] array : field) {
            for (Tile tile : array) {
                //check whether tile null to avoid exception
                if (isCellOccupied(tile)) {
                    tile.setMergedFrom(null);
                }
            }
        }
    }

    public void moveTile(Tile tile, Cell cell) {
        //move tile to another cell
        field[tile.getX()][tile.getY()] = null;
        field[cell.getX()][cell.getY()] = tile;
        tile.updatePosition(cell);
    }

    private Cell getMovingVector(int direction) {
        //The grid is bottom less
//        4
//        3
//        2
//        1
//        y / x 1  2  3  4
        Cell[] map = {
                new Cell(0, 1), // up
                new Cell(1, 0),  // right
                new Cell(0, -1),  // down
                new Cell(-1, 0)  // left
        };
        return map[direction];
    }

    private List<Integer> makeTravelCellX(Cell vector) {
        List<Integer> traversals = new ArrayList<Integer>();

        for (int xx = 0; xx < field.length; xx++) {
            traversals.add(xx);
        }
        if (vector.getX() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private List<Integer> makeTravelCellY(Cell vector) {
        List<Integer> traversals = new ArrayList<Integer>();

        for (int xx = 0; xx < field[0].length; xx++) {
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
        } while (isCellWithinBounds(nextCell) && isCellAvailable(nextCell));

        return new Cell[]{previous, nextCell};
    }

    public boolean movesAvailable() {
        if(isCellsAvailable()){
            System.out.println("Cell avaiable");
        }
        if (tileMatchesAvailable()) {
            System.out.println("Tile match avaiable");
        }
        return this.isCellsAvailable() || this.tileMatchesAvailable();
    }

    private boolean tileMatchesAvailable() {
        Tile tile;

        for (int xx = 0; xx < this.field.length; xx++) {
            for (int yy = 0; yy < this.field[0].length; yy++) {
                tile = this.getCellContent(new Cell(xx, yy));

                if (tile != null) {
                    for (int direction = 0; direction < 4; direction++) {
                        Cell vector = this.getMovingVector(direction);
                        Cell cell = new Cell(xx + vector.getX(), yy + vector.getY());

                        Tile other = this.getCellContent(cell);

                        if (other != null && other.getValue() == tile.getValue()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean move(int direction) {
        return move(direction, null);
    }

    public boolean move(int direction, AnimationGrid animationGrid) {
        if(!playerTurn){
            return false;
        }
        //make travel loop varible
        Cell vector = getMovingVector(direction);
        List<Integer> travelX = makeTravelCellX(vector);
        List<Integer> travelY = makeTravelCellY(vector);

        boolean moved = false;
        //clear merge from
        clearMergedFrom();
        //loop all the cell in grid
        for (int xx : travelX) {
            for (int yy : travelY) {
                if (moveAndCheck(xx, yy, direction, animationGrid) == true) {
                    moved = true;
                }
            }
        }
        return moved;
    }

    public boolean moveAndCheck(int xx, int yy, int direction) {
        return moveAndCheck(xx, yy, direction, null);
    }

    public boolean moveAndCheck(int xx, int yy, int direction, AnimationGrid animationGrid) {
        boolean moved = false;
        //the the moving vector
        Cell vector = getMovingVector(direction);
        //get the content the current cell
        Cell cell = new Cell(xx, yy);
        Tile tile = getCellContent(cell);
        //check whether the current tile is empty or not
        if (tile != null) {
            //find the farthest cell in the direction
            Cell[] positions = findFarthestPosition(cell, vector);
            //get the second cell because the first one is cell before
            Tile next = getCellContent(positions[1]);
            //whether they have the same value and not merge with other cell
            if (next != null && next.getValue() == tile.getValue() && next.getMergedFrom() == null) {
                //they have the same value
                //increment their value by 1
                Tile merge = new Tile(positions[1], tile.getValue() + 1);
                //set the 2 cells are merged
                Tile[] temp = {tile, next};
                merge.setMergedFrom(temp);
                //remove the first one (or moving cell) and insert the merge cell
                insertTile(merge);
                removeTile(tile);
                tile.updatePosition(positions[1]);
                score += tile.getValue() + 1;
                if(animationGrid != null){
                    //add moving and merge animation
                    int[] extras = {xx, yy}; // the cell moving to
                    animationGrid.startAnimation(merge.getX(), merge.getY(), AnimationType.MOVE,
                            MOVE_ANIMATION_TIME, 0, extras);
                    //merge animation after the move animation complete
                    animationGrid.startAnimation(merge.getX(), merge.getY(), AnimationType.MERGE,
                            SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);
                }

            } else {
                //just move the cell
                moveTile(tile, positions[0]);
                if(animationGrid != null){
                    int[] extras = {xx, yy, 0};
                    animationGrid.startAnimation(positions[0].getX(), positions[0].getY(), AnimationType.MOVE,
                            MOVE_ANIMATION_TIME, 0, extras);
                }
            }

            if (!positionsEqual(cell, tile)) {
                //same cell have move
                moved = true;
            }
        }
        return moved;
    }

    private boolean positionsEqual(Cell first, Cell second) {
        return first.getX() == second.getX() && first.getY() == second.getY();
    }

    public Cell randomAvailableCell() {
        ArrayList<Cell> availableCells = getAvailableCells();
        if (availableCells.size() >= 1) {
            return availableCells.get((int) Math.floor(Math.random() * availableCells.size()));
        }
        return null;
    }

    public ArrayList<Cell> getAvailableCells() {
        ArrayList<Cell> availableCells = new ArrayList<Cell>();
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] == null) {
                    availableCells.add(new Cell(xx, yy));
                }
            }
        }
        return availableCells;
    }


    public boolean isCellsAvailable() {
        return (getAvailableCells().size() >= 1);
    }

    public boolean isCellAvailable(Cell cell) {
        return !isCellOccupied(cell);
    }

    public boolean isCellOccupied(Cell cell) {
        return (getCellContent(cell) != null);
    }

    public Tile getCellContent(Cell cell) {
        if (cell != null && isCellWithinBounds(cell)) {
            return field[cell.getX()][cell.getY()];
        } else {
            return null;
        }
    }

    public Tile getCellContent(int x, int y) {
        if (isCellWithinBounds(x, y)) {
            return field[x][y];
        } else {
            return null;
        }
    }

    public boolean isCellWithinBounds(Cell cell) {
        return 0 <= cell.getX() && cell.getX() < field.length
                && 0 <= cell.getY() && cell.getY() < field[0].length;
    }

    private boolean isCellWithinBounds(int x, int y) {
        return 0 <= x && x < field.length
                && 0 <= y && y < field[0].length;
    }

    public void insertTile(Tile tile) {
        field[tile.getX()][tile.getY()] = tile;
    }

    public void removeTile(Tile tile) {
        field[tile.getX()][tile.getY()] = null;
    }

    public void saveTiles() {
        copyField(bufferField, undoField);
    }

    public void prepareSaveTiles() {
        copyField(field, bufferField);
    }

    private void copyField(Tile[][] srcField, Tile[][] desField) {
        for (int xx = 0; xx < srcField.length; xx++) {
            for (int yy = 0; yy < srcField[0].length; yy++) {
                if (srcField[xx][yy] == null) {
                    desField[xx][yy] = null;
                } else {
                    desField[xx][yy] = new Tile(xx, yy, srcField[xx][yy].getValue());
                }
            }
        }
    }

    public void revertTiles() {
        copyField(undoField, field);
    }

    public void clearGrid() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                field[xx][yy] = null;
            }
        }
    }

    public void clearUndoGrid() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                undoField[xx][yy] = null;
            }
        }
    }

    public int countOccupiedCell(){
        int count = 0;
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if(field[xx][yy] != null){
                    count++;
                }
            }
        }
        return  count;
    }

    public int countUndoOccupiedCell(){
        int count = 0;
        for (int xx = 0; xx < undoField.length; xx++) {
            for (int yy = 0; yy < undoField[0].length; yy++) {
                if(undoField[xx][yy] != null){
                    count++;
                }
            }
        }
        return  count;
    }

    public Grid clone() {
        Grid newGrid = new Grid(field.length, field[0].length);
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null) {
                    Tile tile = new Tile(xx, yy, field[xx][yy].getValue());
                    newGrid.insertTile(tile);
                }
            }
        }
        newGrid.score = score;
        newGrid.playerTurn = playerTurn;
        return newGrid;
    }

    // measures how smooth the grid is (as if the values of the pieces
// were interpreted as elevations). Sums of the pairwise difference
// between neighboring tiles (in log space, so it represents the
// number of merges that need to happen before they can merge).
// Note that the pieces can be distant
    public int smoothness(){
        int smootness = 0;
        for (int xx = 0; xx < this.field.length; xx++) {
            for (int yy = 0; yy < this.field[0].length; yy++) {
                if (this.isCellOccupied(new Cell(xx, yy))) {
                    int value = this.getCellContent(xx, yy).getValue();
                    for (int directtion = 1; directtion <= 2; directtion++) {
                        Cell vector = this.getMovingVector(directtion);
                        Cell targetCell = this.findFarthestPosition(new Cell(xx, yy), vector)[1];

                        if (this.isCellOccupied(targetCell)) {
                            Tile target = this.getCellContent(targetCell);
                            int targetValue = target.getValue();
                            smootness -= Math.abs(value - targetValue);
                        }
                    }
                }
            }
        }
        return smootness;
    }


    public int monotonicity(){
        int totals[] = new int[]{0, 0, 0, 0};

        //left . right directtion
        for (int x = 0; x < this.field.length; x++) {
            int current = 0;
            int next = current + 1;
            while (next < this.field.length) {
                while (next < this.field.length && !this.isCellOccupied(new Cell(x, next))) {
                    next ++;
                }
                if (next >= this.field.length) {
                    next --;
                }

                double currentValue = this.isCellOccupied(new Cell(x, current)) ?
                        this.getCellContent(new Cell(x, current)).getValue() : 0;

                double nextValue = this.isCellOccupied(new Cell(x, next)) ?
                        this.getCellContent(new Cell(x, next)).getValue() : 0;

                if (currentValue > nextValue) {
                    totals[0] += nextValue - currentValue;
                } else if (nextValue > currentValue) {
                    totals[1] += currentValue - nextValue;
                }
                current = next;
                next++;
            }
        }

//         up/down direction
        for (int y=0; y<this.field[0].length; y++) {
            int current = 0;
            int next = current+1;
            while ( next< this.field[0].length ) {
                while ( next < this.field[0].length && !this.isCellOccupied(new Cell(next, y) )) {
                    next++;
                }

                if (next >= this.field[0].length) { next--; }

                double currentValue = this.isCellOccupied(new Cell(current, y)) ?
                this.getCellContent(current,y).getValue() : 0;

                double nextValue = this.isCellOccupied(new Cell(next, y)) ?
                       this.getCellContent(next,y).getValue() : 0;


                if (currentValue > nextValue) {
                    totals[2] += nextValue - currentValue;
                } else if (nextValue > currentValue) {
                    totals[3] += currentValue - nextValue;
                }
                current = next;
                next++;
            }
        }

       return Math.max(totals[0], totals[1]) + Math.max(totals[2], totals[3]);
    }

    public int maxValue(){
        int max = 0;
        for (int x = 0; x < this.field.length; x++) {
            for (int y = 0; y < this.field[0].length; y++) {
                if (this.isCellOccupied(new Cell(x, y))) {
                    int value = this.getCellContent(new Cell(x, y)).getValue();
                    if (value > max) {
                        max = value;
                    }
                }
            }
        }

        return max;
    }

    public boolean isWin() {
        for (int x=0; x<this.field.length; x++) {
            for (int y=0; y<this.field[0].length; y++) {
                if (this.isCellOccupied(new Cell(x, y))) {
                    if (this.getCellContent(x, y).getValue() == 11) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void printCurrentField(){
        System.out.print("Currenf field : \n");
        for (int y= this.field[0].length - 1; y >= 0; y--)
       {
                System.out.print("[");
           for (int x=0; x<this.field.length; x++)
            {
                if (this.isCellOccupied(new Cell(x, y))) {
                    System.out.print(this.getCellContent(x, y).getValue());
                } else {
                    System.out.print("*");
                }
                System.out.print(" ");
            }
            System.out.print("]\n");
        }
        System.out.printf("Smoothness : %d\n", this.smoothness());
        System.out.printf("Mono : %d\n", this.monotonicity());
        System.out.printf("Maxcell : %d\n", this.maxValue());
        System.out.printf("Emply : %d\n", this.getAvailableCells().size());
    }

}
