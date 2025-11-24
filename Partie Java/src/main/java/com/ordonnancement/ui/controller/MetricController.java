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
    @FXML
    private BarChart<String, Number> barChartGauche;

    @FXML
    private CategoryAxis xAxisGauche;

    @FXML
    private NumberAxis yAxisGauche;

    @FXML
    private BarChart<String, Number> barChartDroite;

    @FXML
    private CategoryAxis xAxisDroite;

    @FXML
    private NumberAxis yAxisDroite;

    // --- Comboboxes ---
    @FXML
    private ComboBox<String> comboAlgoGauche;

    @FXML
    private ComboBox<String> comboAlgoDroite;

    // --- Labels ---
    @FXML
    private Label messageLabel;

    @FXML
    private Label labelAlgoDroite;

    private final List<String> algorithmes =
            Arrays.asList("FIFO", "ROUND ROBIN", "PRIORITE");

    private final Map<String, List<Metrics>> metricsParAlgorithme = new HashMap<>();
    private List<Resultats> listeResultats = new ArrayList<>();

    // --------------------------------------------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            int nbAlgos = trouverAlgosDisponibles();

            if (nbAlgos == 0) {
                afficherMessage("Aucun résultat disponible.\nLancez d'abord un ordonnancement.");
                cacherLesGraphiques();
                return;
            } else if (nbAlgos == 1) {
                afficherUnSeulGraphique();
            } else {
                afficherDeuxGraphiques();
            }

            comboAlgoGauche.getItems().setAll(algorithmes);
            comboAlgoDroite.getItems().setAll(algorithmes);

            comboAlgoGauche.getSelectionModel().select("FIFO");
            comboAlgoDroite.getSelectionModel().select("ROUND ROBIN");

            comboAlgoGauche.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirGraphiqueGauche());
            comboAlgoDroite.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirGraphiqueDroite());

            xAxisGauche.setTickLabelsVisible(true);
            xAxisGauche.setTickMarkVisible(true);
            xAxisGauche.setOpacity(1);

            xAxisDroite.setTickLabelsVisible(true);
            xAxisDroite.setTickMarkVisible(true);
            xAxisDroite.setOpacity(1);

            try {
                listeResultats = List.of(AppState.getInstance().getResultats());
            } catch (Exception e) {
                listeResultats = new ArrayList<>();
            }

            construireMapMetricsParAlgo();
            rafraichirGraphiqueGauche();
            rafraichirGraphiqueDroite();

        } catch (Exception e) {
            afficherMessage("Erreur : impossible de charger les métriques.");
            cacherLesGraphiques();
        }
    }

    // --------------------------------------------------------
    public void setResultats(List<Resultats> resultats) {
        this.listeResultats = (resultats == null) ? new ArrayList<>() : new ArrayList<>(resultats);
        construireMapMetricsParAlgo();
        rafraichirGraphiqueGauche();
        rafraichirGraphiqueDroite();
    }

    // --------------------------------------------------------
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

    // --------------------------------------------------------
    private int trouverAlgosDisponibles() {
        try {
            List<AlgoConfiguration> algos = ConfigurationManager
                    .getInstance()
                    .getFileConfiguration()
                    .getListeAlgorithmes();

            if (algos == null) return 0;

            Set<String> algosDispos = new HashSet<>();
            for (AlgoConfiguration algo : algos) {
                if (algo != null && algo.getNomAlgorithme() != null)
                    algosDispos.add(algo.getNomAlgorithme().trim().toUpperCase());
            }

            return algosDispos.size();
        } catch (Exception e) {
            return 0;
        }
    }

    // --------------------------------------------------------
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
        messageLabel.setVisible(true);
    }

    private void afficherUnSeulGraphique() {
        barChartGauche.setVisible(true);
        comboAlgoGauche.setVisible(true);
        barChartDroite.setVisible(false);
        comboAlgoDroite.setVisible(false);
        if (labelAlgoDroite != null) labelAlgoDroite.setVisible(false);
        messageLabel.setVisible(false);
    }

    private void afficherDeuxGraphiques() {
        barChartGauche.setVisible(true);
        comboAlgoGauche.setVisible(true);
        barChartDroite.setVisible(true);
        comboAlgoDroite.setVisible(true);
        if (labelAlgoDroite != null) labelAlgoDroite.setVisible(true);
        messageLabel.setVisible(false);
    }
}
