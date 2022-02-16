package com.dm.dmpaint;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class TraceLibre extends Dessin {
        private Path path;

    public TraceLibre(int couleur, int largeur, Path path) {
        super(couleur, largeur, false);
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }

    public void dessiner(Canvas canvas) {
        canvas.drawPath(path, getCrayon());
    }
}
