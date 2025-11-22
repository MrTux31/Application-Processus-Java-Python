package com.ordonnancement.ui.controller;

import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.model.Process;
import com.ordonnancement.model.gantt.IGanttTask;
import com.ordonnancement.service.AppState;
import com.ordonnancement.service.gantt.GanttProcessorService;
import com.ordonnancement.ui.components.GanttPresenter;

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
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controleur pour l'affichage du diagramme de gantt par processeur
 */
public class GanttProcessorController extends Application {

    //Chargement elements fxml
    
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
    @FXML VBox vBoxGantts;

    ///////
    private List<GanttPresenter> listeGanttPresenters;
    private List<Process> listeProcessus; //La liste de tous les processus ordonnancés
    private ObservableList<String> listeCpusDisponibles; //Liste de tous les cpus utilisés dans l'algo d'ordonnancement courant
    private ObservableList<String> listeCpusSelectionnes; //La liste de tous les cpus actuellement sélectionnés dans la list view des cpus
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
            setupListeCpus();
            setupAllGanttPresenter(ganttService.getNomAlgosDisponibles());
            drawAllGantts();
            
            
        }catch(IllegalStateException e){
            afficherMessage("Aucun résultat disponible.\nLancez d'abord un ordonnancement.");
            cacherElements();
        }

    }

    /**
     * Met à jour les listes de CPUs disponibles et sélectionnés.
     *
     * @param cpus Liste des CPUs à afficher et sélectionner
     */
    private void setupListeCpus() {
        List<String> cpus = ganttService.getAllCpus();
        //Rénitialiser le contenu des listes
        listeCpusDisponibles.clear();
        listeCpusSelectionnes.clear();
        listeCpusDisponibles.setAll(cpus);         // met à jour la vue
        listeCpusSelectionnes.setAll(cpus);       // sélection par défaut : tout coché
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
        vBoxGantts.setVisible(false);
        this.message.setVisible(true);
        
    }

    
    /**
     * Affiche le GanttPane dans le ScrollPane et centre verticalement le
     * contenu.
     */
    private void afficherGantts() {
        vBoxGantts.setVisible(true);
        message.setVisible(false);
    }

    /**
     * Permet de mettre en place un GanttPresenter pour chaque
     * algo d'ordonnancement exécuté
     * @param listeAlgos
     */
    private void setupAllGanttPresenter(List<String> listeAlgos){
        listeGanttPresenters = new ArrayList<>();
        vBoxGantts.setAlignment(Pos.TOP_LEFT);
        for(String a : listeAlgos){
            GanttPresenter presenter = new GanttPresenter(a); //Créer le gantt presenter de l'algo
            vBoxGantts.getChildren().add(presenter); //Ajout du presenter dans la vbox
            listeGanttPresenters.add(presenter); //Ajout à l'array list
            
        }

    }

    /**
     * Permet de lancer le dessin des gantts sur les différents gantt presenters
     */
    public void drawAllGantts() {
        afficherGantts();
        if (listeCpusSelectionnes.isEmpty()) {
            afficherMessage("Aucun CPU sélectionné.");
        } 
        else{
            for(GanttPresenter presenter : listeGanttPresenters){
                String nomAlgo = presenter.getText(); //Récupérer le nom de l'algo
                ganttService.changerAlgo(nomAlgo); //Changer d'algo dans le service
                List<IGanttTask> tachesGantt = ganttService.getTachesGantt(); //Récupérer les différentes taches de l'algo
                int dateFinMax = ganttService.getDateFinMax(); //Récupérer la date de fin max des taches
                presenter.presentGantt(tachesGantt, dateFinMax, listeCpusSelectionnes); //On redessinne le gantt
            }
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


    /**
     * Configure la ListView des CPUs avec des CheckBox. Chaque
     * sélection/déselection met à jour la liste des CPUs sélectionnés et
     * redessine le Gantt.
     */
    private void setupListView() {
        listeCpusSelectionnes = FXCollections.observableArrayList();
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
                drawAllGantts();//On redessine tous les gantts avec les nouveaux cpus
            });
            return property;
        }));
    }


    

}
