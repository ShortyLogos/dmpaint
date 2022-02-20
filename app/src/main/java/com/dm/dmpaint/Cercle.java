package com.dm.dmpaint;

import android.graphics.Canvas;

public class Cercle extends Forme {

    public Cercle(int couleur, int opacite, int x1, int y1, int x2, int y2) {
        super(couleur, opacite, x1, y1, x2, y2);
    }

    public void dessiner(Canvas canvas) {
        int x1 = getX1();
        int x2 = getX2();
        int y1 = getY1();
        int y2 = getY2();

        int deltaX = Math.abs(x1 - x2) * Math.abs(x1 - x2);
        int deltaY = Math.abs(y1 - y2) * Math.abs(y1 - y2);
        float rayon = (float) Math.sqrt(deltaX + deltaY);
        canvas.drawCircle(x1, y1, rayon,getCrayon());
    }
}
