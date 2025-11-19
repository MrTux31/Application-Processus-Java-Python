package com.ordonnancement.ui.controller;

import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.gantt.IGanttTask;
import com.ordonnancement.model.gantt.impl.CpuTask;
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
    private String nomAlgo;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(GanttProcessorController.class.getResource("/fxml/GanttProcessorView.fxml"));
        Scene scene = new Scene(loader.load()); //Mettre la vue dans la scene
        stage.setScene(scene); //Mettre la scene dans la pripmmary stage
        
        stage.show();

    }

    @FXML
    private void initialize() {
        this.nomAlgo = "ROUND ROBIN";
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
            List<IGanttTask> tachesGantt = initialiseGanttTask(); //On récupère la liste d'objets convertis en gantt task
            drawPane(tachesGantt, ganttPane); //Dessiner
        }
        

    }
    public List<IGanttTask> initialiseGanttTask(){
        //Récupérer liste des allocations pour l'algo
        List<Allocation> allocations = ProcessUtils.getAllocations(listeProcessus, nomAlgo); //Liste qui va garder les allocations de TOUS les processus
        
        //Liste qui va stocker les taches du gantt
        List<IGanttTask> tachesGantt = new ArrayList<>();
        //Pour chaque allocation, conversion en IGanttTask
        for(Allocation a : allocations){
            IGanttTask tache = new CpuTask(a); //(CPU task car gantt par CPU en Y)
            tachesGantt.add(tache);
        }
        return tachesGantt;

    }

    public void drawPane(List<IGanttTask> tachesGantt, GanttPane ganttPane) {


        //Récupération de la date de fin max 
        int dateFinMax = 0;
        for (IGanttTask tache : tachesGantt) { //Pour chaque tache de la liste on cherche la date max
            if (tache.getDateFin() > dateFinMax) {
                dateFinMax = tache.getDateFin();
            }
        }

        //Récupérer la liste des cpus (soit la liste des catégoies)
        List<String> cpus = ProcessUtils.getAllCpus(listeProcessus, nomAlgo);

        //On dessine le gantt avec en taches les allocation, et en catégories les ID Cpu.
        ganttPane.dessinerGanttProcessor(tachesGantt, dateFinMax, cpus);

    }

}
