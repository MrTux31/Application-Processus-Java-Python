package com.ordonnancement.view;

import java.util.Collections;
import java.util.List;

import com.ordonnancement.model.Allocation;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Objet graphique permettant de créer un gantt
 */

public class GanttCanvas extends Canvas {

    /**
     * Contstructeur
     * @param largeur : la largeur du canva
     * @param hauteur : l'hauteur du canva
     */
    public GanttCanvas(double largeur, double hauteur){
        super(largeur, hauteur);    
    }

    /**
     * Méthode permettant de dessiner le gantt par processeur
     * @param listeAllocations
     */
    public void dessinerGanttProcessor(List<Allocation> listeAllocations, int dateFinMax, List<String> listeCpus){
        Collections.sort(listeCpus); //Trier la liste des cpu dans l'ordre
        
        // Marges
        double margeGauche = 70;
        double margeDroite = 50;
        double margeHaut = 20;
        double margeBas = 60;

        // Calcul échelles
        double longueurTrait = getWidth() - margeGauche - margeDroite; //La largeur qu'il reste pour tracer le canva
        double hauteurTrait = getHeight() - margeHaut - margeBas; //La hauteur restante pour tracer le canva
        double xDebut = margeGauche;
        double xFin = xDebut + longueurTrait; //On part de la marge gauche et on ajoute toute la largeur disponible (avant la marge droite)
        double yDebut = getHeight() - margeBas; //On démarre juste au-dessus de la marge du bas
        double yFin = margeHaut; //On s'arrete avant la marge haut
        double espaceGraduationsX = longueurTrait / dateFinMax; //Echelle du temps, on calcule l'espace nécessaire entre chaque graudations 
        double espacesGraduationsY = hauteurTrait / listeCpus.size();
        //DESSIN DES AXES///
        dessinerAxes(xDebut, xFin, yDebut, yFin, dateFinMax,listeCpus,espaceGraduationsX, espacesGraduationsY);
        //////

        double padding = 5; // espace vertical entre les rectangles

        // Dessiner un rectangle pour chaque allocation
        for (Allocation a : listeAllocations) {
            //Récupérer indice du cpu de l'allocation dans la liste des CPU
            int indiceCPU = listeCpus.indexOf(a.getProcessor()); //trouver position graduation Y du processeur
            if (indiceCPU == -1) continue; // sécurité si aucun cpu n'est trouvé


            //Trouver position x de début exacte du rectangle (multiplier la date de debut par l'espace pour aller à la bonne graduation X)
            double x = xDebut + a.getDateDebutExecution() * espaceGraduationsX; //coordonée X du coin en haut à gauche du rectangle

            //Calcul du nombre de graduations nécessaires pour le rectangle (en X car largeur)
            double largeur = (a.getDateFinExecution() - a.getDateDebutExecution()) * espaceGraduationsX; 
            //Trouver position Y de début du rectangle (multiplier par indice du CPU pour aller a la bonne graduation Y ) (soustraction car on va vers le haut)
            double y = yDebut - espacesGraduationsY - indiceCPU * espacesGraduationsY + padding; //Coordonée Y du coin en haut à gauche du rectangle
            
            //Calcul du nombre de graduations nécessaires (en Y car Hauteur)
            double hauteur = espacesGraduationsY - 2 * padding; //-2 * padding pour laisser petit espace en haut et en bas
           
            //DESSIN DU RECTANGLE///
            dessinerRectangle(hauteur, largeur, x, y, a.getIdProcessus(), a.getProcessus().hashCode()); //Hash code pour avoir un "identifiant" unique du processus
           ///
        }


    }


