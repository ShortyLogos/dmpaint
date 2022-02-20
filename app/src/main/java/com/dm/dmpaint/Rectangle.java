package com.dm.dmpaint;

import android.graphics.Canvas;

public class Rectangle extends Forme {

    public Rectangle(int couleur, int opacite, int x1, int y1, int x2, int y2) {
        super(couleur, opacite, x1, y1, x2, y2);
    }

    public void dessiner(Canvas canvas) {
        canvas.drawRect(getX1(), getY1(), getX2(), getY2(), getCrayon());
    }
}
