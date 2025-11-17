package com.ordonnancement.view;

import java.util.Collections;
import java.util.List;

import com.ordonnancement.model.Allocation;

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
 * Objet graphique permettant de créer un gantt
 */
public class GanttPane extends BorderPane {

    private final double espacesGraduationsY = 50; // Hauteur de chaque ligne CPU
    private final double espacesGraduationsX = 40;
    

    /**
     * Dessine le diagramme de Gantt pour plusieurs CPU.
     *
     * @param listeAllocations Liste des allocations de processus (avec dateDebutExecution, dateFinExecution, CPU, idProcessus).
     * @param dateFinMax Temps maximal à afficher sur l'échelle de temps.
     * @param listeCpus Liste des CPU à afficher.
     */
    public void dessinerGanttProcessor(List<Allocation> listeAllocations, int dateFinMax, List<String> listeCpus) {

        this.getChildren().clear();
        Collections.sort(listeCpus); //Trier la liste des cpu dans l'ordre

        //Créer vertical box pour empiler les cpu 
        VBox lignesCPU = new VBox(5);
        for (String cpu : listeCpus) {
            HBox ligne = creerLigneCpu(cpu, listeAllocations, dateFinMax);
            lignesCPU.getChildren().add(ligne); //On ajoute la ligne des allocation de ce cpu
        }

        //Horizontal box qui stocke les labels des dates
        HBox echelleTemps = creerEchelleTemps(dateFinMax);
        echelleTemps.setPadding(new Insets(0, 0, 0, 80)); //Marge a gauche pour faire commencer graduation au bon endroit 

        this.setTop(echelleTemps); //L'échelle temps en haut du border pane
        this.setCenter(lignesCPU); //On ajoute ces lignes au centre du border pane

        

    }
    
    /**
     * Crée une ligne de Gantt pour un CPU donné.
     *
     * @param cpu Nom du CPU.
     * @param listeAllocations Liste des allocations de processus.
     * @param dateFinMax Temps maximal pour déterminer la largeur de la zone d'allocation.
     */

    private HBox creerLigneCpu(String cpu, List<Allocation> listeAllocations, int dateFinMax) {
        HBox ligne = new HBox();
        ligne.setAlignment(Pos.CENTER_LEFT); //Centré en haut a gauche
        ligne.setPrefHeight(espacesGraduationsY); //Hauteur de la boite
        ligne.setMinHeight(espacesGraduationsY);

        // Label du CPU à gauche
        Label labelCPU = new Label(cpu);
        labelCPU.setPrefWidth(80);
        labelCPU.setAlignment(Pos.CENTER); //Nom du cpu centré
        labelCPU.setPadding(new Insets(0, 10, 0, 0));
        labelCPU.setStyle("-fx-font-weight: bold;");

        //Utilisation d'un pane pour mettre le rectangle du processus dedans (pane permet de placer aux coordonnées voulues)
        Pane zoneAllocations = new Pane(); //Pane pour ajouter toutes les allocations dedans
        zoneAllocations.setPrefWidth((dateFinMax+1) * espacesGraduationsX); //On fait la bonne taille en longueur par rapport à l'échelle des graduations
                                                                            //+1 pour que le contour aille jusqu'a la fin
        zoneAllocations.setPrefHeight(espacesGraduationsY); //Hauteur su pane
        zoneAllocations.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;"); //bordure zone allocations

       

        //Pour chaque allocation dans la liste des allocations
        for (Allocation a : listeAllocations) {
            if (a.getProcessor().equals(cpu)) { //On prend les allocations qui sont celles sur le CPU actuel (en param)
                StackPane allocation = creerAllocation(a);
                zoneAllocations.getChildren().add(allocation); //Ajouter l'allocation créée dans le pane

            }
        }
        //Ajouter le label du cpu en face du pane contenant toutes les allocations
        ligne.getChildren().addAll(labelCPU,zoneAllocations); 
        return ligne;
    }



    /**
     * Crée un bloc représentant une allocation (rectangle + texte centré).
     *
     * @param a Allocation L'allocation processeur
     */

    private StackPane creerAllocation(Allocation a) {
        //On a un stackpane qui va contenir rectangle + son texte par dessus 


        StackPane stack = new StackPane(); //Stackpane pour empiler un rectangle et un texte dessus

        double x = a.getDateDebutExecution() * espacesGraduationsX; //Calcul de la position en X par rapport à l'échelle
        //On calcule pas Y etant donné qu'on est deja sur la bonne ligne CPU

        //On calcule l'espace en largeur dont on aura besoin pour l'allocation (le temps que l'alloc prends ramené à l'échelle)
        double largeur = Math.ceil(a.getDateFinExecution() - a.getDateDebutExecution()) * espacesGraduationsX; 
        stack.setLayoutX(x); //On place le stack pane a la bonne ordonnée
        
        stack.setPrefWidth(largeur); //Definition largeur
        stack.setPrefHeight(espacesGraduationsY); //Definition longueur

        //Création du rectangle
        Rectangle rectangle = new Rectangle(largeur, espacesGraduationsY-1); //-1 pour rentrer bien entre les lignes tracées (par zoneAllocations)
        //Couleur du rectangle 
        rectangle.setFill(Color.hsb(a.getProcessus().hashCode() % 360, 0.6, 0.8)); //Couleur en HSB (s'exprime de 0 à 360) 
        rectangle.setStroke(Color.BLACK); // Couleur de la bordure                                  donc on prend reste division euclidienne de 360 pour avoir valeur dans intervalle)
        rectangle.setStrokeWidth(1); //epaisseur trait
        
        // Label avec l'ID du processus
        Label labelProcessus = new Label(a.getIdProcessus());
        labelProcessus.setTextFill(Color.BLACK);


        stack.getChildren().addAll(rectangle,labelProcessus); //On empile le rectangle et le texte (au centre )
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

}
