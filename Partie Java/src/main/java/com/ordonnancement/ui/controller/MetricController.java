package com.ordonnancement.ui.controller;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.Metrics;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.service.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.*;

public class MetricController implements Initializable {

    // --- Graphiques & axes ---
    @FXML private BarChart<String, Number> barChartGauche;
    @FXML private CategoryAxis xAxisGauche;
    @FXML private NumberAxis yAxisGauche;

    @FXML private BarChart<String, Number> barChartDroite;
    @FXML private CategoryAxis xAxisDroite;
    @FXML private NumberAxis yAxisDroite;

    // --- Comboboxes ---
    @FXML private ComboBox<String> comboAlgoGauche;
    @FXML private ComboBox<String> comboAlgoDroite;

    // --- Labels ---
    @FXML private Label messageLabel;
    @FXML private Label labelAlgoDroite;
    @FXML private Label labelAlgoGauche;

    // --- Données internes ---
    private final Map<String, List<Metrics>> metricsParAlgorithme = new HashMap<>();
    private List<Resultats> listeResultats = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            try {
                listeResultats = List.of(AppState.getInstance().getResultats());
            } catch (Exception e) {
                listeResultats = new ArrayList<>();
            }

            construireMapMetricsParAlgo();

            List<String> algosTrouvés = new ArrayList<>(metricsParAlgorithme.keySet());
            Collections.sort(algosTrouvés);

            if (algosTrouvés.isEmpty()) {
                afficherMessage("Aucun résultat disponible.\nLancez d'abord un ordonnancement.");
                cacherLesGraphiques();
                return;
            } else if (algosTrouvés.size() == 1) {
                afficherUnSeulGraphique();
            } else {
                afficherDeuxGraphiques();
            }

            comboAlgoGauche.getItems().setAll(algosTrouvés);
            comboAlgoDroite.getItems().setAll(algosTrouvés);

            comboAlgoGauche.getSelectionModel().selectFirst();
            if (algosTrouvés.size() > 1)
                comboAlgoDroite.getSelectionModel().select(1);
            else
                comboAlgoDroite.getSelectionModel().selectFirst();

            // Listeners
            comboAlgoGauche.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirGraphiqueGauche());
            comboAlgoDroite.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirGraphiqueDroite());

            // Premier affichage
            rafraichirGraphiqueGauche();
            rafraichirGraphiqueDroite();

        } catch (Exception e) {
            afficherMessage("Erreur : impossible de charger les métriques.");
            cacherLesGraphiques();
        }
    }

    public void setResultats(List<Resultats> resultats) {
        this.listeResultats = (resultats == null) ? new ArrayList<>() : new ArrayList<>(resultats);
        construireMapMetricsParAlgo();

        List<String> algosTrouvés = new ArrayList<>(metricsParAlgorithme.keySet());
        Collections.sort(algosTrouvés);

        comboAlgoGauche.getItems().setAll(algosTrouvés);
        comboAlgoDroite.getItems().setAll(algosTrouvés);

        if (!algosTrouvés.isEmpty()) {
            comboAlgoGauche.getSelectionModel().selectFirst();
            comboAlgoDroite.getSelectionModel().select(Math.min(1, algosTrouvés.size() - 1));
        }

        rafraichirGraphiqueGauche();
        rafraichirGraphiqueDroite();
    }

    private void construireMapMetricsParAlgo() {
        metricsParAlgorithme.clear();
        for (Resultats res : listeResultats) {
            if (res == null || res.getListeMetrics() == null) continue;
            for (Metrics m : res.getListeMetrics()) {
                if (m == null || m.getNomAlgorithme() == null) continue;
                String cle = m.getNomAlgorithme().trim().toUpperCase();
                metricsParAlgorithme.computeIfAbsent(cle, x -> new ArrayList<>()).add(m);
            }
        }
    }

    private void rafraichirGraphiqueGauche() {
        remplirGraphique(barChartGauche, comboAlgoGauche.getValue());
    }

    private void rafraichirGraphiqueDroite() {
        remplirGraphique(barChartDroite, comboAlgoDroite.getValue());
    }

    private void remplirGraphique(BarChart<String, Number> chart, String nomAlgoDemande) {
        chart.getData().clear();
        if (nomAlgoDemande == null) return;

        String cle = nomAlgoDemande.trim().toUpperCase();
        List<Metrics> liste = metricsParAlgorithme.get(cle);
        if (liste == null || liste.isEmpty()) return;

        XYChart.Series<String, Number> serieMakespan = new XYChart.Series<>();
        serieMakespan.setName("Makespan");

        XYChart.Series<String, Number> serieAttente = new XYChart.Series<>();
        serieAttente.setName("Temps d'attente moyen");

        XYChart.Series<String, Number> serieReponse = new XYChart.Series<>();
        serieReponse.setName("Temps de réponse moyen");

        int indexScenario = 1;
        for (Metrics m : liste) {
            String scenarioLabel = "Scénario " + indexScenario;
            serieMakespan.getData().add(new XYChart.Data<>(scenarioLabel, m.getMakespan()));
            serieAttente.getData().add(new XYChart.Data<>(scenarioLabel, m.getTempsAttenteMoyen()));
            serieReponse.getData().add(new XYChart.Data<>(scenarioLabel, m.getTempsReponseMoyen()));
            indexScenario++;
        }

        chart.getData().addAll(serieMakespan, serieAttente, serieReponse);
        chart.setCategoryGap(15);
        chart.setBarGap(3);
        chart.setAnimated(false);
    }

    private void afficherMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setVisible(true);
            messageLabel.setAlignment(Pos.CENTER);
        }
    }

    private void cacherLesGraphiques() {
        barChartGauche.setVisible(false);
        barChartDroite.setVisible(false);
        comboAlgoGauche.setVisible(false);
        comboAlgoDroite.setVisible(false);
        if (labelAlgoDroite != null) labelAlgoDroite.setVisible(false);
        if (labelAlgoGauche != null) labelAlgoGauche.setVisible(false);
        messageLabel.setVisible(true);
    }

    private void afficherUnSeulGraphique() {
        barChartGauche.setVisible(true);
        comboAlgoGauche.setVisible(true);
        if (labelAlgoGauche != null) labelAlgoGauche.setVisible(true);

        barChartDroite.setVisible(false);
        comboAlgoDroite.setVisible(false);
        if (labelAlgoDroite != null) labelAlgoDroite.setVisible(false);

        messageLabel.setVisible(false);
    }

    private void afficherDeuxGraphiques() {
        barChartGauche.setVisible(true);
        comboAlgoGauche.setVisible(true);
        if (labelAlgoGauche != null) labelAlgoGauche.setVisible(true);

        barChartDroite.setVisible(true);
        comboAlgoDroite.setVisible(true);
        if (labelAlgoDroite != null) labelAlgoDroite.setVisible(true);

        messageLabel.setVisible(false);
    }
}
