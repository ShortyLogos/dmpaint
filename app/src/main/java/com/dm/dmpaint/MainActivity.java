package com.dm.dmpaint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Trace;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    Surface surface;
    ConstraintLayout surfaceContainer;
    SeekBar largeur;
    TextView largeurTexte;
    Button displayActiveColor;
    LinearLayout colorsPalette;
    LinearLayout tools;
    ImageView undo;
    ImageView supprimer;

    Vector<Dessin> objetsDessin = new Vector<Dessin>();

    Path traceLibre;

    Paint crayonPlein, crayonContour;

    int couleurActive = Color.BLACK;
    Integer couleurFondActive = Color.WHITE;
    int largeurActive;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceContainer = findViewById(R.id.surfaceContainer);
        largeur = findViewById(R.id.largeur);
        largeurTexte = findViewById(R.id.largeurTexte);
        colorsPalette = findViewById(R.id.colorsPalette);
        displayActiveColor = findViewById(R.id.displayActiveColor);
        tools = findViewById(R.id.tools);
        undo = findViewById(R.id.undo);
        supprimer = findViewById(R.id.supprimer);

        // 1. Mise en place de la surface de dessin
        // 1.1 Création de l'objet Surface
        surface = new Surface(this);

        // 1.2 Taille de l'objet Surface
        surface.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // 1.3 Ajout de la Surface à son conteneur avec la méthode .addView()
        surfaceContainer.addView(surface);

        // Paramètres par défaut lors de l'ouverture de l'app
        displayActiveColor.setBackgroundColor(couleurActive);
        largeur.setMin(1);
        largeur.setMax(125);
        largeur.setProgress(15);
        largeurActive = largeur.getProgress();
        largeurTexte.setText(String.valueOf(largeurActive));

        // 2. Gestion des évènements
        // 2.1 Création des écouteurs
        EcouteurCouleurs ecCouleurs = new EcouteurCouleurs();
        EcouteurSurface ecSurface = new EcouteurSurface();
        EcouteurOutils ecOutils = new EcouteurOutils();

        // 2.2 Inscriptions des sources aux écouteurs
        for (int i = 0; i < colorsPalette.getChildCount(); i++) {
            if (colorsPalette.getChildAt(i) instanceof Button) {
                colorsPalette.getChildAt(i).setOnClickListener(ecCouleurs);
            }
        }

        surface.setOnTouchListener(ecSurface);
        largeur.setOnSeekBarChangeListener(ecOutils);
        undo.setOnClickListener(ecOutils);
        supprimer.setOnClickListener(ecOutils);
    }

    // 2.3 Définition des classes Écouteurs
    private class EcouteurCouleurs implements View.OnClickListener {

        @Override
        public void onClick(View source) {
            Button btn = (Button)source;
            couleurActive = ((ColorDrawable)btn.getBackground()).getColor();
            displayActiveColor.setBackgroundColor(couleurActive);
        }
    }

    private class EcouteurSurface implements View.OnTouchListener {

        @Override
        public boolean onTouch(View source, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                traceLibre = new Path();
                traceLibre.moveTo(motionEvent.getX(), motionEvent.getY());
                surface.invalidate();
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                traceLibre.lineTo(motionEvent.getX(), motionEvent.getY());
                surface.invalidate();
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                TraceLibre trait = new TraceLibre(couleurActive, largeurActive, traceLibre);
                objetsDessin.add(trait);
            }

            return true;
        }
    }

    private class EcouteurOutils implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

        @Override
        public void onClick(View source) {
            if (source == undo && objetsDessin.size() > 0) {
                objetsDessin.remove(objetsDessin.size() -1);
                surface.invalidate();
                traceLibre = null;
            }
            else if (source == supprimer && objetsDessin.size() > 0)  {
                objetsDessin.clear();
                surface.invalidate();
                traceLibre = null;
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar == largeur) {
                largeurActive = progress;
                largeurTexte.setText(String.valueOf(largeurActive));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    // Classe interne qui définit la surface de dessin
    private class Surface extends View {

        public Surface(Context context) {
            super(context);
            // Initialisation dans le constructeur des objets Paint avec
            // lesquels on dessinera sur la surface de dessin.
            crayonPlein = new Paint(Paint.ANTI_ALIAS_FLAG);
            crayonContour = new Paint(Paint.ANTI_ALIAS_FLAG);
            crayonContour.setStyle(Paint.Style.STROKE);

            // Pour des tests :
            crayonPlein.setStrokeWidth(30);
            crayonPlein.setColor(Color.BLACK);
        }

        // Redéfinition de la méthode onDraw de la classe interne Surface
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            surface.setBackgroundColor(couleurFondActive);
            crayonContour.setColor(couleurActive);
            crayonContour.setStrokeWidth(largeurActive);

            // Ici on dessinera
            if (objetsDessin.size() > 0) {
                for (Dessin dessin : objetsDessin) {
                    dessin.dessiner(canvas);
                }
            }

            if (traceLibre != null) {
                canvas.drawPath(traceLibre, crayonContour);
            }
        }
    }

}