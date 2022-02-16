package com.dm.dmpaint;

import android.graphics.Canvas;
import android.graphics.Path;

public class Triangle extends Forme {
    int x3, y3;

    public Triangle(int couleur, int x1, int y1, int x2, int y2, int x3, int y3) {
        super(couleur, x1, y1, x2, y2);
        this.x3 = x3;
        this.y3 = y3;
    }

    public void dessiner(Canvas canvas) {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        // On dessine les segments du triangle et on le referme
        path.lineTo(getX2(), getY2());
        path.lineTo(x3, y3);
        path.lineTo(getX1(), getY1());
        path.close();

//        // 1er segment
//        canvas.drawLine(getX1(), getY1(), getX2(), getY2(), crayon);
//        // 2e segment
//        canvas.drawLine(getX2(), getY2(), x3, y3, crayon);
//        // 3e segment
//        canvas.drawLine(x3, y3, getX1(), getY1(), crayon);

        canvas.drawPath(path, getCrayon());
    }
}
