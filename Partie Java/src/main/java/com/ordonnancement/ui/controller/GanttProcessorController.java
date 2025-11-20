package com.ordonnancement.ui.controller;

import java.util.ArrayList;
import java.util.List;

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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controleur pour l'affichage du diagramme de gantt par processeur
 */
public class GanttProcessorController extends Application {
    //Chargement elements fxml
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ComboBox<String> algosComboBox;
    @FXML
    ListView<String> listViewCpu;
    @FXML
    Label labelAlgo;
    @FXML
    Label labelCpu;
    ///////
    private GanttPane ganttPane;

    private List<Process> listeProcessus; //La liste de tous les processus ordonnancés
    private ObservableList<String> listeCpusDisponibles = FXCollections.observableArrayList(); //Liste de tous les cpus utilisés dans l'algo d'ordonnancement courant
    private ObservableList<String> listeCpusSelectionnes = FXCollections.observableArrayList(); //La liste de tous les cpus actuellement sélectionnés dans la list view des cpus
    
    private List<IGanttTask> tachesGantt; //Les différentes taches de gantt à afficher
    private String nomAlgo; //Le nom de l'algo sélectionné dans la combobox
    private int dateFinMax; //La date de fin maximale des allocations à afficher sur le gantt

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(GanttProcessorController.class.getResource("/fxml/GanttProcessorView.fxml"));
        Scene scene = new Scene(loader.load()); //Mettre la vue dans la scene
        stage.setScene(scene); //Mettre la scene dans la pripmmary stage
        stage.show();

    }

    /**
     * Initialise le contrôleur et configure les composants graphiques.
     * Lance le premier dessin du gantt
     */

    @FXML
    private void initialize() {
        try { //On essaie de récup la liste des processus du résultat
            this.listeProcessus = AppState.getInstance().getResultats().getListeProcessus();
            setupListView();
            setupGanttPane();
            setupComboBoxAlgos();
            selectDefaultAlgo();
            initialiserListeners();
        } catch (IllegalStateException e) { //Si aucun résultat n'est encore arrivé (on a pas encore lancé l'exécution)
            afficherMessage("Aucun ordonnancement n'a encore été réalisé");
            cacherElements();

        }
        
    }

    

    /**
     * Configure la ComboBox des algorithmes disponibles.
     * Récupère la liste des algorithmes exécutés et les ajoute à la ComboBox.
     */
    private void setupComboBoxAlgos() {
        List<AlgoConfiguration> algos = ConfigurationManager.getInstance().getFileConfiguration().getListeAlgorithmes(); //Récupération de la liste des algorithmes exécutés dans le singleton
        ObservableList<String> nomAlgos = FXCollections.observableArrayList();
        for (AlgoConfiguration algo : algos) { //On récup le nom de chaque algo exécuté
            nomAlgos.add(algo.getNomAlgorithme()); //Sauvegarde dans l'observable list

        }
        algosComboBox.setItems(nomAlgos); //On met tout dans la comboBox
    }


    /**
     * Remplit la liste des CPUs utilisés pour l'algorithme courant.
     * Appelle updateListeCpu pour mettre à jour les listes observables.
     */
    private void remplirListeCpu() {
        //On charge la liste des cpus qui ont été utilisé dans l'algo d'ordonnacement
        List<String> cpus = ProcessUtils.getAllCpus(listeProcessus, nomAlgo);
        //Maj des listes cpus
        updateListeCpu(cpus);

    }

    /**
     * Met à jour les listes de CPUs disponibles et sélectionnés.
     * @param cpus Liste des CPUs à afficher et sélectionner
     */
    private void updateListeCpu(List<String> cpus) {
        //Rénitialiser le contenu des listes
        listeCpusDisponibles.clear();
        listeCpusSelectionnes.clear();

        listeCpusDisponibles.setAll(cpus);         // met à jour la vue
        listeCpusSelectionnes.setAll(cpus);       // sélection par défaut : tout coché
    }

    /**
     * Sélectionne l'algorithme par défaut (le premier de la liste)
     * et initialise la liste des CPUs et les tâches Gantt associées.
     */
    private void selectDefaultAlgo() {
        if (!algosComboBox.getItems().isEmpty()) {
            String defaultAlgo = algosComboBox.getItems().get(0);  //Récup le premier nom d'algo
            this.algosComboBox.getSelectionModel().selectFirst(); //Sélectionner le premier elt
            changerAlgo(defaultAlgo); //On effectue le changement d'algo
        }

    }
    /**
     * Change l'algorithme courant affiché dans le Gantt.
     * 
     * Cette méthode met à jour le nom de l'algorithme courant, recharge la liste
     * des CPUs disponibles et sélectionnés pour cet algorithme, crée les tâches
     * Gantt correspondantes et redessine le diagramme.
     *
     * @param nomAlgo Nom de l'algorithme à sélectionner
     */
    private void changerAlgo(String nomAlgo){
        this.nomAlgo = nomAlgo; //On récupère le nom du nouvel 
        remplirListeCpu();
        this.tachesGantt = createGanttTasks(); //On récupère la liste des allocations (de l'algo sélectionné) converties en gantt task
        drawPane(); //Redessiner le gantt

    }

    /**
     * Initialise les listeners pour les changements de sélection de l'algorithme dans la checkbox.
     * Met à jour la liste des CPUs, les tâches Gantt et redessine le Gantt à chaque changement.
     */
    private void initialiserListeners() {
        // Écouter les changements de sélection
        algosComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    changerAlgo(newValue); //On effectue le changement d'algo (newValue = le nouveau nom d'algo)
                }
        );

    }

    
    /**
     * Crée la liste des tâches Gantt à partir des allocations CPU de l'algorithme courant.
     * Met également à jour la date de fin maximale.
     *
     * @return Liste des tâches Gantt créées
     */
    private List<IGanttTask> createGanttTasks() {
        //Récupérer liste des allocations pour l'algo
        List<Allocation> allocations = ProcessUtils.getAllocations(listeProcessus, nomAlgo); //Liste qui va garder les allocations de TOUS les processus

        //Liste qui va stocker les taches du gantt
        tachesGantt = new ArrayList<>();

        dateFinMax = 0;
        //Pour chaque allocation, conversion en IGanttTask
        for (Allocation a : allocations) {
            if (listeCpusDisponibles.contains(a.getProcessor())) {
                IGanttTask tache = new CpuTask(a); //(CPU task car gantt par CPU en Y)
                tachesGantt.add(tache);
                //trouver la date de fin max
                if (a.getDateFinExecution() > dateFinMax) {
                    dateFinMax = a.getDateFinExecution();
                }
            }

        }
        return tachesGantt;

    }

    /**
     * Affiche le GanttPane dans le ScrollPane et centre verticalement le contenu.
     */
    private void afficherGantt() {
        //On met le gantt dans une v box pour le centrer dedans
        VBox boite = new VBox(ganttPane);
        boite.setAlignment(Pos.CENTER);
        // Force la VBox à prendre au minimum la hauteur du ScrollPane pour permettre le centrage vertical même en fullscreen
        boite.minHeightProperty().bind(scrollPane.heightProperty());
        scrollPane.setContent(boite); //On met la boite dans le scroll
    }

    /**
     * Permet de lancer le dessin du diagramme de gantt
     */
    public void drawPane() {
        // Si pas de processus OU pas de CPUs sélectionnés
        if (this.listeProcessus == null || this.listeProcessus.isEmpty()) {
            afficherMessage("Aucun processus disponible.\nVeuillez lancer une exécution.");
            ganttPane.clear();
        } else if (this.listeCpusSelectionnes == null || this.listeCpusSelectionnes.isEmpty()) {
            afficherMessage("Aucun CPU sélectionné.\nVeuillez en sélectionner au moins un.");
            ganttPane.clear();

        } // Si pas de tâches Gantt pour cet algo
        else if (this.tachesGantt == null || this.tachesGantt.isEmpty()) {
            afficherMessage("Aucune allocation trouvée pour cet algorithme.\nVérifiez vos paramètres ou relancez une exécution.");
            ganttPane.clear();

        } else {

            afficherGantt(); //On met le gantt dans le scroll pane
            //On dessine le gantt avec en taches les allocation, et en catégories les ID Cpu.
            ganttPane.dessinerGanttProcessor(tachesGantt, dateFinMax, listeCpusSelectionnes);
        }

    }

    /**
     * Affiche un message centré dans le ScrollPane.
     *
     * @param message Texte du message à afficher
     */
    private void afficherMessage(String message) {
        VBox boite = new VBox();
        boite.setAlignment(Pos.CENTER);
        boite.setMinHeight(scrollPane.getHeight()); //Centrage vertical
        boite.minHeightProperty().bind(scrollPane.heightProperty());
        Label texte = new Label(message); //On crée le label texte
        texte.setMaxWidth(Double.MAX_VALUE);   // Le Label prend toute la largeur
        texte.setAlignment(Pos.CENTER);        // Centrage horizontal DU TEXTE
        texte.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
        boite.getChildren().add(texte);//Mettre le label dans la vbox
        scrollPane.setContent(boite);
    }

    /**
     * Configure et initialise le GanttPane.
     */
    private void setupGanttPane() {
        // Créer le GanttPane
        ganttPane = new GanttPane();
        afficherGantt();
        scrollPane.setPannable(true);
    }

    /**
     * Configure la ListView des CPUs avec des CheckBox.
     * Chaque sélection/déselection met à jour la liste des CPUs sélectionnés et redessine le Gantt.
     */
    private void setupListView() {
        listeCpusDisponibles = FXCollections.observableArrayList(); //Créer une arraylist observable
        listViewCpu.setItems(listeCpusDisponibles);
        //Cell factory pour mettre des checkbox dans la list view
        listViewCpu.setCellFactory(CheckBoxListCell.forListView(cpu -> {
            BooleanProperty property = new SimpleBooleanProperty(listeCpusSelectionnes.contains(cpu)); //Booléen observable qui est true/false si le cpu sélectionné est présent dans la liste
            property.addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    listeCpusSelectionnes.add(cpu); //Si il est sélectionné on l'ajoute a la liste
                } else {
                    listeCpusSelectionnes.remove(cpu); //Sinon on l'enlève de la liste
                }
                drawPane(); //On redessine le gantt avec les nouveaux cpus
            });
            return property;
        }));
    }

    /**
     * Permet de cacher la liste view des cpus et la combo
     * box des algos
     */
    private void cacherElements(){
        this.listViewCpu.setVisible(false);
        this.algosComboBox.setVisible(false);
        labelAlgo.setVisible(false); //Cacher les labels
        labelCpu.setVisible(false);
    }

    

}
