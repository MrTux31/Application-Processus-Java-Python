package com.ordonnancement.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.gantt.IGanttTask;
import com.ordonnancement.model.gantt.impl.ProcessusTask;
import com.ordonnancement.service.AppState;
import com.ordonnancement.ui.components.GanttPane;
import com.ordonnancement.util.ProcessUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;

/**
 * Controleur pour l'affichage du diagramme de gantt par processus
 */
public class GanttProcessusController {

    // Chargement elements fxml
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ComboBox<String> algosComboBox;
    @FXML
    private ListView<String> listViewProcessus;
    @FXML
    private Label labelAlgo;
    @FXML
    private Label labelProcessus;
    @FXML
    private Label message;

    ///////
    private GanttPane ganttPane;
    private List<Process> listeProcessus; // La liste de tous les processus ordonnancés

    // Liste de tous les processus (ID) utilisés dans l'algo d'ordonnancement
    // courant
    private ObservableList<String> listeProcessusDisponibles = FXCollections.observableArrayList();

    // La liste de tous les processus actuellement sélectionnés dans la list view
    private ObservableList<String> listeProcessusSelectionnes = FXCollections.observableArrayList();

    /**
     * Initialise le contrôleur et configure les composants graphiques. Lance le
     * premier dessin du gantt
     */
    @FXML
    private void initialize() {
        try {
            this.listeProcessus = AppState.getInstance().getResultats().getListeProcessus();
            setupListView();
            setupGanttPane();
            setupComboBoxAlgos();
            selectDefaultAlgo();
            initialiserListeners();
        } catch (IllegalStateException e) {
            afficherMessage("Aucun résultat disponible.\nLancez d'abord un ordonnancement.");
            cacherElements();
        }
    }

