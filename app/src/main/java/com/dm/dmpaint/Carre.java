package com.dm.dmpaint;

import android.graphics.Canvas;

public class Carre extends Forme {

    public Carre(int couleur, int x1, int y1, int x2, int y2) {
        super(couleur, x1, y1, x2, y2);
    }

    public void dessiner(Canvas canvas) {
        canvas.drawRect(getX1(), getY1(), getX2(), getY2(), getCrayon());
    }
}
