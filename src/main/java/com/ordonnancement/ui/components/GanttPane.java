package com.ordonnancement.ui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ordonnancement.model.gantt.IGanttTask;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Objet graphique permettant de créer un Gantt
 * @author ROMA Quentin
 */
public class GanttPane extends BorderPane {

    private final double espacesGraduationsY = 60; // Hauteur de entre chaque élément en Y
    private final double espacesGraduationsX = 50; //Espace entre chaque éléments en X
    private final Map<String, Integer> idCouleurs; //Map pour référencer l'identifiant de chaque tache avec un id de couleur
    private final Map<String, List<IGanttTask>> tachesParCategorie = new HashMap<>(); //Map pour regrouper les catégories et leurs taches
    private int nextColor = 0; //Indice de la couleur suivante

    public GanttPane(){
        idCouleurs = new HashMap<>();
    }

    /**
     * Classe permettant de dessiner un diagramme de gantt
     *
     * @param listeTask : la liste des tâches du gantt a afficher en X
     * @param dateFinMax Temps maximal à afficher sur l'échelle de temps.
     * @param listeCategories : liste des catégories à afficher en Y
     */
    public void dessinerGanttProcessor(List<IGanttTask> listeTask, int dateFinMax, List<String> listeCategories) {
        this.getChildren().clear(); //Effacer tout
        
        initialiserMapCategories(listeTask); //On initialise un map qui associe à chaque catégorie sa liste de IGantTask (pour les récupérer plus facilement)
        initialiserCouleurs(listeTask); //On initialise le dico des couleurs pour chaque tache

        Collections.sort(listeCategories); //Trier la liste des catégories dans l'ordre (éléments affichés en Y)
        //Créer vertical box pour empiler les catégories 
        VBox lignesCategories = new VBox(5);
        for (String categorie : listeCategories) { //Pour chaque catégorie
            List<IGanttTask> tachesCategorie = tachesParCategorie.getOrDefault(categorie, Collections.emptyList()); //Récupérer les taches de la catégorie (si la catégorie existe pas, on crée une liste vide)
            HBox ligne = creerLigneTachesCategorie(categorie, tachesCategorie, dateFinMax); //Générer la ligne avec les taches
            lignesCategories.getChildren().add(ligne); //On ajoute la ligne des taches de cette cétagorie
        }

        //Horizontal box qui stocke les labels des dates
        HBox echelleTemps = creerEchelleTemps(dateFinMax);
        echelleTemps.setPadding(new Insets(0, 0, 0, 80)); //Marge a gauche pour faire commencer graduation au bon endroit 

        this.setTop(echelleTemps); //L'échelle temps en haut du border pane
        this.setCenter(lignesCategories); //On ajoute ces lignes au centre du border pane        

    }
    
    /**
     * Crée une ligne de Gantt pour une catégorie donnée.
     *
     * @param Nom de la catégorie.
     * @param listeTaches Liste des tâches de la catégorie
     * @param dateFinMax Temps maximal pour déterminer la largeur de la zone des tâches.
     */

    private HBox creerLigneTachesCategorie(String categorie, List<IGanttTask> listeTache, int dateFinMax) {
        HBox ligne = new HBox();
        ligne.setAlignment(Pos.CENTER_LEFT); //Centré en haut a gauche
        ligne.setPrefHeight(espacesGraduationsY); //Hauteur de la boite
        ligne.setMinHeight(espacesGraduationsY);

        // Label de la catégorie à gauche
        Label labelCategorie = new Label(categorie);
        labelCategorie.setPrefWidth(80);
        labelCategorie.setAlignment(Pos.CENTER); //Nom du de la catégorie centrée
        labelCategorie.setPadding(new Insets(0, 10, 0, 0));
        labelCategorie.setStyle("-fx-font-weight: bold;");

        //Utilisation d'un pane pour mettre le rectangle de la tache dedans (pane permet de placer aux coordonnées voulues)
        Pane zoneTaches = new Pane(); //Pane pour ajouter toutes les allocations dedans
        zoneTaches.setPrefWidth((dateFinMax+1) * espacesGraduationsX); //On fait la bonne taille en longueur par rapport à l'échelle des graduations
                                                                            //+1 pour que le contour aille jusqu'a la fin
        zoneTaches.setPrefHeight(espacesGraduationsY); //Hauteur su pane
        zoneTaches.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;"); //bordure zone allocations

       

        //Pour chaque allocation dans la liste des allocations
        for (IGanttTask t : listeTache) {
            StackPane tache = creerTache(t); //Récupérer la tache 
            zoneTaches.getChildren().add(tache); //Ajouter la tache créée dans le pane
        }
        //Ajouter le label de la catégorie en face du pane contenant toutes les taches
        ligne.getChildren().addAll(labelCategorie,zoneTaches); 
        return ligne;
    }



