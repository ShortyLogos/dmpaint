//package com.dm.dmpaint;
//
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.Path;
//
//public class Efface extends Dessin {
//    private Path path;
//    private Paint crayon;
//
//    public Efface(Integer couleurFond, int largeur, Path path) {
//        super(couleurFond, largeur);
//        this.path = path;
//        this.crayon = new Paint(Paint.ANTI_ALIAS_FLAG);
//
//        crayon.setColor(couleurFond);
//        crayon.setStrokeWidth(largeur);
//        crayon.setStyle(Paint.Style.STROKE);
//    }
//
//    public Path getPath() {
//        return this.path;
//    }
//
//    public void dessiner(Canvas canvas) {
//        canvas.drawPath(path, crayon);
//    }
//}
