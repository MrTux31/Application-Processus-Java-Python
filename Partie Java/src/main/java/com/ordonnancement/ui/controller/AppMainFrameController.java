package com.ordonnancement.ui.controller;


import java.util.List;

import com.ordonnancement.model.Resultats;
import com.ordonnancement.service.AppState;
import com.ordonnancement.ui.Alert.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
/**
 * Contrôleur principal de l'application.
 * Gère la fenêtre principale et la navigation entre les différentes fonctionnalités.
 */
public class AppMainFrameController {

    //Charger éléments de la vue FXML
    @FXML 
    StackPane mainContentPane; //Conteneur principal
    @FXML 
    MenuBar menuBar;
    @FXML
    Menu configMenu;
    @FXML
    Menu processMenu;
    @FXML
    Menu ganttMenu;
    @FXML
    Menu metricsMenu;
    @FXML
    MenuItem graphMetric;
    @FXML
    MenuItem ganttCpuMenu;
    @FXML
    MenuItem ganttProcessMenu;
    //////
    
    //Controleurs 
    GanttProcessorController ganttProcessorController;
    MetricController comparaisonController;
    private List<Resultats> listeResultats;

    public void setListeResultats(List<Resultats> resultats) {
    this.listeResultats = resultats;
    }


    /**
     * Affiche le gantt par cpu
     * 
     * @author ROMA Quentin
     */
    @FXML
    private void doAfficherGanttCPU() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GanttProcessorView.fxml")); // Préparer le
                                                                                                         // chargement
                                                                                                         // du FXML
            BorderPane ganttProcessor = loader.load(); // Charger le fxml
            mainContentPane.getChildren().setAll(ganttProcessor); // Mettre le border pane dans le stackpane
            ganttProcessorController = loader.getController(); // Récupérer le controleur de la vue

        } catch (Exception e) {

            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }

            // Récupérer le message réel de l'exception racine
            String cleanMessage = root.getMessage();
            if (cleanMessage == null || cleanMessage.isEmpty()) {
                cleanMessage = root.toString(); // fallback si pas de message
            }

            AlertUtils.showError(
                    "Erreur",
                    "Erreur affichange Gantt CPU :\n" + cleanMessage,
                    (Stage) mainContentPane.getScene().getWindow());

        }

    }

    /**
     * Affiche les graphiques de comparaison des algorithme d'ordonnacement
     * @author Antonin Le donné
     */
    @FXML
    private void doAfficherComparaisonAlgos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MetricView.fxml"));
            BorderPane comparaisonPane = loader.load();

            comparaisonPane.prefWidthProperty().bind(mainContentPane.widthProperty());
            comparaisonPane.prefHeightProperty().bind(mainContentPane.heightProperty());

            mainContentPane.getChildren().setAll(comparaisonPane);

            comparaisonController = loader.getController();

            comparaisonController.setResultats(
                List.of(AppState.getInstance().getResultats())
            );

        } catch (Exception e) {
            AlertUtils.showError(
                    "Erreur",
                    "Impossible d'ouvrir la comparaison des algorithmes :\n" + e.getMessage(),
                    (Stage) mainContentPane.getScene().getWindow()
            );
            e.printStackTrace();
        }
    }

}