    /**
     * Configure la ListView des Processus avec des CheckBox. Chaque
     * sélection/déselection met à jour la liste des processus sélectionnés et
     * redessine le Gantt.
     */
    private void setupListView() {
        listeProcessusDisponibles = FXCollections.observableArrayList();
        listViewProcessus.setItems(listeProcessusDisponibles);
        // Cell factory pour mettre des checkbox dans la list view
        listViewProcessus.setCellFactory(CheckBoxListCell.forListView(procId -> {
            BooleanProperty property = new SimpleBooleanProperty(listeProcessusSelectionnes.contains(procId));
            property.addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    listeProcessusSelectionnes.add(procId);
                } else {
                    listeProcessusSelectionnes.remove(procId);
                }
                drawPane(); // On redessine le gantt avec les nouveaux processus
            });
            return property;
        }));
    }

    /**
     * Configure la ComboBox des algorithmes disponibles. Récupère la liste des
     * algorithmes exécutés et les ajoute à la ComboBox.
     */
    private void setupComboBoxAlgos() {
        List<AlgoConfiguration> algos = ConfigurationManager.getInstance().getFileConfiguration().getListeAlgorithmes();
        ObservableList<String> nomAlgos = FXCollections.observableArrayList();
        for (AlgoConfiguration algo : algos) {
            nomAlgos.add(algo.getNomAlgorithme());
        }
        algosComboBox.setItems(nomAlgos);
    }

    /**
     * Formate l'ID du processus pour l'affichage et le tri.
     * Utilise un format "P01", "P02" pour garantir un tri alphabétique correct qui
     * correspond au tri numérique.
     */
    private String formatId(String id) {
        try {
            int val = Integer.parseInt(id);
            return String.format("P%02d", val); // P01, P02, P10...
        } catch (NumberFormatException e) {
            return "P" + id;
        }
    }

    /**
     * Met à jour les listes de Processus disponibles et sélectionnés pour l'algo
     * donné.
     *
     * @param nomAlgo Nom de l'algorithme courant
     */
    private void updateListeProcessus(String nomAlgo) {
        // Récupérer tous les IDs de processus qui ont des allocations pour cet algo
        Set<String> ensembleProcessus = new HashSet<>();
        for (Process p : listeProcessus) {
            List<Allocation> allocs = ProcessUtils.getAllocations(p, nomAlgo);
            if (allocs != null && !allocs.isEmpty()) {
                ensembleProcessus.add(formatId(p.getId()));
            }
        }
        List<String> procs = new ArrayList<>(ensembleProcessus);

        // Le tri alphabétique standard fonctionne maintenant grâce au formatage P01,
        // P02...
        Collections.sort(procs);

        // Réinitialiser le contenu des listes
        listeProcessusDisponibles.clear();
        listeProcessusSelectionnes.clear();
        listeProcessusDisponibles.setAll(procs);
        listeProcessusSelectionnes.setAll(procs); // sélection par défaut : tout coché
    }

    /**
     * Sélectionne l'algorithme par défaut (le premier de la liste) et
     * initialise la liste des Processus et les tâches Gantt associées.
     */
    private void selectDefaultAlgo() {
        if (!algosComboBox.getItems().isEmpty()) {
            String defaultAlgo = algosComboBox.getItems().get(0);
            this.algosComboBox.getSelectionModel().selectFirst();
            changerAlgo(defaultAlgo);
        }
    }

    /**
     * Change l'algorithme courant affiché dans le Gantt.
     *
     * @param nomAlgo Nom de l'algorithme à sélectionner
     */
    private void changerAlgo(String nomAlgo) {
        updateListeProcessus(nomAlgo);
        drawPane();
    }

    /**
     * Initialise les listeners pour les changements de sélection de
     * l'algorithme dans la checkbox.
     */
    private void initialiserListeners() {
        algosComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        changerAlgo(newValue);
                    }
                });
    }

    /**
     * Affiche un message centré dans le ScrollPane.
     *
     * @param message Texte du message à afficher
     */
    private void afficherMessage(String message) {
        this.message.setText(message);
        this.message.setMaxWidth(Double.MAX_VALUE);
        this.message.setAlignment(Pos.CENTER);
        this.message.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");
        scrollPane.setVisible(false);
        this.message.setVisible(true);
    }

    /**
     * Configure et initialise le GanttPane.
     */
    private void setupGanttPane() {
        ganttPane = new GanttPane();
        VBox boite = new VBox(ganttPane);
        boite.setAlignment(Pos.CENTER);
        boite.minHeightProperty().bind(scrollPane.heightProperty());
        scrollPane.setContent(boite);

        afficherGantt();
        scrollPane.setPannable(true);
    }

    /**
     * Affiche le GanttPane dans le ScrollPane.
     */
    private void afficherGantt() {
        scrollPane.setVisible(true);
        message.setVisible(false);
    }

    /**
     * Permet de lancer le dessin du diagramme de gantt
     */
    public void drawPane() {
        String nomAlgo = algosComboBox.getSelectionModel().getSelectedItem();
        if (nomAlgo == null)
            return;

        // Récupérer allocations pour l'algo
        List<Allocation> allocations = ProcessUtils.getAllocations(listeProcessus, nomAlgo);

        List<IGanttTask> tachesGantt = new ArrayList<>();
        int dateFinMax = 0;

        // Convertir allocations en ProcessusTask si le processus est sélectionné
        for (Allocation a : allocations) {
            String formattedId = formatId(a.getIdProcessus());
            if (listeProcessusSelectionnes.contains(formattedId)) {
                // On utilise directement ProcessusTask avec le constructeur personnalisé
                // pour passer l'ID formaté (P01, P02...) comme catégorie.
                IGanttTask tache = new ProcessusTask(a, formattedId);
                tachesGantt.add(tache);
            }
            if (a.getDateFinExecution() > dateFinMax) {
                dateFinMax = a.getDateFinExecution();
            }
        }

        if (tachesGantt.isEmpty()) {
            ganttPane.clear();
            if (allocations.isEmpty()) {
                afficherMessage(
                        "Aucune allocation trouvée pour cet algorithme.\nVérifiez vos paramètres ou relancez une exécution.");
            } else {
                afficherMessage("Aucun Processus sélectionné.");
            }
        } else {
            afficherGantt();
            // On dessine le gantt avec en taches les allocation, et en catégories les ID
            // Processus formatés.
            ganttPane.dessinerGanttProcessor(tachesGantt, dateFinMax, listeProcessusSelectionnes);
        }
    }

    /**
     * Permet de cacher la liste view et la combo box
     */
    private void cacherElements() {
        this.listViewProcessus.setVisible(false);
        this.algosComboBox.setVisible(false);
        this.labelAlgo.setVisible(false);
        this.labelProcessus.setVisible(false);
    }
}