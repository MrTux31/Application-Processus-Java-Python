package com.ordonnancement.ui.controller;

import java.util.List;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.util.ProcessUtils;
import com.ordonnancement.view.GanttCanvas;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

/**
 * Controleur pour l'affichage du digaramme de gantt par processeur
 */
public class GanttProcessorController extends Application {

    @FXML
    private ScrollPane scrollPane;

    private static List<Process> listeProcessus;

    public static void runApp(Resultats resultats) {
        listeProcessus = resultats.getListeProcessus();

        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Controller.class.getResource("/fxml/GanttProcessorView.fxml"));
        Scene scene = new Scene(loader.load()); //Mettre la vue dans la scene
        stage.setScene(scene); //Mettre la scene dans la pripmmary stage
        stage.show();

    }

    @FXML
    private void initialize() {
         

        // Récupérer les allocations et infos
        List<Allocation> allocations = ProcessUtils.getAllocations(listeProcessus, "ROUND ROBIN");
        int dateFinMax = allocations.stream()
                                    .mapToInt(Allocation::getDateFinExecution)
                                    .max()
                                    .orElse(0);
        List<String> cpus = ProcessUtils.getAllCpus(listeProcessus, "ROUND ROBIN");

        // Définir une taille de Canvas suffisante pour toutes les données
        double margeGauche = 70, margeDroite = 50, margeHaut = 20, margeBas = 60;
        double minEspaceX = 20; // largeur minimale par unité de temps
        double minEspaceY = 40; // hauteur minimale par CPU
        double largeurCanvas = margeGauche + dateFinMax * minEspaceX + margeDroite;
        double hauteurCanvas = margeHaut + cpus.size() * minEspaceY + margeBas;

        GanttCanvas ganttCanvas = new GanttCanvas(largeurCanvas,hauteurCanvas);
    
        scrollPane.setContent(ganttCanvas);
        scrollPane.setPannable(true);

        // Dessin initial
        drawCanva("ROUND ROBIN", ganttCanvas);


        // Redessiner quand la fenêtre change de taille
        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
        // On définit la taille du canvas = max(fenêtre visible, taille minimale)
        //On vérifie si la taille de la fenetre est plus grande que la taille minimale
        double largeur = Math.max(newVal.getWidth(), largeurCanvas); 
        double hauteur = Math.max(newVal.getHeight(), hauteurCanvas);
        ganttCanvas.setWidth(largeur);
        ganttCanvas.setHeight(hauteur);

        // Redessiner avec les nouvelles dimensions
        drawCanva("ROUND ROBIN", ganttCanvas);
    });

        

    

    }

    public void drawCanva(String nomAlgo, GanttCanvas ganttCanvas) {
        List<Allocation> allocations = ProcessUtils.getAllocations(listeProcessus, nomAlgo); //Liste qui va garder les allocations de TOUS les processus
        //Récupération de la date de fin max
        int dateFinMax = 0;
        for (Allocation a : allocations) {
            if (a.getDateFinExecution() > dateFinMax) {
                dateFinMax = a.getDateFinExecution();
            }
        }

        //Récupérer la liste des cpus
        List<String> cpus = ProcessUtils.getAllCpus(listeProcessus, nomAlgo); 

    
        ganttCanvas.dessinerGanttProcessor(allocations, dateFinMax, cpus);

    }

}