    /**
     * Dessine un rectangle coloré et affiche le texte centré à l'intérieur.
     * 
     * @param hauteur Hauteur du rectangle en pixels.
     * @param largeur Largeur du rectangle en pixels.
     * @param x Coordonnée X du coin supérieur gauche du rectangle.
     * @param y Coordonnée Y du coin supérieur gauche du rectangle.
     * @param contenu Texte à afficher au centre du rectangle.
     * @param identifiantCouleur Entier utilisé pour générer une couleur unique du rectangle.
     */
    private void dessinerRectangle(double hauteur, double largeur, double x, double y, String contenu, int identifiantCouleur){
        GraphicsContext gc = this.getGraphicsContext2D(); //Objet permettant de dessiner sur le canva

        // Rectangle coloré
        gc.setFill(Color.hsb(identifiantCouleur % 360, 0.6, 0.8)); //Couleur en HSB (s'exprime de 0 à 360) 
        gc.fillRect(x, y, largeur, hauteur); //Dessiner le rectangle (juste la couleur)        // donc on prend reste division euclidienne de 360 pour avoir valeur dans intervalle)
        gc.setStroke(Color.BLACK); //Definir couleur contour rectangle
        gc.strokeRect(x, y, largeur, hauteur); //Tracer contour rectangle

        // Texte centé dans le rectangle centré

        //On crée un objet Text temporaire pour mesurer sa taille
        Text textNode = new Text(contenu);
        textNode.setFont(gc.getFont()); //On défini la police
        double textWidth = textNode.getLayoutBounds().getWidth(); //On récupère la largeur
        double textHeight = textNode.getLayoutBounds().getHeight(); //On récupère la hauteur
        
        //Calcul des coordonées du texte pour etre au centre du rectangle
        double textX = x + (largeur - textWidth) / 2;
        double textY = y + (hauteur + textHeight) / 2 - 2;
        //Couleur du texte
        gc.setFill(Color.BLACK);
        gc.fillText(contenu, textX, textY); //Ecrire dans le rectangle aux bonnes coordonnées


    }


    /**
     * Dessine les axes X et Y avec leurs graduations sur le Canvas.
     * 
     * @param xDebut Coordonnée X de départ de l’axe horizontal
     * @param xFin Coordonnée X de fin de l’axe horizontal
     * @param yDebut Coordonnée Y de l’axe horizontal et départ de l’axe vertical
     * @param yFin Coordonnée Y de fin de l’axe vertical
     * @param valeurGraduationXMax Valeur maximale des graduations en X
     * @param elementsGraduationsY Liste des étiquettes à afficher sur l’axe Y
     * @param espaceGraduationsX Espacement entre les graduations de l’axe X
     * @param espaceGraduationsY Espacement entre les graduations de l’axe Y
     */

    private void dessinerAxes(double xDebut, double xFin, double yDebut, double yFin, int valeurGraduationXMax, List<String> elementsGraduationsY, double espaceGraduationsX, double espaceGraduationsY) {
        
        GraphicsContext gc = this.getGraphicsContext2D(); //Objet permettant de dessiner sur le canva
        gc.clearRect(0, 0, getWidth(), getHeight()); // effacer tout

        //Couleur ligne
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        //Tracer axe des X 
        gc.strokeLine(xDebut, yDebut, xFin, yDebut);
        //Tracer axe des Y 
        gc.strokeLine(xDebut, yDebut, xDebut, yFin);

        //Police ecriture
        gc.setFont(Font.font("Arial", 12));
        //Couleur texte
        gc.setFill(Color.BLACK);

        //Y positif = vers le bas
        //Y negatif = vers le haut

        //Tracer graduations X
        for (int position = 0; position <= valeurGraduationXMax; position++) {
            double x = xDebut + position * espaceGraduationsX; //Position en X de la graduation (on multiplie la position par l'espace entre les graduations pour mettre à l'échelle) (on fait des + pour aller vers droite)
            gc.strokeLine(x, yDebut-5, x, yDebut+5); //Tracer la graduation à la position X et en faisant dépasser le trait
            gc.fillText(String.valueOf(position), x-3, yDebut+20); //Placer le numéro de la graduation en dessous
        }

        //Tracer graduations Y
        for (int position = 0; position < elementsGraduationsY.size(); position++) {
            double y = yDebut-espaceGraduationsY - position * espaceGraduationsY; //Position en Y de la graduation (on multiple la position par l'espace entre graduations pour mettre à l'echelle) (on fait des - pour aller vers le haut)
            gc.strokeLine(xDebut-5, y,xDebut+5, y); //Tracer le trait vers gauche et droite (la graduation)
            gc.fillText(elementsGraduationsY.get(position), xDebut-50, y+(espaceGraduationsY/2)); //Mettre le nom de l'élément a gauche
           
        }



    }


    

}
