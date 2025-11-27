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

/**
 * Contrôleur responsable de l'affichage et de la comparaison
 * des performances des algorithmes d’ordonnancement.
 *
 * Ce contrôleur gère deux graphiques à barres (gauche et droite)
 * permettant de visualiser les métriques issues des différents algorithmes :
 *      Makespan (temps total d’exécution)
 *      Temps d’attente moyen
 *      Temps de réponse moyen
 * 
 * Il permet de comparer les résultats de plusieurs algorithmes
 * grâce aux comboboxes et s’appuie sur les données stockées dans AppState.
 *
 * @author Antonin Le Donné
 */
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

    /**
     * Méthode appelée automatiquement à l’initialisation du contrôleur FXML.
     * 
     * Elle charge les résultats stockés dans AppState, construit la map
     * des métriques par algorithme, initialise les ComboBox et affiche les graphiques.
     *
     * @param location  URL de localisation (non utilisée ici)
     * @param resources Ressources pour la localisation (non utilisées ici)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Chargement des résultats sauvegardés dans l’état global
            try {
                listeResultats = List.of(AppState.getInstance().getResultats());
            } catch (Exception e) {
                // En cas d’erreur ou d’absence de données
                listeResultats = new ArrayList<>();
            }

            // Construction de la map (clé = algo, valeur = liste des métriques)
            construireMapMetricsParAlgo();

            // Récupération et tri alphabétique des noms d'algorithmes
            List<String> algosTrouvés = new ArrayList<>(metricsParAlgorithme.keySet());
            Collections.sort(algosTrouvés);

            // Cas : aucun résultat trouvé → afficher message et cacher les graphiques
            if (algosTrouvés.isEmpty()) {
                afficherMessage("Aucun résultat disponible.\nLancez d'abord un ordonnancement.");
                cacherLesGraphiques();
                return;
            }
            // Cas : un seul algorithme → un seul graphique
            else if (algosTrouvés.size() == 1) {
                afficherUnSeulGraphique();
            }
            // Cas : plusieurs algorithmes → deux graphiques côte à côte
            else {
                afficherDeuxGraphiques();
            }

            // Remplissage des combobox avec les noms d’algorithmes
            comboAlgoGauche.getItems().setAll(algosTrouvés);
            comboAlgoDroite.getItems().setAll(algosTrouvés);

            // Sélection par défaut : premier à gauche, second à droite (si possible)
            comboAlgoGauche.getSelectionModel().selectFirst();
            if (algosTrouvés.size() > 1)
                comboAlgoDroite.getSelectionModel().select(1);
            else
                comboAlgoDroite.getSelectionModel().selectFirst();

            // Listeners : mise à jour automatique des graphiques lors du changement d’algo
            comboAlgoGauche.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirGraphiqueGauche());
            comboAlgoDroite.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirGraphiqueDroite());

            // Premier affichage des graphiques dès l’ouverture
            rafraichirGraphiqueGauche();
            rafraichirGraphiqueDroite();

        } catch (Exception e) {
            // Gestion d’erreur générale : affichage d’un message à l’utilisateur
            afficherMessage("Erreur : impossible de charger les métriques.");
            cacherLesGraphiques();
        }
    }

    /**
     * Met à jour les résultats affichés dans les graphiques.
     *
     * @param resultats Liste de résultats à afficher (peut être null)
     */
    public void setResultats(List<Resultats> resultats) {
        // Copie défensive de la liste pour éviter les effets de bord
        this.listeResultats = (resultats == null) ? new ArrayList<>() : new ArrayList<>(resultats);
        construireMapMetricsParAlgo();

        // Recalcule la liste des algorithmes disponibles
        List<String> algosTrouvés = new ArrayList<>(metricsParAlgorithme.keySet());
        Collections.sort(algosTrouvés);

        // Met à jour les combobox
        comboAlgoGauche.getItems().setAll(algosTrouvés);
        comboAlgoDroite.getItems().setAll(algosTrouvés);

        // Sélectionne par défaut les deux premiers (ou le seul disponible)
        if (!algosTrouvés.isEmpty()) {
            comboAlgoGauche.getSelectionModel().selectFirst();
            comboAlgoDroite.getSelectionModel().select(Math.min(1, algosTrouvés.size() - 1));
        }

        // Rafraîchit les graphiques
        rafraichirGraphiqueGauche();
        rafraichirGraphiqueDroite();
    }

    /**
     * Construit la map des métriques regroupées par nom d’algorithme.
     * Ignore les valeurs nulles et formate les clés en majuscules.
     */
    private void construireMapMetricsParAlgo() {
        metricsParAlgorithme.clear(); // Réinitialise les données précédentes
        for (Resultats res : listeResultats) {
            if (res == null || res.getListeMetrics() == null) continue;
            for (Metrics m : res.getListeMetrics()) {
                if (m == null || m.getNomAlgorithme() == null) continue;

                // Normalisation du nom d’algorithme (majuscule sans espaces)
                String cle = m.getNomAlgorithme().trim().toUpperCase();

                // Ajoute la métrique dans la liste correspondante (créée si absente)
                metricsParAlgorithme.computeIfAbsent(cle, x -> new ArrayList<>()).add(m);
            }
        }
    }

    /**
     * Rafraîchit le graphique de gauche en fonction de la sélection courante.
     */
    private void rafraichirGraphiqueGauche() {
        // Recharge le graphique avec les données du nouvel algo sélectionné
        remplirGraphique(barChartGauche, comboAlgoGauche.getValue());
    }

    /**
     * Rafraîchit le graphique de droite en fonction de la sélection courante.
     */
    private void rafraichirGraphiqueDroite() {
        // Recharge le graphique avec les données du nouvel algo sélectionné
        remplirGraphique(barChartDroite, comboAlgoDroite.getValue());
    }

    /**
     * Remplit un graphique à barres avec les données d’un algorithme donné.
     *
     * @param chart           Le graphique à remplir
     * @param nomAlgoDemande  Le nom de l’algorithme dont on veut afficher les métriques
     */
    private void remplirGraphique(BarChart<String, Number> chart, String nomAlgoDemande) {
        chart.getData().clear(); // Supprime les anciennes données
        if (nomAlgoDemande == null) return;

        // Récupère la clé (nom d’algo) et la liste de métriques associées
        String cle = nomAlgoDemande.trim().toUpperCase();
        List<Metrics> liste = metricsParAlgorithme.get(cle);
        if (liste == null || liste.isEmpty()) return;

        // Trois séries : Makespan / Attente / Réponse
        XYChart.Series<String, Number> serieMakespan = new XYChart.Series<>();
        serieMakespan.setName("Makespan");

        XYChart.Series<String, Number> serieAttente = new XYChart.Series<>();
        serieAttente.setName("Temps d'attente moyen");

        XYChart.Series<String, Number> serieReponse = new XYChart.Series<>();
        serieReponse.setName("Temps de réponse moyen");

        // Ajout des points de données (un par scénario)
        int indexScenario = 1;
        for (Metrics m : liste) {
            String scenarioLabel = "Scénario " + indexScenario;

            // Ajout des trois valeurs correspondantes
            serieMakespan.getData().add(new XYChart.Data<>(scenarioLabel, m.getMakespan()));
            serieAttente.getData().add(new XYChart.Data<>(scenarioLabel, m.getTempsAttenteMoyen()));
            serieReponse.getData().add(new XYChart.Data<>(scenarioLabel, m.getTempsReponseMoyen()));

            indexScenario++;
        }

        // Ajoute les séries au graphique et règle son apparence
        chart.getData().addAll(serieMakespan, serieAttente, serieReponse);
        chart.setCategoryGap(15);  // Espacement entre les groupes de barres
        chart.setBarGap(3);        // Espacement entre les barres d’un même groupe
        chart.setAnimated(false);  // Désactive l’animation pour une mise à jour instantanée
    }

    /**
     * Affiche un message dans la zone prévue à cet effet.
     *
     * @param message Texte du message à afficher
     */
    private void afficherMessage(String message) {
        if (messageLabel != null) {
            // Met à jour le texte et centre le message
            messageLabel.setText(message);
            messageLabel.setVisible(true);
            messageLabel.setAlignment(Pos.CENTER);
        }
    }

    /**
     * Cache tous les graphiques et éléments associés.
     * Utilisé lorsqu’aucune donnée n’est disponible ou en cas d’erreur.
     */
    private void cacherLesGraphiques() {
        // Masque les deux graphiques et leurs comboboxes
        barChartGauche.setVisible(false);
        barChartDroite.setVisible(false);
        comboAlgoGauche.setVisible(false);
        comboAlgoDroite.setVisible(false);

        // Masque les labels s’ils existent
        if (labelAlgoDroite != null) labelAlgoDroite.setVisible(false);
        if (labelAlgoGauche != null) labelAlgoGauche.setVisible(false);

        // Affiche uniquement le message d’information
        messageLabel.setVisible(true);
    }

    /**
     * Configure l’affichage pour un seul graphique (cas d’un seul algorithme).
     */
    private void afficherUnSeulGraphique() {
        // Active uniquement le graphique et la combobox de gauche
        barChartGauche.setVisible(true);
        comboAlgoGauche.setVisible(true);
        if (labelAlgoGauche != null) labelAlgoGauche.setVisible(true);

        // Désactive le graphique de droite
        barChartDroite.setVisible(false);
        comboAlgoDroite.setVisible(false);
        if (labelAlgoDroite != null) labelAlgoDroite.setVisible(false);

        messageLabel.setVisible(false);
    }

    /**
     * Configure l’affichage pour deux graphiques (comparaison entre deux algorithmes).
     */
    private void afficherDeuxGraphiques() {
        // Affiche les deux graphiques et leurs comboboxes
        barChartGauche.setVisible(true);
        comboAlgoGauche.setVisible(true);
        if (labelAlgoGauche != null) labelAlgoGauche.setVisible(true);

        barChartDroite.setVisible(true);
        comboAlgoDroite.setVisible(true);
        if (labelAlgoDroite != null) labelAlgoDroite.setVisible(true);

        messageLabel.setVisible(false);
    }
}
