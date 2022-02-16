package com.dm.dmpaint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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
    ImageView pinceau;
    ImageView efface;
    ImageView pipette;
    ImageView cercle;
    ImageView carre;
    ImageView info;
    ImageView supprimer;

    Vector<Dessin> objetsDessin = new Vector<Dessin>();

    Path traceLibre;

    Paint crayonPlein, crayonContour;
    Point depart;
    Point arrivee;

    int couleurActive = Color.BLACK;
    Integer couleurFondActive = Color.WHITE;
    int largeurActive;
    String outilActif = "traceLibre";

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
        pinceau = findViewById(R.id.pinceau);
        pipette = findViewById(R.id.pipette);
        cercle = findViewById(R.id.cercle);
        carre = findViewById(R.id.carre);
        info = findViewById(R.id.info);
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

        for (int i = 0; i < tools.getChildCount(); i++) {
                tools.getChildAt(i).setOnClickListener(ecOutils);
        }

        surface.setOnTouchListener(ecSurface);
        largeur.setOnSeekBarChangeListener(ecOutils);
        undo.setOnClickListener(ecOutils);
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

                if (outilActif.equals("traceLibre") || outilActif.equals("efface")) {
                    traceLibre = new Path();
                    traceLibre.moveTo(motionEvent.getX(), motionEvent.getY());
                    surface.invalidate();
                }
                else if (outilActif.equals("pipette")) {
                    outilActif = "pipette";
                    Bitmap bitmap = surface.getBitmapImage();
                    couleurActive = bitmap.getPixel((int)motionEvent.getX(), (int)motionEvent.getY());
                }
                else if (outilActif.equals("cercle") || outilActif.equals("carre")) {
                    depart = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                    surface.invalidate();
                }

            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                if (outilActif.equals("traceLibre") || outilActif.equals("efface")) {
                    traceLibre.lineTo(motionEvent.getX(), motionEvent.getY());
                    surface.invalidate();
                }
                else if (outilActif.equals("pipette")) {
                    outilActif = "pipette";
                    Bitmap bitmap = surface.getBitmapImage();
                    couleurActive = bitmap.getPixel((int)motionEvent.getX(), (int)motionEvent.getY());
                }
                else if (outilActif.equals("cercle") || outilActif.equals("carre")) {
                    arrivee = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                    surface.invalidate();
                }

            }

            else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                if (traceLibre != null) {
                    if (outilActif.equals("traceLibre")) {
                        TraceLibre trait = new TraceLibre(couleurActive, largeurActive, traceLibre);
                        objetsDessin.add(trait);
                        traceLibre = null;
                    }
                    else if (outilActif.equals("efface")) {
                        Efface efface = new Efface(couleurFondActive, largeurActive, traceLibre);
                        objetsDessin.add(efface);
                        traceLibre = null;
                    }
                }

                if (outilActif.equals("pipette")) {
                    displayActiveColor.setBackgroundColor(couleurActive);
                    outilActif = "traceLibre";
                }
                else if (outilActif.equals("cercle")) {
                    Cercle cercle = new Cercle(couleurActive, depart.x, depart.y, arrivee.x, arrivee.y);
                    objetsDessin.add(cercle);
                }
                else if (outilActif.equals("carre")) {
                    Carre carre = new Carre(couleurActive, depart.x, depart.y, arrivee.x, arrivee.y);
                    objetsDessin.add(carre);
                }

                if (depart != null && arrivee != null) {
                    depart = null;
                    arrivee = null;
                }
            }

            return true;
        }
    }

    private class EcouteurOutils implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

        @Override
        public void onClick(View source) {
            if (source == undo && objetsDessin.size() > 0) {
                objetsDessin.remove(objetsDessin.size() - 1);
                surface.invalidate();
                traceLibre = null;
            }
            else if (source == pinceau) {
                outilActif = "traceLibre";
            }
            else if (source == efface) {
                outilActif = "efface";
            }
            else if (source == pipette) {
                outilActif = "pipette";
            }
            else if (source == cercle) {
                outilActif = "cercle";
            }
            else if (source == carre) {
                outilActif = "carre";
            }
            else if (source == supprimer && objetsDessin.size() > 0)  {
                objetsDessin.clear();
                surface.invalidate();
                traceLibre = null;
            }
            else if (source == info) {
                String msg = "Réalisé par Déric Marchand\n\n" +
                        "Couleur personnalisée : cliquez sur l'afficheur de couleur active.\n\n" +
                        "Prenez garde : la suppression d'une image à l'aide du bouton Supprimer est permanente. Le bouton Undo ne vous sauvera pas :)";
                boiteDialogue(msg, "DMPaint v1.0");
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
        }

        // Redéfinition de la méthode onDraw de la classe interne Surface
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            surface.setBackgroundColor(couleurFondActive);
            crayonContour.setColor(couleurActive);
            crayonPlein.setColor(couleurActive);
            crayonContour.setStrokeWidth(largeurActive);
            crayonPlein.setStrokeWidth(largeurActive);

            // Ici on dessinera
            if (objetsDessin.size() > 0) {
                for (Dessin dessin : objetsDessin) {
                    if (dessin instanceof Efface) {
                        Paint efface = dessin.getCrayon();
                        efface.setColor(couleurFondActive);
                    }
                    dessin.dessiner(canvas);
                }
            }

            if (traceLibre != null) {
                canvas.drawPath(traceLibre, crayonContour);
            }

            if (depart != null && arrivee != null) {

                if (outilActif.equals("cercle")) {
                    int deltaX = Math.abs(arrivee.x - depart.x) * Math.abs(arrivee.x - depart.x);
                    int deltaY = Math.abs(arrivee.y - depart.y) * Math.abs(arrivee.y - depart.y);
                    float rayon = (float) Math.sqrt(deltaX + deltaY);
                    canvas.drawCircle(depart.x, depart.y, rayon, crayonPlein);
                }

                else if (outilActif.equals("carre")) {
                    canvas.drawRect(depart.x, depart.y, arrivee.x, arrivee.y, crayonPlein);
                }

            }
        }

        public Bitmap getBitmapImage() {
            this.buildDrawingCache();
            Bitmap bitmapImage = Bitmap.createBitmap(this.getDrawingCache());
            this.destroyDrawingCache();

            return bitmapImage;
        }
    }

    public void boiteDialogue(String msg, String titre) {
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);

        b.setMessage(msg)
                .setTitle(titre);

        AlertDialog dialog = b.create();
        dialog.show();
    }

}