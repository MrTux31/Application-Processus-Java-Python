package com.ordonnancement.ui.controller;

import com.ordonnancement.model.Metrics;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.service.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.*;

public class MetricController implements Initializable {

    @FXML
    private LineChart<Number, Number> lineChartGauche;

    @FXML
    private NumberAxis xAxisGauche;

    @FXML
    private NumberAxis yAxisGauche;

    @FXML
    private LineChart<Number, Number> lineChartDroite;

    @FXML
    private NumberAxis xAxisDroite;

    @FXML
    private NumberAxis yAxisDroite;

    @FXML
    private ComboBox<String> comboAlgoGauche;

    @FXML
    private ComboBox<String> comboAlgoDroite;

    private final List<String> algorithmes =
            Arrays.asList("FIFO", "ROUND ROBIN", "PRIORITE");

    private final Map<String, List<Metrics>> metricsParAlgorithme = new HashMap<>();

    private List<Resultats> listeResultats = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboAlgoGauche.getItems().setAll(algorithmes);
        comboAlgoDroite.getItems().setAll(algorithmes);

        comboAlgoGauche.getSelectionModel().select("FIFO");
        comboAlgoDroite.getSelectionModel().select("ROUND ROBIN");

        comboAlgoGauche.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirGraphiqueGauche());
        comboAlgoDroite.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirGraphiqueDroite());

        xAxisGauche.setTickLabelsVisible(false);
        xAxisGauche.setTickMarkVisible(false);
        xAxisGauche.setOpacity(0);

        xAxisDroite.setTickLabelsVisible(false);
        xAxisDroite.setTickMarkVisible(false);
        xAxisDroite.setOpacity(0);

        // Charger automatiquement les résultats depuis AppState
        try {
            listeResultats = List.of(AppState.getInstance().getResultats());
        } catch (Exception e) {
            listeResultats = new ArrayList<>();
        }

        construireMapMetricsParAlgo();
        rafraichirGraphiqueGauche();
        rafraichirGraphiqueDroite();
    }


    public void setResultats(List<Resultats> resultats) {
        if (resultats == null)
            this.listeResultats = new ArrayList<>();
        else
            this.listeResultats = new ArrayList<>(resultats);

        construireMapMetricsParAlgo();
        rafraichirGraphiqueGauche();
        rafraichirGraphiqueDroite();
    }


    private void construireMapMetricsParAlgo() {
        metricsParAlgorithme.clear();

        for (Resultats res : listeResultats) {
            if (res == null || res.getListeMetrics() == null) {
                continue;
            }

            for (Metrics m : res.getListeMetrics()) {
                if (m == null || m.getNomAlgorithme() == null) continue;

                String cle = m.getNomAlgorithme().trim().toUpperCase();

                metricsParAlgorithme
                        .computeIfAbsent(cle, x -> new ArrayList<>())
                        .add(m);
            }
        }
    }


    private void rafraichirGraphiqueGauche() {
        remplirGraphique(lineChartGauche, comboAlgoGauche.getValue());
    }

    private void rafraichirGraphiqueDroite() {
        remplirGraphique(lineChartDroite, comboAlgoDroite.getValue());
    }


    private void remplirGraphique(LineChart<Number, Number> chart, String nomAlgoDemande) {
        chart.getData().clear();

        if (nomAlgoDemande == null) {
            return;
        }

        String cle = nomAlgoDemande.trim().toUpperCase();
        List<Metrics> liste = metricsParAlgorithme.get(cle);

        if (liste == null || liste.isEmpty()) {
            return;
        }

        XYChart.Series<Number, Number> serieMakespan = new XYChart.Series<>();
        serieMakespan.setName("Makespan");

        XYChart.Series<Number, Number> serieAttente = new XYChart.Series<>();
        serieAttente.setName("Temps d'attente moyen");

        XYChart.Series<Number, Number> serieReponse = new XYChart.Series<>();
        serieReponse.setName("Temps de réponse moyen");

        int indexScenario = 1;
        for (Metrics m : liste) {
            serieMakespan.getData().add(new XYChart.Data<>(indexScenario, m.getMakespan()));
            serieAttente.getData().add(new XYChart.Data<>(indexScenario, m.getTempsAttenteMoyen()));
            serieReponse.getData().add(new XYChart.Data<>(indexScenario, m.getTempsReponseMoyen()));
            indexScenario++;
        }

        chart.getData().addAll(serieMakespan, serieAttente, serieReponse);
    }
}
