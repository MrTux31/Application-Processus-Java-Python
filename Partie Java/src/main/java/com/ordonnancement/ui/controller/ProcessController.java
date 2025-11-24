package com.ordonnancement.ui.controller;

import java.util.List;
import java.util.Map;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.service.AppState;
import com.ordonnancement.ui.Alert.AlertUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Contrôleur pour l'affichage de la liste des processus et de leurs détails.
 * Affiche une ListView des processus à gauche et les détails du processus sélectionné à droite.
 */
public class ProcessController {
    @FXML
    private VBox detailsPane;
    @FXML
    private ListView<Process> listViewProcessus;
    @FXML
    private Label labelTitre;
    @FXML
    private VBox detailsContent;
    @FXML
    private Label labelId;
    @FXML
    private Label labelDateSoumission;
    @FXML
    private Label labelTempsExecution;
    @FXML
    private Label labelRequiredRam;
    @FXML
    private Label labelDeadline;
    @FXML
    private Label labelPriority;
    @FXML
    private VBox executionInfoContainer;

    private ObservableList<Process> listeProcessusObservable;
    private List<Process> listeProcessus;

    /**
     * Initialise le contrôleur et configure les composants graphiques.
     */
    @FXML
    private void initialize() {
        try {
            this.listeProcessus = AppState.getInstance().getResultats().getListeProcessus();
            setupListView();
            setupSelectionListener();
        } catch (IllegalStateException e) {
            afficherMessageErreur("Aucun résultat disponible.\nLancez d'abord un ordonnancement.");
        } catch (Exception e) {
            AlertUtils.showError(
                    "Erreur",
                    "Erreur lors du chargement des processus :\n" + e.getMessage(),
                    null);
            e.printStackTrace();
        }
    }

    /**
     * Configure la ListView avec la liste des processus.
     */
    private void setupListView() {
        listeProcessusObservable = FXCollections.observableArrayList(listeProcessus);
        listViewProcessus.setItems(listeProcessusObservable);
        
        // Personnaliser l'affichage des processus dans la ListView
        listViewProcessus.setCellFactory(param -> new javafx.scene.control.ListCell<Process>() {
            @Override
            protected void updateItem(Process process, boolean empty) {
                super.updateItem(process, empty);
                if (empty || process == null) {
                    setText(null);
                } else {
                    setText("Processus " + process.getId());
                }
            }
        });
    }

    /**
     * Configure l'écouteur de sélection pour afficher les détails du processus sélectionné.
     */
    private void setupSelectionListener() {
        listViewProcessus.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        afficherDetailsProcessus(newValue);
                    } else {
                        masquerDetails();
                    }
                });
    }

    /**
     * Affiche les détails du processus sélectionné.
     * 
     * @param process Le processus dont on veut afficher les détails
     */
    private void afficherDetailsProcessus(Process process) {
        // Afficher les informations générales
        labelId.setText(process.getId());
        labelDateSoumission.setText(String.valueOf(process.getDateSoumission()));
        labelTempsExecution.setText(String.valueOf(process.getTempsExecution()));
        labelRequiredRam.setText(String.valueOf(process.getRequiredRam()));
        labelDeadline.setText(String.valueOf(process.getDeadline()));
        labelPriority.setText(String.valueOf(process.getPriority()));

        // Afficher les informations d'exécution par algorithme
        afficherInformationsExecution(process);

        // Afficher la zone de détails
        labelTitre.setVisible(false);
        detailsContent.setVisible(true);
    }

    /**
     * Affiche les informations d'exécution du processus pour chaque algorithme.
     * 
     * @param process Le processus dont on veut afficher les informations d'exécution
     */
    private void afficherInformationsExecution(Process process) {
        executionInfoContainer.getChildren().clear();

        Map<String, ExecutionInfo> executions = process.getAllExecutions();
        Map<String, List<Allocation>> allocations = process.getAllAllocations();

        if (executions.isEmpty() && allocations.isEmpty()) {
            Label labelAucuneInfo = new Label("Aucune information d'exécution disponible.");
            labelAucuneInfo.setStyle("-fx-text-fill: gray;");
            executionInfoContainer.getChildren().add(labelAucuneInfo);
            return;
        }

        // Parcourir tous les algorithmes disponibles
        for (String nomAlgo : executions.keySet()) {
            VBox algoBox = new VBox(5);
            algoBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 10;");
            
            Label labelAlgo = new Label("Algorithme : " + nomAlgo);
            labelAlgo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
            algoBox.getChildren().add(labelAlgo);

            // Informations d'exécution
            ExecutionInfo execInfo = executions.get(nomAlgo);
            if (execInfo != null) {
                VBox execBox = new VBox(3);
                execBox.setPadding(new Insets(5, 0, 0, 10));
                
                HBox hboxDebut = new HBox(10);
                hboxDebut.getChildren().addAll(
                    new Label("Date de début :"),
                    new Label(String.valueOf(execInfo.getDateDebut()))
                );
                
                HBox hboxFin = new HBox(10);
                hboxFin.getChildren().addAll(
                    new Label("Date de fin :"),
                    new Label(String.valueOf(execInfo.getDateFin()))
                );
                
                HBox hboxRam = new HBox(10);
                hboxRam.getChildren().addAll(
                    new Label("RAM utilisée :"),
                    new Label(String.valueOf(execInfo.getUsedRam()))
                );
                
                execBox.getChildren().addAll(hboxDebut, hboxFin, hboxRam);
                algoBox.getChildren().add(execBox);
            }

            // Allocations
            List<Allocation> allocs = allocations.get(nomAlgo);
            if (allocs != null && !allocs.isEmpty()) {
                VBox allocBox = new VBox(3);
                allocBox.setPadding(new Insets(5, 0, 0, 10));
                
                Label labelAllocs = new Label("Allocations processeur (" + allocs.size() + ") :");
                labelAllocs.setStyle("-fx-font-weight: bold;");
                allocBox.getChildren().add(labelAllocs);
                
                for (Allocation alloc : allocs) {
                    HBox hboxAlloc = new HBox(10);
                    hboxAlloc.setPadding(new Insets(2, 0, 0, 10));
                    hboxAlloc.getChildren().addAll(
                        new Label("• " + alloc.getProcessor() + " : [" + 
                                 alloc.getDateDebutExecution() + " - " + 
                                 alloc.getDateFinExecution() + "]")
                    );
                    allocBox.getChildren().add(hboxAlloc);
                }
                
                algoBox.getChildren().add(allocBox);
            }

            executionInfoContainer.getChildren().add(algoBox);
        }
    }

    /**
     * Masque la zone de détails et affiche le message par défaut.
     */
    private void masquerDetails() {
        labelTitre.setVisible(true);
        detailsContent.setVisible(false);
    }

    /**
     * Affiche un message d'erreur dans la zone de détails.
     * 
     * @param message Le message d'erreur à afficher
     */
    private void afficherMessageErreur(String message) {
        labelTitre.setText(message);
        labelTitre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");
        labelTitre.setVisible(true);
        detailsContent.setVisible(false);
        listViewProcessus.setVisible(false);
    }
}

