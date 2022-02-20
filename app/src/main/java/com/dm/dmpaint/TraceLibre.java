package com.dm.dmpaint;

import android.graphics.Canvas;
import android.graphics.Path;

public class TraceLibre extends Dessin {
        private Path path;

    public TraceLibre(int couleur, int largeur, int opacite, Path path) {
        super(couleur, largeur, opacite, false);
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }

    public void dessiner(Canvas canvas) {
        canvas.drawPath(path, getCrayon());
    }
}
