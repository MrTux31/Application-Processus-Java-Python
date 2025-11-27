
package com.ordonnancement.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.ordonnancement.ui.components.GanttPresenter;
import com.ordonnancement.util.ProcessUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;

/**
 * Controleur pour l'affichage du diagramme de gantt par processus
 */
public class GanttProcessusController {

    // Chargement elements fxml
    @FXML
    private VBox vBoxParent;

    @FXML
    private ListView<String> listViewProcessus;
    @FXML
    private Label labelAlgo;
    @FXML
    private Label labelProcessus;
    @FXML
    private Label message;

    ///////
    private List<GanttPresenter> listeGanttPresenters;
    private VBox vBoxGantts;
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

            if (labelAlgo != null) {
                labelAlgo.setVisible(false);
                labelAlgo.setManaged(false);
            }

            setupListView();
            setupGanttContainer();

            List<String> algos = getAvailableAlgos();
            setupAllGanttPresenter(algos);

            // Initialiser la liste des processus disponibles (union de tous les processus
            // de tous les algos)
            updateListeProcessusGlobal(algos);

            drawAllGantts();

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
                drawAllGantts(); // On redessine le gantt avec les nouveaux processus
            });
            return property;
        }));
    }

    /**
     * Récupère la liste des noms des algorithmes disponibles.
     */
    private List<String> getAvailableAlgos() {
        List<AlgoConfiguration> algos = ConfigurationManager.getInstance().getFileConfiguration().getListeAlgorithmes();
        List<String> nomAlgos = new ArrayList<>();
        for (AlgoConfiguration algo : algos) {
            nomAlgos.add(algo.getNomAlgorithme());
        }
        return nomAlgos;
    }

    /**
     * Met à jour les listes de Processus disponibles et sélectionnés pour tous les
     * algos.
     */
    private void updateListeProcessusGlobal(List<String> algos) {
        Set<String> ensembleProcessus = new HashSet<>();

        for (String nomAlgo : algos) {
            for (Process p : listeProcessus) {
                List<Allocation> allocs = ProcessUtils.getAllocations(p, nomAlgo);
                if (allocs != null && !allocs.isEmpty()) {
                    ensembleProcessus.add(p.getId());
                }
            }
        }

        List<String> procs = new ArrayList<>(ensembleProcessus);
        Collections.sort(procs);

        // Réinitialiser le contenu des listes
        listeProcessusDisponibles.clear();
        listeProcessusSelectionnes.clear();
        listeProcessusDisponibles.setAll(procs);
        listeProcessusSelectionnes.setAll(procs); // sélection par défaut : tout coché
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
        vBoxParent.setVisible(false);
        this.message.setVisible(true);
    }

    /**
     * Configure le conteneur pour les GanttPresenters.
     */
    private void setupGanttContainer() {
        vBoxGantts = new VBox();
        vBoxGantts.setAlignment(Pos.TOP_LEFT);
        vBoxParent.getChildren().add(vBoxGantts);
        afficherGantt();
    }

    /**
     * Permet de mettre en place un GanttPresenter pour chaque
     * algo d'ordonnancement exécuté
     * 
     * @param listeAlgos
     */
    private void setupAllGanttPresenter(List<String> listeAlgos) {
        listeGanttPresenters = new ArrayList<>();
        vBoxGantts.getChildren().clear();
        for (String a : listeAlgos) {
            GanttPresenter presenter = new GanttPresenter(a); // Créer le gantt presenter de l'algo
            vBoxGantts.getChildren().add(presenter); // Ajout du presenter dans la vbox
            listeGanttPresenters.add(presenter); // Ajout à l'array list
            if (listeGanttPresenters.size() > 1) { // Si ce ne sont pas les premiers, on les fermes
                presenter.setExpanded(false);
            }
        }

    }

    /**
     * Affiche le ScrollPane contenant les Gantts.
     */
    private void afficherGantt() {
        vBoxParent.setVisible(true);
        message.setVisible(false);
    }

    /**
     * Permet de lancer le dessin des gantts sur les différents gantt presenters
     */
    public void drawAllGantts() {
        if (listeProcessusSelectionnes.isEmpty()) {
            afficherMessage("Aucun Processus sélectionné.");
            return;
        }

        afficherGantt();

        for (GanttPresenter presenter : listeGanttPresenters) {
            String nomAlgo = presenter.getText();
            List<IGanttTask> tachesGantt = new ArrayList<>();
            List<String> processusAffichesPourCetAlgo = new ArrayList<>(); // Liste filtrée pour cet algo
            int dateFinMax = 0;

            // Parcourir chaque processus pour récupérer ses allocations et calculer les
            // attentes
            for (Process p : listeProcessus) {
                String formattedId = p.getId();

                // Si le processus n'est pas sélectionné, on passe
                if (!listeProcessusSelectionnes.contains(formattedId)) {
                    continue;
                }

                List<Allocation> allocations = ProcessUtils.getAllocations(p, nomAlgo);

                // Si aucune allocation pour ce processus dans cet algo, on ne l'affiche pas
                // (filtre "inutiles")
                if (allocations == null || allocations.isEmpty()) {
                    continue;
                }

                // Si on a des allocations, on ajoute ce processus à la liste des catégories à
                // afficher pour cet algo
                processusAffichesPourCetAlgo.add(formattedId);

                // Trier les allocations par date de début pour calculer les trous (attentes)
                allocations.sort(Comparator.comparingInt(Allocation::getDateDebutExecution));

                int currentTime = p.getDateSoumission();

                for (Allocation a : allocations) {
                    // Ajouter une tâche d'attente s'il y a un trou entre le temps courant et le
                    // début de l'allocation
                    if (a.getDateDebutExecution() > currentTime) {
                        tachesGantt.add(
                                new ProcessusTask("EN ATTENTE", formattedId, currentTime, a.getDateDebutExecution()));
                    }

                    // Ajouter la tâche d'exécution (allocation)
                    tachesGantt.add(new ProcessusTask(a, formattedId));

                    // Mettre à jour le temps courant et la date de fin max
                    currentTime = a.getDateFinExecution();
                    if (a.getDateFinExecution() > dateFinMax) {
                        dateFinMax = a.getDateFinExecution();
                    }
                }
            }

            // On passe la liste filtrée (processusAffichesPourCetAlgo) au lieu de la liste
            // globale
            presenter.presentGantt(tachesGantt, dateFinMax, processusAffichesPourCetAlgo);
        }
    }

    /**
     * Permet de cacher la liste view et les labels
     */
    private void cacherElements() {
        this.listViewProcessus.setVisible(false);
        if (this.labelAlgo != null)
            this.labelAlgo.setVisible(false);
        this.labelProcessus.setVisible(false);
    }
}