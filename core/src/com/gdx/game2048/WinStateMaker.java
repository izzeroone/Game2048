package com.gdx.game2048;


import java.util.Comparator;
import java.util.PriorityQueue;


public class WinStateMaker {
    private int maxCell;
    private long timer;

    WinStateMaker(int numberCells) {
        maxCell = numberCells;
        timer = System.currentTimeMillis();
    }

    public Grid makeWinState(Grid startGrid) {
        PriorityQueue<Grid> queue = new PriorityQueue<Grid>(100, new Comparator<Grid>() {
            @Override
            public int compare(Grid grid, Grid t1) {
                return grid.getNumberOccupiedCells() - t1.getNumberOccupiedCells();
            }
        });

        startGrid.score = 0;
        queue.add(startGrid);
        Grid min = startGrid;

        while (!queue.isEmpty()) {
            min = queue.poll();
            if (min.getNumberOccupiedCells() <= maxCell) {
                return min;
            }

            if (System.currentTimeMillis() - timer >= 1000) //over 1 second so break
                break;

            for (int direction = 0; direction < 4; direction++) {
                min.move(direction);
                queue.add(min.clone());
            }
        }
        return min;
    }
}