    /**
     * Crée un bloc représentant une tâche du gantt (rectangle + texte centré).
     *
     * @param tache La tache a convertir en bloc pour le  gantt
     */

    private StackPane creerTache(IGanttTask tache) {
        //On a un stackpane qui va contenir rectangle + son texte par dessus 


        StackPane stack = new StackPane(); //Stackpane pour empiler un rectangle et un texte dessus

        double x = tache.getDateDebut() * espacesGraduationsX; //Calcul de la position en X par rapport à l'échelle
        //On calcule pas Y etant donné qu'on est deja sur la bonne ligne de catégorie

        //On calcule l'espace en largeur dont on aura besoin pour la tache (le temps que la tache à etre réalisée ramené à l'échelle)
        double largeur = Math.ceil(tache.getDateFin() - tache.getDateDebut()) * espacesGraduationsX; 
        stack.setLayoutX(x); //On place le stack pane a la bonne ordonnée
        
        stack.setPrefWidth(largeur); //Definition largeur
        stack.setPrefHeight(espacesGraduationsY); //Definition longueur

        //Création du rectangle
        Rectangle rectangle = new Rectangle(largeur, espacesGraduationsY-1); //-1 pour rentrer bien entre les lignes tracées (par zoneTaches)
        //Couleur du rectangle
        int couleurTache = idCouleurs.get(tache.getId()); //On récup l'id couleur de la tache courante
        rectangle.setFill(Color.hsb(couleurTache*137 % 360, 0.6, 0.8)); //Couleur en HSB (s'exprime de 0 à 360) 
        rectangle.setStroke(Color.BLACK); // Couleur de la bordure                                  donc on prend reste division euclidienne de 360 pour avoir valeur dans intervalle)
        rectangle.setStrokeWidth(1); //epaisseur trait
        
        // Label avec l'ID du de la tache
        Label labelTache = new Label(tache.getId());
        labelTache.setTextFill(Color.BLACK);
        labelTache.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");



        stack.getChildren().addAll(rectangle,labelTache); //On empile le rectangle et le texte (au centre )
        return stack;

    }

    /**
     * Crée l'échelle de temps affichée en haut du Gantt.
     *
     * @param dateFinMax Temps maximal à afficher sur l'échelle (en unité de temps).
     */

    private HBox creerEchelleTemps(int dateFinMax){
        HBox ligne = new HBox();
        //Pour chaque date
        for(int t = 0; t <= dateFinMax; t++){
            Label temps = new Label(String.valueOf(t)); //On fait un label avec la valeur du temps
            temps.setPrefWidth(espacesGraduationsX); //On règle l'espace entre chaque date 
            temps.setAlignment(Pos.CENTER);
            temps.setStyle("-fx-font-size: 10px; -fx-border-color: lightgray; -fx-border-width: 0 1 0 1;");   
            ligne.getChildren().add(temps); //On ajoute le label avec la date correspondante dans la HBOX
        }
        return ligne;
    }


    private void initialiserMapCategories(List<IGanttTask> listeTask){
        for(IGanttTask tache : listeTask){ //Pour chaque tache dans la liste
            List<IGanttTask> liste;
            if (tachesParCategorie.containsKey(tache.getCategorie())) { //Si la catégorie est présente, récupérer la liste de taches associée à la catégorie
                liste = tachesParCategorie.get(tache.getCategorie());
            } else { //Si la catégorie de la tache n'est pas présente, créer une nouvelle liste
                liste = new ArrayList<>();
                tachesParCategorie.put(tache.getCategorie(), liste); //Associer la catégorie a la liste
            }
            liste.add(tache); //On ajoute la tache dans la liste
        }
        
        
    }


    private void initialiserCouleurs(List<IGanttTask> taches) {
        idCouleurs.clear();
        nextColor = 0;
        // Récupérer tous les IDs uniques et les trier
        Set<String> ids = new TreeSet<>(); //On les garde dans un ensemble qui les trie automatiquement en ordre croissant
        for (IGanttTask t : taches) {
            ids.add(t.getId());
        }
        // Assigner une couleur unique par ID
        for (String id : ids) {
            idCouleurs.put(id, nextColor++);
        }
    }



    /**
     * Permet d'effacer le gantt
     */
    public void clear() {
        this.setTop(null);
        this.setBottom(null);
        this.setLeft(null);
        this.setRight(null);
        this.setCenter(null);
    }


}


