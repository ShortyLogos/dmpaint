package com.dm.dmpaint;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public abstract class Dessin {
    private int couleur;
    private int largeur;
    private int opacite;
    private boolean plein;
    private Paint crayon;

    public Dessin (int couleur, int largeur, int opacite, boolean plein) {
        this.couleur = couleur;
        this.largeur = largeur;
        this.opacite = opacite;
        this.plein = plein;
        this.crayon = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (plein) {
            crayon.setStyle(Paint.Style.FILL);
        }
        else {
            crayon.setStyle(Paint.Style.STROKE);
        }

        crayon.setColor(couleur);
        crayon.setStrokeWidth(largeur);
        crayon.setAlpha(opacite);
    }

    public void dessiner(Canvas canvas) {

    }

    public int getCouleur() {
        return couleur;
    }

    public void setCouleur(int couleur) {
        this.couleur = couleur;
    }

    public int getLargeur() {
        return largeur;
    }

    public void setLargeur(int largeur) {
        this.largeur = largeur;
    }

    public boolean isPlein() {
        return plein;
    }

    public void setPlein(boolean plein) {
        this.plein = plein;
    }

    public Paint getCrayon() {
        return crayon;
    }

    public void setCrayon(Paint crayon) {
        this.crayon = crayon;
    }
}
