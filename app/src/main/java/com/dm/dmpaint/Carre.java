package com.dm.dmpaint;

import android.graphics.Canvas;

public class Carre extends Dessin {
    int x1, x2, y1, y2;

    public Carre(int couleur, int x1, int y1, int x2, int y2) {
        super(couleur, 20, true);
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public void dessiner(Canvas canvas) {
        canvas.drawRect(x1, y1, x2, y2, getCrayon());
    }
}
