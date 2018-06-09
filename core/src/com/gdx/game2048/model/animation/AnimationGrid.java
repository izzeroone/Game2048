package com.gdx.game2048.model.animation;

import java.util.ArrayList;

/**
 * Created by Cyber on 12/3/2017.
 */
//Save the animation information, the draw is done on the MainView
public class AnimationGrid {
    public final ArrayList<AnimationCell> globalAnimation = new ArrayList<AnimationCell>();
    private final ArrayList<AnimationCell>[][] field;
    private int activeAnimations = 0;
    private boolean oneMoreFrame = false;

    public AnimationGrid(int x, int y) {
        field = new ArrayList[x][y];

        for (int xx = 0; xx < x; xx++) {
            for (int yy = 0; yy < y; yy++) {
                field[xx][yy] = new ArrayList<AnimationCell>();
            }
        }
    }

    public void startAnimation(int x, int y, AnimationType animationType, long length, long delay, int[] extras) {
        AnimationCell animationToAdd = new AnimationCell(x, y, animationType, length, delay, extras);
        if (x == -1 && y == -1) {
            globalAnimation.add(animationToAdd);
        } else {
            field[x][y].add(animationToAdd);
        }
        activeAnimations = activeAnimations + 1;
    }

    public void updateAll(long deltatime) {
        ArrayList<AnimationCell> cancelledAnimations = new ArrayList<AnimationCell>();
        update(deltatime, cancelledAnimations, globalAnimation);

        for (ArrayList<AnimationCell>[] array : field) {
            for (ArrayList<AnimationCell> list : array) {
                update(deltatime, cancelledAnimations, list);
            }
        }

        for (AnimationCell animation : cancelledAnimations) {
            cancelAnimation(animation);
        }
    }

    private void update(long deltatime, ArrayList<AnimationCell> cancelledAnimations, ArrayList<AnimationCell> globalAnimation) {
        for (AnimationCell animation : globalAnimation) {
            animation.update(deltatime);
            if (animation.animationDone()) {
                cancelledAnimations.add(animation);
                activeAnimations = activeAnimations - 1;
            }
        }
    }

    public boolean isAnimationActive() {
        if (activeAnimations != 0) {
            oneMoreFrame = true;
            return true;
        } else if (oneMoreFrame) {
            oneMoreFrame = false;
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<AnimationCell> getAnimationCell(int x, int y) {
        return field[x][y];
    }

    public void cancelAnimations() {
        for (ArrayList<AnimationCell>[] array : field) {
            for (ArrayList<AnimationCell> list : array) {
                list.clear();
            }
        }
        globalAnimation.clear();
        activeAnimations = 0;
    }

    private void cancelAnimation(AnimationCell animation) {
        if (animation.getX() == -1 && animation.getY() == -1) {
            globalAnimation.remove(animation);
        } else {
            field[animation.getX()][animation.getY()].remove(animation);
        }
    }
}
