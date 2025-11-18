package com.ordonnancement.ui.controller;

import java.util.List;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.Process;
import com.ordonnancement.service.AppState;
import com.ordonnancement.ui.components.GanttPane;
import com.ordonnancement.util.ProcessUtils;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controleur pour l'affichage du digaramme de gantt par processeur
 */
public class GanttProcessorController extends Application {

    @FXML
    private ScrollPane scrollPane;

    private static List<Process> listeProcessus;


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(GanttProcessorController.class.getResource("/fxml/GanttProcessorView.fxml"));
        Scene scene = new Scene(loader.load()); //Mettre la vue dans la scene
        stage.setScene(scene); //Mettre la scene dans la pripmmary stage
        
        stage.show();

    }

    @FXML
    private void initialize() {

        this.listeProcessus = AppState.getInstance().getResultats().getListeProcessus();

        // Créer le GanttPane
        GanttPane ganttPane = new GanttPane();
        //On met le gantt dans une v box pour le centrer dedans
        VBox boite = new VBox(ganttPane);
        boite.setAlignment(Pos.CENTER);
        
        // Force la VBox à prendre au minimum la hauteur du ScrollPane pour permettre le centrage vertical même en fullscreen
        boite.minHeightProperty().bind(scrollPane.heightProperty());
        
        scrollPane.setContent(boite); //On met la boite dans le scroll
        scrollPane.setPannable(true);
        
        if(listeProcessus!= null){
            // Dessin
            drawPane("ROUND ROBIN", ganttPane);
        }
        

    }

    public void drawPane(String nomAlgo, GanttPane ganttPane) {

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

        ganttPane.dessinerGanttProcessor(allocations, dateFinMax, cpus);

    }

}
