package com.dm.dmpaint;

import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class Dessin {
    private int couleur;
    private int largeur;

    public Dessin (int couleur, int largeur) {
        this.couleur = couleur;
        this.largeur = largeur;
    }

    public void dessiner(Canvas canvas) {

    }
}
