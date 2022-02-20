package com.dm.dmpaint;

import android.graphics.Canvas;
import android.graphics.Path;

public class Efface extends TraceLibre {

    public Efface(int couleur, int largeur, Path path) {
        super(couleur, largeur, 255, path);
    }

    public void dessiner(Canvas canvas) {
        super.dessiner(canvas);
    }
}
