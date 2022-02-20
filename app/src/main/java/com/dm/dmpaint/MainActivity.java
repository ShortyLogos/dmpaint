package com.dm.dmpaint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    Surface surface;
    ConstraintLayout surfaceContainer;
    SeekBar opacite;
    TextView opaciteTexte;
    SeekBar largeur;
    TextView largeurTexte;
    Button afficheCouleurActive;
    LinearLayout colorsPalette;
    LinearLayout tools;
    ImageView undo;
    ImageView pinceau;
    ImageView efface;
    ImageView pipette;
    ImageView cercle;
    ImageView rectangle;
    ImageView triangle;
    ImageView remplir;
    ImageView sauvegarder;
    ImageView info;
    ImageView supprimer;

    Vector<Dessin> objetsDessin = new Vector<Dessin>();

    Paint crayonPlein, crayonContour, crayonEfface;
    Point depart;
    Point arrivee;
    Point intermediaire; // Ce point est seulement requis pour le traçage d'un triangle

    int couleurActive;
    Integer couleurFondActive;
    int largeurActive;
    int opaciteActive;
    String outilActif;
    String nomImage;

    Path traceLibre; // Path pour afficher le tracé avant qu'il soit ajouté au Vecteur d'objets Dessin

    // Cette variable permet de compter le nombre de points dessinés afin d'ajouter le triangle
    // dessiné au Vecteur d'objets Dessin au moment opportun
    int trianglePoints = 0;
    Path triangleCourant = new Path();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceContainer = findViewById(R.id.surfaceContainer);
        opacite = findViewById(R.id.opacite);
        opaciteTexte = findViewById(R.id.opaciteTexte);
        largeur = findViewById(R.id.largeur);
        largeurTexte = findViewById(R.id.largeurTexte);
        colorsPalette = findViewById(R.id.colorsPalette);
        afficheCouleurActive = findViewById(R.id.afficheCouleurActive);
        tools = findViewById(R.id.tools);
        undo = findViewById(R.id.undo);
        pinceau = findViewById(R.id.pinceau);
        efface = findViewById(R.id.efface);
        pipette = findViewById(R.id.pipette);
        cercle = findViewById(R.id.cercle);
        rectangle = findViewById(R.id.rectangle);
        triangle = findViewById(R.id.triangle);
        remplir = findViewById(R.id.remplir);
        sauvegarder = findViewById(R.id.sauvegarder);
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
        largeur.setMin(1);
        largeur.setMax(125);
        largeur.setProgress(15);
        largeurActive = largeur.getProgress();
        largeurTexte.setText(String.valueOf(largeurActive));
        opacite.setMin(1);
        opacite.setMax(255);
        opacite.setProgress(255);
        opaciteActive = opacite.getProgress();
        opaciteTexte.setText((String.valueOf(opaciteActive)));
        couleurActive = Color.BLACK;
        couleurFondActive = Color.WHITE;
        afficheCouleurActive.setBackgroundColor(couleurActive);
        opaciteActive = 255;
        outilActif = "traceLibre";

        // 2. Gestion des évènements
        // 2.1 Création des écouteurs
        EcouteurCouleurs ecCouleurs = new EcouteurCouleurs();
        EcouteurSurface ecSurface = new EcouteurSurface();
        EcouteurOutils ecOutils = new EcouteurOutils();

        // 2.2 Inscriptions des sources aux écouteurs
        // On inscrit tous les boutons utilisés pour la sélection de couleurs
        for (int i = 0; i < colorsPalette.getChildCount(); i++) {
            if (colorsPalette.getChildAt(i) instanceof Button) {
                colorsPalette.getChildAt(i).setOnClickListener(ecCouleurs);
            }
        }

        // On inscrit tous les boutons disponibles dans la barre d'outils
        for (int i = 0; i < tools.getChildCount(); i++) {
                tools.getChildAt(i).setOnClickListener(ecOutils);
        }

        surface.setOnTouchListener(ecSurface);
        largeur.setOnSeekBarChangeListener(ecOutils);
        opacite.setOnSeekBarChangeListener(ecOutils);
        afficheCouleurActive.setOnClickListener(ecCouleurs);
        undo.setOnClickListener(ecOutils);
    }

    // 2.3 Définition des classes Écouteurs
    private class EcouteurCouleurs implements View.OnClickListener {
        @Override
        public void onClick(View source) {
            Button btn = (Button)source;
            couleurActive = ((ColorDrawable)btn.getBackground()).getColor();
            afficheCouleurActive.setBackgroundColor(couleurActive);

            if (source == afficheCouleurActive) {
                // Appel de la fonction pour sélectionner une couleur personnalisée.
                // Possible grâce à une librairie trouvée sur Github : https://github.com/yukuku/ambilwarna
                boiteDialogueCouleur();
            }

        }
    }

    private class EcouteurSurface implements View.OnTouchListener {
        Triangle triangle; // Variable nécessaire dans la classe pour redessiner le sommet du triangle lorsqu'on déplace le doigt
        @Override
        public boolean onTouch(View source, MotionEvent motionEvent) {
            // Dans cet écouteur, on gère le dessin en temps réel + le moment où un dessin
            // est ajouté au vecteur d'objets Dessin
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                if (outilActif.equals("traceLibre") | outilActif.equals("efface")) {
                    traceLibre = new Path();
                    traceLibre.moveTo(motionEvent.getX(), motionEvent.getY());
                    surface.invalidate();
                }
                else if (outilActif.equals("pipette")) {
                    outilActif = "pipette";
                    Bitmap bitmap = surface.getBitmapImage();
                    couleurActive = bitmap.getPixel((int)motionEvent.getX(), (int)motionEvent.getY());
                }
                else if (outilActif.equals("cercle") | outilActif.equals("rectangle")) {
                    depart = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                    surface.invalidate();
                }
                else if (outilActif.equals("triangle")) {
                    if (trianglePoints == 0) {
                        depart = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                        trianglePoints++;
                    }
                    else if (trianglePoints == 2) {
                        arrivee = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                        surface.invalidate();
                        trianglePoints++;
                    }
                }
                else if (outilActif.equals(("remplir"))) {
                    couleurFondActive = couleurActive;
                    surface.invalidate();
                }

            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                if (outilActif.equals("traceLibre") | outilActif.equals("efface")) {
                    traceLibre.lineTo(motionEvent.getX(), motionEvent.getY());
                    surface.invalidate();
                }
                else if (outilActif.equals("pipette")) {
                    outilActif = "pipette";
                    Bitmap bitmap = surface.getBitmapImage();
                    couleurActive = bitmap.getPixel((int)motionEvent.getX(), (int)motionEvent.getY());
                }
                else if (outilActif.equals("cercle") || outilActif.equals("rectangle")) {
                    arrivee = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                    surface.invalidate();
                }
                else if (outilActif.equals("triangle")) {
                    if (trianglePoints == 1) {
                        intermediaire = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                        surface.invalidate();
                    }
                    else if (trianglePoints == 3) {
                        arrivee = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                        triangleCourant = new Path(); // Important d'initialiser sans cesse un nouvel objet Path pour éviter un effet fantôme lors du traçage du triangle
                        surface.invalidate();
                    }

                }
            }

            else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                if (traceLibre != null) {
                    if (outilActif.equals("traceLibre")) {
                        TraceLibre trait = new TraceLibre(couleurActive, largeurActive, opaciteActive, traceLibre);
                        objetsDessin.add(trait);
                        traceLibre = null;
                    }
                    else if (outilActif.equals("efface")) {
                        Efface efface = new Efface(couleurFondActive, largeurActive, traceLibre);
                        objetsDessin.add(efface);
                        traceLibre = null;
                    }
                }
                else if (outilActif.equals("pipette")) {
                    afficheCouleurActive.setBackgroundColor(couleurActive);
                    outilActif = "traceLibre";
                }
                else if (outilActif.equals("cercle")) {
                    Cercle cercle = new Cercle(couleurActive, opaciteActive, depart.x, depart.y, arrivee.x, arrivee.y);
                    objetsDessin.add(cercle);
                }
                else if (outilActif.equals("rectangle")) {
                    Rectangle rectangle = new Rectangle(couleurActive, opaciteActive, depart.x, depart.y, arrivee.x, arrivee.y);
                    objetsDessin.add(rectangle);
                }
                else if (outilActif.equals("triangle")) {
                    if (trianglePoints == 1) {
                        trianglePoints++;
                        surface.invalidate();
                    }
                    else if (trianglePoints == 3) {
                        triangle = new Triangle(couleurActive, opaciteActive, depart.x, depart.y,
                                intermediaire.x, intermediaire.y, arrivee.x, arrivee.y);
                        objetsDessin.add(triangle);

                        surface.invalidate();

                        depart = null;
                        intermediaire = null;
                        arrivee = null;
                        trianglePoints = 0;
                    }
                }
                if (depart != null && arrivee != null) {
                    if (!outilActif.equals("triangle")) {
                        depart = null;
                        arrivee = null;
                    }
                }
            }

            return true;
        }
    }

    private class EcouteurOutils implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

        @Override
        public void onClick(View source) {
            // On supprime le dernier élément ajouté dans le vecteur d'objets Dessin

            if (source == undo && objetsDessin.size() > 0) {
                objetsDessin.remove(objetsDessin.size() - 1);
                surface.invalidate();
                traceLibre = null;
            }
            else if (source == pinceau) {
                outilActif = "traceLibre";
                Toast.makeText(MainActivity.this,
                        "Tracé libre.", Toast.LENGTH_SHORT).show();
            }
            else if (source == efface) {
                outilActif = "efface";
                Toast.makeText(MainActivity.this,
                        "Efface.", Toast.LENGTH_SHORT).show();
            }
            else if (source == pipette) {
                outilActif = "pipette";
                Toast.makeText(MainActivity.this,
                        "Pipette.", Toast.LENGTH_SHORT).show();
            }
            else if (source == cercle) {
                outilActif = "cercle";
                Toast.makeText(MainActivity.this,
                        "Cercle.", Toast.LENGTH_SHORT).show();
            }
            else if (source == rectangle) {
                outilActif = "rectangle";
                Toast.makeText(MainActivity.this,
                        "Rectangle.", Toast.LENGTH_SHORT).show();
            }
            else if (source == triangle) {
                outilActif = "triangle";
                Toast toast = Toast.makeText(MainActivity.this,
                        "Triangle.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
            else if (source == sauvegarder) {
                String msg = "Veuillez donner un nom à votre réalisation. Celle-ci sera sauvegardée au format PNG.";
                // Appel de la fonction sauvegarderImage() via la boîte de dialogue.
                boiteDialogue(msg, "Sauvegarder", false, true);
            }
            else if (source == remplir) {
                outilActif = "remplir";
            }
            else if (source == supprimer)  {
                String msg = "Vous vous apprêtez à supprimer votre image. Cette action est" +
                        " irréversible.";
                // Appel de la fonction supprimerSurface() via la boîte de dialogue.
                boiteDialogue(msg, "Supprimer", true, false);
            }
            else if (source == info) {
                String msg = "Réalisé par Déric Marchand\n\n" +
                        "Couleur personnalisée : cliquez sur l'afficheur de couleur active.\n\n" +
                        "O : Opacité.\n" +
                        "L : Largeur du trait.\n\n" +
                        "Prenez garde! La suppression d'une image à l'aide du bouton Supprimer est permanente.";
                boiteDialogue(msg, "DMPaint v1.0", false, false);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar == largeur) {
                largeurActive = progress;
                largeurTexte.setText(String.valueOf(largeurActive));
            }
            else if (seekBar == opacite) {
                opaciteActive = progress;
                opaciteTexte.setText(String.valueOf(opaciteActive));
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
            crayonPlein.setStyle(Paint.Style.FILL);
            crayonContour = new Paint(Paint.ANTI_ALIAS_FLAG);
            crayonContour.setStyle(Paint.Style.STROKE);
            crayonEfface = new Paint(Paint.ANTI_ALIAS_FLAG);
            crayonEfface.setStyle(Paint.Style.STROKE);
        }

        // Redéfinition de la méthode onDraw de la classe interne Surface
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // On récupère les paramètres utilisateur avant
            // de dessiner quoi que ce soit
            crayonContour.setColor(couleurActive);
            crayonPlein.setColor(couleurActive);
            crayonEfface.setColor(couleurFondActive);

            crayonContour.setStrokeWidth(largeurActive);
            crayonPlein.setStrokeWidth(largeurActive);
            crayonEfface.setStrokeWidth(largeurActive);

            // On s'assure de ne pas modifier l'opacité de l'outil Efface
            // car il doit demeurer à 255 en tous temps.
            crayonContour.setAlpha(opaciteActive);
            crayonPlein.setAlpha(opaciteActive);

            // On dessine les objets du vecteur d'objets Dessin en premier pour s'assurer que les tracés
            // libres et les formes se superposent aux dessins déjà réalisés.
            if (objetsDessin.size() > 0) {
                for (Dessin dessin : objetsDessin) {
                    if (dessin instanceof Efface) {
                        Paint efface = dessin.getCrayon();
                        efface.setColor(couleurFondActive);
                    }
                    dessin.dessiner(canvas);
                }
            }

            // Le reste de la méthode onDraw permet de dessiner en temps réel ce que
            // l'utilisateur réalise avant que ça ne soit ajouté au vecteur d'objets Dessin
            if (traceLibre != null) {
                if (outilActif.equals("efface")) {
                    canvas.drawPath(traceLibre, crayonEfface);
                }
                else {
                    canvas.drawPath(traceLibre, crayonContour);
                }
            }
            else if (outilActif.equals("cercle") | outilActif.equals("rectangle")) {
                if (depart != null && arrivee != null) {
                    if (outilActif.equals("cercle")) {
                        int deltaX = Math.abs(arrivee.x - depart.x) * Math.abs(arrivee.x - depart.x);
                        int deltaY = Math.abs(arrivee.y - depart.y) * Math.abs(arrivee.y - depart.y);
                        float rayon = (float) Math.sqrt(deltaX + deltaY);
                        canvas.drawCircle(depart.x, depart.y, rayon, crayonPlein);
                    }
                    else {
                        canvas.drawRect(depart.x, depart.y, arrivee.x, arrivee.y, crayonPlein);
                    }
                }
            }
            else if (outilActif.equals("triangle")) {
                if (intermediaire != null) {
                    if (arrivee == null) {
                        canvas.drawLine(depart.x, depart.y, intermediaire.x, intermediaire.y, crayonContour);
                    }
                    else {
                        // On dessine les segments du triangle et on le referme
                        triangleCourant.moveTo(depart.x, depart.y);
                        triangleCourant.lineTo(intermediaire.x, intermediaire.y);
                        triangleCourant.lineTo(arrivee.x, arrivee.y);

                        triangleCourant.close();
                        canvas.drawPath(triangleCourant, crayonPlein);
                    }
                }
            }

            surface.setBackgroundColor(couleurFondActive);
        }

        // On récupère la Surface de dessin sous forme de Bitmap
        public Bitmap getBitmapImage() {
            this.buildDrawingCache();
            Bitmap bitmapImage = Bitmap.createBitmap(this.getDrawingCache());
            this.destroyDrawingCache();

            return bitmapImage;
        }

        public void supprimerImage() {
            if (objetsDessin.size() > 0) {
                objetsDessin.clear();
            }
            couleurFondActive = Color.WHITE;
            surface.invalidate();
            traceLibre = null;
        }
    }

    public void sauvegarderImage(Context context, String filename) throws IOException {
        Bitmap image = surface.getBitmapImage();
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, filename + ".png");

        FileOutputStream outputStream = new FileOutputStream(file);
        image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        outputStream.getFD().sync();
        outputStream.close();

        MediaScannerConnection.scanFile(context, new String[] {file.getAbsolutePath()}, null, null);
    }

    public void boiteDialogueCouleur() {
        AmbilWarnaDialog b = new AmbilWarnaDialog(MainActivity.this, couleurActive, false, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                couleurActive = color;
                afficheCouleurActive.setBackgroundColor(couleurActive);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) { }
        });
        b.show();
    }

    public void boiteDialogue(String msg, String titre, boolean supprimer, boolean sauvegarder) {
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);

        b.setMessage(msg);
        b.setTitle(titre);

        // Pour supprimer l'image au complet (aucun retour arrière possible)
        if (supprimer) {
            b.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    surface.supprimerImage();

                   Toast.makeText(MainActivity.this,
                            "Image supprimée.", Toast.LENGTH_SHORT).show();
                }
            });
            b.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this,
                            "Action annulée.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Pour sauvegarder l'image dans le dossier Pictures de l'appareil
        else if (sauvegarder) {
            final EditText saisie = new EditText(this);
            b.setView(saisie);

            b.setPositiveButton("Sauvegarder", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    nomImage = saisie.getText().toString();
                    try {
                        sauvegarderImage(MainActivity.this, nomImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this,
                            "Image sauvegardée.", Toast.LENGTH_SHORT).show();

                }
            });
            b.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this,
                            "Action annulée.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        AlertDialog dialog = b.create();
        dialog.show();
    }

}