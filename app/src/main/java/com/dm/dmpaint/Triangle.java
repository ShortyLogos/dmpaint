package com.dm.dmpaint;

import android.graphics.Canvas;
import android.graphics.Path;

public class Triangle extends Forme {
    int x3, y3;

    public Triangle(int couleur, int opacite, int x1, int y1, int x2, int y2, int x3, int y3) {
        super(couleur, opacite, x1, y1, x2, y2);
        this.x3 = x3;
        this.y3 = y3;
    }

    public void dessiner(Canvas canvas) {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        // On dessine les segments du triangle et on le referme
        path.moveTo(getX1(), getY1());
        path.lineTo(getX2(), getY2());
        path.lineTo(x3, y3);

        path.close();

        canvas.drawPath(path, getCrayon());
    }
}
