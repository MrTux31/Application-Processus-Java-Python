package com.ordonnancement.ui.controller;

import java.util.List;

import com.ordonnancement.model.Process;
import com.ordonnancement.model.gantt.IGanttTask;
import com.ordonnancement.service.AppState;
import com.ordonnancement.service.gantt.GanttProcessorService;
import com.ordonnancement.ui.components.GanttPane;

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
    private ListView<String> listViewCpu;
    @FXML
    private Label labelAlgo;
    @FXML
    private Label labelCpu;
    @FXML
    private Label message;

    ///////
    private GanttPane ganttPane;
    private List<Process> listeProcessus; //La liste de tous les processus ordonnancés
    private ObservableList<String> listeCpusDisponibles = FXCollections.observableArrayList(); //Liste de tous les cpus utilisés dans l'algo d'ordonnancement courant
    private ObservableList<String> listeCpusSelectionnes = FXCollections.observableArrayList(); //La liste de tous les cpus actuellement sélectionnés dans la list view des cpus

    private GanttProcessorService ganttService;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(GanttProcessorController.class.getResource("/fxml/GanttProcessorView.fxml"));
        Scene scene = new Scene(loader.load()); //Mettre la vue dans la scene
        stage.setScene(scene); //Mettre la scene dans la pripmmary stage
        stage.show();

    }

    /**
     * Initialise le contrôleur et configure les composants graphiques. Lance le
     * premier dessin du gantt
     */
    @FXML
    private void initialize() {
        
        try {
            this.listeProcessus = AppState.getInstance().getResultats().getListeProcessus();
            this.ganttService = new GanttProcessorService(listeProcessus);
            setupListView();
            setupGanttPane();
            setupComboBoxAlgos();
            selectDefaultAlgo();
            initialiserListeners();
        }catch(IllegalStateException e){
            afficherMessage("Aucun résultat disponible.\nLancez d'abord un ordonnancement.");
            cacherElements();
        }

    }

    /**
     * Configure la ListView des CPUs avec des CheckBox. Chaque
     * sélection/déselection met à jour la liste des CPUs sélectionnés et
     * redessine le Gantt.
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
     * Configure la ComboBox des algorithmes disponibles. Récupère la liste des
     * algorithmes exécutés et les ajoute à la ComboBox.
     */
    private void setupComboBoxAlgos() {
        ObservableList<String> nomAlgos = FXCollections.observableArrayList();
        nomAlgos.addAll(ganttService.getNomAlgosDisponibles()); //On ajoute les noms d'algos disponibles
        algosComboBox.setItems(nomAlgos); //On met tout dans la comboBox
    }

    /**
     * Met à jour les listes de CPUs disponibles et sélectionnés.
     *
     * @param cpus Liste des CPUs à afficher et sélectionner
     */
    private void updateListeCpu() {
        List<String> cpus = ganttService.getCpusDisponibles();
        //Rénitialiser le contenu des listes
        listeCpusDisponibles.clear();
        listeCpusSelectionnes.clear();
        listeCpusDisponibles.setAll(cpus);         // met à jour la vue
        listeCpusSelectionnes.setAll(cpus);       // sélection par défaut : tout coché
    }

    /**
     * Sélectionne l'algorithme par défaut (le premier de la liste) et
     * initialise la liste des CPUs et les tâches Gantt associées.
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
     * Cette méthode met à jour le nom de l'algorithme courant, recharge la
     * liste des CPUs disponibles et sélectionnés pour cet algorithme, crée les
     * tâches Gantt correspondantes et redessine le diagramme.
     *
     * @param nomAlgo Nom de l'algorithme à sélectionner
     */
    private void changerAlgo(String nomAlgo) {
        this.ganttService.changerAlgo(nomAlgo); //On change le nom d'algo dans le service
        updateListeCpu(); //On met a jour la liste des CPU dispo
        drawPane(); //Redessiner le gantt

    }

    /**
     * Initialise les listeners pour les changements de sélection de
     * l'algorithme dans la checkbox. Met à jour la liste des CPUs, les tâches
     * Gantt et redessine le Gantt à chaque changement.
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
     * Affiche un message centré dans le ScrollPane.
     *
     * @param message Texte du message à afficher
     */
    private void afficherMessage(String message) {
        this.message.setText(message); //On crée le label texte
        this.message.setMaxWidth(Double.MAX_VALUE);   // Le Label prend toute la largeur
        this.message.setAlignment(Pos.CENTER);        // Centrage horizontal DU TEXTE
        this.message.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");
        scrollPane.setVisible(false);
        this.message.setVisible(true);
        
    }

    /**
     * Configure et initialise le GanttPane.
     */
    private void setupGanttPane() {
        // Créer le GanttPane
        ganttPane = new GanttPane();
        //On met le gantt dans une v box pour le centrer dedans
        VBox boite = new VBox(ganttPane);
        boite.setAlignment(Pos.CENTER);
        // Force la VBox à prendre au minimum la hauteur du ScrollPane pour permettre le centrage vertical même en fullscreen
        boite.minHeightProperty().bind(scrollPane.heightProperty());
        scrollPane.setContent(boite); //On met la boite dans le scroll
        
        afficherGantt();
        scrollPane.setPannable(true);
    }

    /**
     * Affiche le GanttPane dans le ScrollPane et centre verticalement le
     * contenu.
     */
    private void afficherGantt() {
        scrollPane.setVisible(true);
        message.setVisible(false);
    }

    /**
     * Permet de lancer le dessin du diagramme de gantt
     */
    public void drawPane() {
        List<IGanttTask> tachesGantt = ganttService.getTachesGantt();
        int dateFinMax = ganttService.getDateFinMax();
        if (tachesGantt == null || tachesGantt.isEmpty()) {
            ganttPane.clear();
            afficherMessage("Aucune allocation trouvée pour cet algorithme.\nVérifiez vos paramètres ou relancez une exécution.");
            
        }
        else if (listeCpusSelectionnes.isEmpty()) {
            ganttPane.clear();
            afficherMessage("Aucun CPU sélectionné.");
            

        } 
        else {
            afficherGantt(); //On met le gantt dans le scroll pane
            //On dessine le gantt avec en taches les allocation, et en catégories les ID Cpu.
            ganttPane.dessinerGanttProcessor(tachesGantt, dateFinMax, listeCpusSelectionnes);
        }

    }

    /**
     * Permet de cacher la liste view des cpus et la combo box des algos
     */
    private void cacherElements() {
        this.listViewCpu.setVisible(false);
        this.algosComboBox.setVisible(false);
        labelAlgo.setVisible(false); //Cacher les labels
        labelCpu.setVisible(false);
    }

}
