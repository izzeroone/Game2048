package com.gdx.game2048.CellColor;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;

public class CellsColor {
    ArrayList<Color> cellsColor = new ArrayList<Color>();
    public CellsColor(){
        cellsColor.add(new Color(1,1,1,1)); // dummy first color
        cellsColor.add(1, colorFromInt(254, 63 , 107, 1));
        cellsColor.add(2, colorFromInt(255, 168 , 68, 1));
        cellsColor.add(3, colorFromInt(117, 100 , 255, 1));
        cellsColor.add(4, colorFromInt(67, 204 , 246, 1));
        cellsColor.add(5, colorFromInt(182, 88 , 62, 1));
        cellsColor.add(6, colorFromInt(246, 94 , 59, 1));
        cellsColor.add(7, colorFromInt(169, 15 , 219, 1));
        cellsColor.add(8, colorFromInt(149, 94 , 255, 1));
        cellsColor.add(9, colorFromInt(237, 200 , 80, 1));

    }

    public Color getColor(int cellValue) {
        if(cellValue > 9)
            return cellsColor.get(9);
        return cellsColor.get(cellValue);
    }

    private Color colorFromInt(int r, int g, int b, float a) {
        return new Color(r / 255f, g / 255f, b / 255f, a);
    }
}
