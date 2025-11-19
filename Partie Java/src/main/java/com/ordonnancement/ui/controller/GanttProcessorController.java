package com.ordonnancement.ui.controller;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.CheckComboBox;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.gantt.IGanttTask;
import com.ordonnancement.model.gantt.impl.CpuTask;
import com.ordonnancement.service.AppState;
import com.ordonnancement.ui.components.GanttPane;
import com.ordonnancement.util.ProcessUtils;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controleur pour l'affichage du digaramme de gantt par processeur
 */
public class GanttProcessorController extends Application {

    @FXML
    private ScrollPane scrollPane;
    @FXML 
    private ComboBox<String> algosComboBox;
    @FXML
    private CheckComboBox<String> cpuComboBox;
    private GanttPane ganttPane;


    private List<Process> listeProcessus;
    private List<String> listeCpus;
    private List<IGanttTask> tachesGantt;
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
        this.listeProcessus = AppState.getInstance().getResultats().getListeProcessus();
        // Créer le GanttPane
        ganttPane = new GanttPane();
         //On met le gantt dans une v box pour le centrer dedans
        VBox boite = new VBox(ganttPane);
        boite.setAlignment(Pos.CENTER);
        
        // Force la VBox à prendre au minimum la hauteur du ScrollPane pour permettre le centrage vertical même en fullscreen
        boite.minHeightProperty().bind(scrollPane.heightProperty());
        
        scrollPane.setContent(boite); //On met la boite dans le scroll
        scrollPane.setPannable(true);

        remplirComboBoxAlgos();
        if (!algosComboBox.getItems().isEmpty()) {
            this.nomAlgo = algosComboBox.getItems().get(0);  //Récup le premier nom d'algo
            this.algosComboBox.getSelectionModel().selectFirst(); //Sélectionner le premier elt
            this.listeCpus = ProcessUtils.getAllCpus(listeProcessus, nomAlgo); 
            this.tachesGantt = initialiseGanttTask(); //Intitialiser les taches
        }
        remplirComboBoxCpu();
        initialiserListeners();
        drawPane(); //Faire le premier dessin

        

    }

    public void initialiserListeners(){
         // Écouter les changements de sélection
        algosComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                this.nomAlgo = newValue; //On récupère le nom du nouvel algo cliqué
                remplirComboBoxCpu();
                this.tachesGantt = initialiseGanttTask(); //On récupère la liste des allocations (de l'algo sélectionné) converties en gantt task
                drawPane(); //Redessiner le gantt
            }
        );

        cpuComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
            ObservableList<Integer> checkedIndices = cpuComboBox.getCheckModel().getCheckedIndices(); //Récupérer les indices sélectionnés de la combobox
            if (checkedIndices.isEmpty() && !listeCpus.isEmpty()) { //Si il n'y a aucun CPU sélectionné
                cpuComboBox.getCheckModel().check(0); // cocher automatiquement le premier élément (forcer 1 minimum)
            }
           
            // Récupère les CPU sélectionnés
            
            List<String> checkedCpus = new ArrayList<>();
            for (String cpu : cpuComboBox.getCheckModel().getCheckedItems()) {
                if (cpu != null) {       // On ignore les null
                    checkedCpus.add(cpu);
                }
            }
            this.listeCpus = checkedCpus;


            drawPane(); //Redessiner le gantt

        }
        );

        
    }

    public void remplirComboBoxAlgos(){
        List<AlgoConfiguration> algos = ConfigurationManager.getInstance().getFileConfiguration().getListeAlgorithmes(); //Récupération de la liste des algorithmes exécutés dans le singleton
        ObservableList<String> nomAlgos = FXCollections.observableArrayList();
        for(AlgoConfiguration algo : algos){ //On récup le nom de chaque algo exécuté
            nomAlgos.add(algo.getNomAlgorithme()); //Sauvegarde dans l'observable list
           
        }
        algosComboBox.setItems(nomAlgos); //On met tout dans la comboBox
    }

    public void remplirComboBoxCpu(){
        this.listeCpus = ProcessUtils.getAllCpus(listeProcessus, nomAlgo); 
        //Ajouter tous les CPUS dans la combobox
        cpuComboBox.getItems().setAll(listeCpus);
        // Cocher tous les CPU par défaut
        cpuComboBox.getCheckModel().checkAll();
    }


    public List<IGanttTask> initialiseGanttTask(){
        //Récupérer liste des allocations pour l'algo
        List<Allocation> allocations = ProcessUtils.getAllocations(listeProcessus, nomAlgo); //Liste qui va garder les allocations de TOUS les processus
        
        //Liste qui va stocker les taches du gantt
        List<IGanttTask> tachesGantt = new ArrayList<>();
        //Pour chaque allocation, conversion en IGanttTask
        for(Allocation a : allocations){
            if(listeCpus.contains(a.getProcessor())){
                IGanttTask tache = new CpuTask(a); //(CPU task car gantt par CPU en Y)
                tachesGantt.add(tache);
            }
            
        }
        return tachesGantt;

    }

    public void drawPane() {


        //Récupération de la date de fin max 
        int dateFinMax = 0;
        for (IGanttTask tache : tachesGantt) { //Pour chaque tache de la liste on cherche la date max
            if (tache.getDateFin() > dateFinMax) {
                dateFinMax = tache.getDateFin();
            }
        }

        if(this.listeProcessus != null && this.listeCpus !=null){
            //On dessine le gantt avec en taches les allocation, et en catégories les ID Cpu.
            ganttPane.dessinerGanttProcessor(tachesGantt, dateFinMax, listeCpus);
        }
       

    }

}
