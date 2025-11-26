package com.ordonnancement.ui.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.config.DefaultPaths;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.service.configuration.ConfigurationWriter;
import com.ordonnancement.service.configuration.ConfigurationWriterException;
import com.ordonnancement.ui.Alert.AlertUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ConfigController {

    @FXML
    private Label metriquesLabel;
    @FXML
    private Label labelProcessPath;
    @FXML
    private Label labelRessourcesPath;
    @FXML
    private VBox algoContainer;
    @FXML
    private Button btnEnregistrer;

    @FXML
    private CheckBox cbFifo;
    @FXML
    private CheckBox cbPriorite;
    @FXML
    private CheckBox cbRR;
    @FXML
    private TextField tfQuantum;
    @FXML
    private Label labQuantum;
    @FXML
    private TitledPane tpResultats;
    @FXML
    private VBox vBoxResultats;
    @FXML
    private Button btnDefaut;

    private final Label fifoDet = new Label("Aucun");
    private final Label fifoGlob = new Label("Aucun");
    private final Label prioriteDet = new Label("Aucun");
    private final Label prioriteGlob = new Label("Aucun");
    private final Label rrDet = new Label("Aucun");
    private final Label rrGlob = new Label("Aucun");

    private final String destination = ConfigurationManager.getInstance().getCheminFichierConfig();
    private AppMainFrameController mainController;

    @FXML
    /*
        * Cette methode permet d'initialiser les composants graphiques, de configurer les listeners et de charger la configuration existante.
    */

    private void initialize() {
        if (tfQuantum != null) {
            tfQuantum.setDisable(true);
        }
        if (btnEnregistrer != null) {
            btnEnregistrer.setDisable(true);
        }

        cbFifo.selectedProperty().addListener((obs, o, n) -> refreshUI());
        cbPriorite.selectedProperty().addListener((obs, o, n) -> refreshUI());
        cbRR.selectedProperty().addListener((obs, o, n) -> {
            if (tfQuantum != null) {
                tfQuantum.setDisable(!n);
            }
            refreshUI();
        });
        if (tfQuantum != null) {
            tfQuantum.textProperty().addListener((obs, o, n) -> refreshUI());
        }

        Platform.runLater(this::reloadExistingConfig);
    }

    /*
        * Cette methode permet de definir le controleur principal de l'application.
        * @param c Ce parametre de type AppMainFrameController représente le controleur principal.
    */

    public void setAppMainFrameController(AppMainFrameController c) {
        this.mainController = c;
    }

    @FXML
    /*
        * Cette methode permet de choisir ou creer un fichier CSV pour les metriques globales.
        * Cette methode affiche une fenetre de selection/sauvegarde de fichier.
    */

    private void choisirFichierMetriques() {

        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir le fichier des métriques globales");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        // Si un chemin existe déjà → dossier initial
        if (metriquesLabel != null && metriquesLabel.getText() != null && !metriquesLabel.getText().equals("Aucun fichier")) {
            File prev = new File(metriquesLabel.getText());
            if (prev.exists()) {
                fc.setInitialDirectory(prev.isDirectory() ? prev : prev.getParentFile());
            }
        }

        File sel = fc.showSaveDialog(getOwnerWindow());
        if (sel != null) {
            metriquesLabel.setText(sel.getAbsolutePath());
            refreshUI();
        }
    }

    @FXML
    /*
        * Cette methode permet de choisir un fichier CSV contenant la liste des processus.
        * Cette methode affiche une fenetre de selection de fichier.
    */

    private void choisirFichierProcess() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir le fichier des processus");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File sel = fc.showOpenDialog(getOwnerWindow());
        if (sel != null) {
            labelProcessPath.setText(sel.getAbsolutePath());
            refreshUI();
        }
    }

    @FXML
    /*
        * Cette methode permet de choisir un fichier JSON contenant la liste des ressources.
        * Cette methode affiche une fenetre de selection de fichier.
    */

    private void choisirFichierRessources() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir le fichier des ressources");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File sel = fc.showOpenDialog(getOwnerWindow());
        if (sel != null) {
            labelRessourcesPath.setText(sel.getAbsolutePath());
            refreshUI();
        }
    }

    @FXML
    /*
        * Cette methode permet d'annuler la creation de configuration et de retourner a l'accueil.
        * Cette methode affiche une fenetre de confirmation.
    */

    private void doAnnuler() {
        if (mainController != null) {
            boolean answer = AlertUtils.showConfirmation("Annulation", "Êtes-vous sûr de vouloir annuler ?\nCette action vous renverra à l'accueil.", getOwnerWindow());
            if (answer) {
                mainController.afficherHome();
            }

        }
    }

    @FXML
    /*
        * Cette methode permet de valider les parametres, de generer la configuration et de l'enregistrer sur le disque.
        * Cette methode affiche des boites de dialogue en cas d'erreur ou de succes.
    */

    private void doValider() {
        Window owner = getOwnerWindow();
        List<AlgoConfiguration> algos = new ArrayList<>();
        try {
            if (cbFifo.isSelected()) {
                algos.add(new AlgoConfiguration("FIFO", fifoDet.getText(), fifoGlob.getText(), null));
            }
            if (cbPriorite.isSelected()) {
                algos.add(new AlgoConfiguration("PRIORITE", prioriteDet.getText(), prioriteGlob.getText(), null));
            }
            if (cbRR.isSelected()) {
                if (tfQuantum.getText() == null || tfQuantum.getText().isBlank()) {
                    AlertUtils.showError("Erreur", "Vous avez choisi Round Robin mais aucun quantum n'a été saisi.", owner);
                    return;
                }
                int q;
                try {
                    q = Integer.parseInt(tfQuantum.getText());
                    if (q <= 0) {
                        throw new NumberFormatException();
                
                    }} catch (NumberFormatException ex) {
                    AlertUtils.showError("Erreur", "Le quantum doit être un entier naturel non nul !", owner);
                    return;
                }
                algos.add(new AlgoConfiguration("ROUND ROBIN", rrDet.getText(), rrGlob.getText(), q));
            }
        } catch (IllegalArgumentException iae) {
            AlertUtils.showError("Erreur", "Erreur de configuration d'un algorithme : " + iae.getMessage(), owner);
            return;
        }

        if (algos.isEmpty()) {
            AlertUtils.showError("Erreur", "Vous devez sélectionner au moins un algorithme.", owner);
            return;
        }

        if (metriquesLabel == null || metriquesLabel.getText() == null || metriquesLabel.getText().equals("Aucun dossier")) {
            AlertUtils.showError("Erreur", "Le fichier des métriques n'est pas renseigné.", owner);
            return;
        }

        
       

        FileConfiguration fileConfig;
        try {
            fileConfig = new FileConfiguration(
                    labelProcessPath.getText(),
                    metriquesLabel.getText(),
                    labelRessourcesPath.getText(),
                    algos
            );
        } catch (IllegalArgumentException e) {
            AlertUtils.showError("Erreur", "Configuration invalide : " + e.getMessage(), owner);
            return;
        }

        try {
            new ConfigurationWriter().writeConfiguration(fileConfig, destination);
            ConfigurationManager.getInstance().setFileConfiguration(fileConfig);
            AlertUtils.showInfo("Succès", "Configuration enregistrée !", owner);
            if (mainController != null) {
                mainController.afficherHome();
            }
        } catch (ConfigurationWriterException e) {
            AlertUtils.showError("Erreur", "Impossible d'enregistrer : " + e.getMessage(), owner);
        }
    }

    /*
        * Cette methode permet de recuperer la fenetre parente associée aux elements de l'interface.
        * @return Cette methode retourne une valeur de type Window qui represente la fenetre proprietaire.
    */

    private Window getOwnerWindow() {
        if (btnEnregistrer != null && btnEnregistrer.getScene() != null) {
            return btnEnregistrer.getScene().getWindow();
        }
        if (labelProcessPath != null && labelProcessPath.getScene() != null) {
            return labelProcessPath.getScene().getWindow();
        }
        return null;
    }


    /*
        * Cette methode permet de mettre a jour l'affichage en fonction des selections d'algorithmes.
        * Cette methode affiche et met a jour dynamiquement les blocs d'algorithmes.
    */

    private void refreshUI() {

        // Vider la zone des algos
        vBoxResultats.getChildren().clear();
        HBox.setHgrow(vBoxResultats, Priority.ALWAYS);
        // Recréer dynamiquement les blocs d’algorithmes
        if (cbFifo.isSelected()) {
            vBoxResultats.getChildren().add(buildAlgoBlock("FIFO", fifoDet, fifoGlob));
        }
        if (cbPriorite.isSelected()) {
            vBoxResultats.getChildren().add(buildAlgoBlock("Priorité", prioriteDet, prioriteGlob));
        }
        if (cbRR.isSelected()) {
            vBoxResultats.getChildren().add(buildAlgoBlock("Round Robin", rrDet, rrGlob));
        }

        // Si aucune sélection → hboxResultatsAlgos vide, mais on NE TOUCHE PAS au reste du contenu
        boolean hasAlgo = !vBoxResultats.getChildren().isEmpty();
        vBoxResultats.setVisible(hasAlgo);
        vBoxResultats.setManaged(hasAlgo);

        // Bouton valider activé ou pas
        btnEnregistrer.setDisable(!canSave());
    }

    /*
        * Cette methode permet de construire dynamiquement un bloc UI pour un algorithme selectionné.
        * @param nom Ce parametre de type String représente le nom de l'algorithme.
        * @param det Ce parametre de type Label représente le label du fichier de resultats detailles.
        * @param glob Ce parametre de type Label représente le label du fichier de resultats globaux.
        * @return Cette methode retourne une valeur de type VBox qui represente le bloc graphique cree.
    */

    private VBox buildAlgoBlock(String nom, Label det, Label glob) {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding:10; -fx-border-color:#ddd; -fx-border-radius:6;");
        Label title = new Label(nom);
        title.setStyle("-fx-font-weight:bold;");
        box.getChildren().add(title);

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);

        Button bDet = new Button("Enregistrer sous...");
        bDet.setOnAction(e -> {
            File chosen = chooseCsvFile(det);
            if (chosen != null) {
                det.setText(chosen.getAbsolutePath());
            }
            refreshUI();
        });
        g.add(new Label("Résultats détaillés :"), 0, 0);
        g.add(bDet, 1, 0);
        g.add(det, 2, 0);

        Button bGlob = new Button("Enregistrer sous...");
        bGlob.setOnAction(e -> {
            File chosen = chooseCsvFile(glob);
            if (chosen != null) {
                glob.setText(chosen.getAbsolutePath());
            }
            refreshUI();
        });
        g.add(new Label("Résultats globaux :"), 0, 1);
        g.add(bGlob, 1, 1);
        g.add(glob, 2, 1);

        



        box.getChildren().add(g);
        return box;
    }


    /*
        * Cette methode permet de choisir un fichier CSV a enregistrer via un FileChooser.
        * @param ref Ce parametre de type Label représente le label contenant le chemin precedent.
        * @return Cette methode retourne une valeur de type File qui represente le fichier choisi par l'utilisateur.
    */

    private File chooseCsvFile(Label ref) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir fichier CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        if (ref != null && ref.getText() != null && !ref.getText().startsWith("Aucun")) {
            File prev = new File(ref.getText());
            if (prev.exists()) {
                fc.setInitialDirectory(prev.getParentFile());
            }
        }

        return fc.showSaveDialog(getOwnerWindow());
    }



    @FXML
    /*
        * Cette methode permet de charger automatiquement les chemins et valeurs par defaut pour tous les parametres de configuration.
        * Cette methode affiche et met a jour l'interface avec les valeurs par defaut.
    */
    private void doDefaut() {

        // --- Forcer la sélection de tous les algorithmes ---
        cbFifo.setSelected(true);
        cbPriorite.setSelected(true);
        cbRR.setSelected(true);

        // --- Chemins généraux ---
        labelProcessPath.setText(DefaultPaths.PROCESS_INIT.getPath());
        labelRessourcesPath.setText(DefaultPaths.RESSOURCES_INIT.getPath());
        metriquesLabel.setText(DefaultPaths.METRIQUES_GLOBAL.getPath());

        // --- FIFO ---
        fifoDet.setText(DefaultPaths.R_DETAILLED_FIFO.getPath());
        fifoGlob.setText(DefaultPaths.R_GLOBAL_FIFO.getPath());

        // --- Priorité ---
        prioriteDet.setText(DefaultPaths.R_DETAILLED_PRIORITE.getPath());
        prioriteGlob.setText(DefaultPaths.R_GLOBAL_PRIORITE.getPath());

        // --- Round Robin ---
        rrDet.setText(DefaultPaths.R_DETAILLED_RR.getPath());
        rrGlob.setText(DefaultPaths.R_GLOBAL_RR.getPath());
        tfQuantum.setDisable(false);
        tfQuantum.setText("2"); // valeur par défaut

        refreshUI();
    }

    /*
        * Cette methode permet de charger une configuration existante depuis le disque et de mettre a jour l'affichage en consequence.
        * Cette methode affiche une alerte en cas d'erreur de chargement.
    */
    private void reloadExistingConfig() {
        try {
            ConfigurationManager.getInstance().loadConfiguration();
            FileConfiguration conf = ConfigurationManager.getInstance().getFileConfiguration();
            if (conf == null) {
                if (labelProcessPath != null) {
                    labelProcessPath.setText("Aucun fichier");
                }
                if (labelRessourcesPath != null) {
                    labelRessourcesPath.setText("Aucun fichier");
                }
                if (metriquesLabel != null) {
                    metriquesLabel.setText("Aucun fichier");
                }
                refreshUI();
                return;
            }

            if (labelRessourcesPath != null && conf.getFichierRessourcesDisponibles() != null) {
                labelRessourcesPath.setText(conf.getFichierRessourcesDisponibles());
            }

            if (labelProcessPath != null && conf.getFichierProcessus() != null) {
                labelProcessPath.setText(conf.getFichierProcessus());
            }

            if (metriquesLabel != null && conf.getFichierMetriquesGlobales() != null) {
                metriquesLabel.setText(conf.getFichierMetriquesGlobales());

            }

            if (conf.getListeAlgorithmes() != null) {
                for (AlgoConfiguration a : conf.getListeAlgorithmes()) {
                    switch (a.getNomAlgorithme().toUpperCase()) {
                        case "FIFO":
                            cbFifo.setSelected(true);
                            fifoDet.setText(a.getFichierResultatsDetailles());
                            fifoGlob.setText(a.getFichierResultatsGlobaux());
                            break;
                        case "PRIORITE":
                            cbPriorite.setSelected(true);
                            prioriteDet.setText(a.getFichierResultatsDetailles());
                            prioriteGlob.setText(a.getFichierResultatsGlobaux());
                            break;
                        case "ROUND ROBIN":
                            cbRR.setSelected(true);
                            rrDet.setText(a.getFichierResultatsDetailles());
                            rrGlob.setText(a.getFichierResultatsGlobaux());
                            if (a.getQuantum() != null) {
                                tfQuantum.setDisable(false);
                                tfQuantum.setText(String.valueOf(a.getQuantum()));
                            }
                            break;
                    }
                }
            }
            refreshUI();
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible de charger la configuration existante.\nVeuillez remplir les champs pour en créer une nouvelle.", getOwnerWindow());
        }
    }


    /*
        * Cette methode permet de verifier si tous les champs obligatoires pour l'enregistrement d'une configuration sont correctement renseignes.
        * @return Cette methode retourne une valeur de type boolean qui represente la possibilite ou non de sauvegarder la configuration.
    */
    private boolean canSave() {

        // Vérif de base : chemins généraux
        if (labelProcessPath == null || labelRessourcesPath == null || metriquesLabel == null) {
            return false;
        }

        if (labelProcessPath.getText() == null || labelProcessPath.getText().startsWith("Aucun")) {
            return false;
        }

        if (labelRessourcesPath.getText() == null || labelRessourcesPath.getText().startsWith("Aucun")) {
            return false;
        }

        if (metriquesLabel.getText() == null || metriquesLabel.getText().startsWith("Aucun")) {
            return false;
        }

        // --- Vérif FIFO ---
        if (cbFifo.isSelected()) {

            if (fifoDet.getText() == null || fifoDet.getText().startsWith("Aucun")) {
                return false;
            }

            if (fifoGlob.getText() == null || fifoGlob.getText().startsWith("Aucun")) {
                return false;
            }

        }

        // --- Vérif Priorité ---
        if (cbPriorite.isSelected()) {

            if (prioriteDet.getText() == null || prioriteDet.getText().startsWith("Aucun")) {
                return false;
            }

            if (prioriteGlob.getText() == null || prioriteGlob.getText().startsWith("Aucun")) {
                return false;
            }

        }

        // --- Vérif Round Robin ---
        if (cbRR.isSelected()) {

            if (rrDet.getText() == null || rrDet.getText().startsWith("Aucun")) {
                return false;
            }

            if (rrGlob.getText() == null || rrGlob.getText().startsWith("Aucun")) {
                return false;
            }

            // Quantum obligatoire
            if (tfQuantum == null || tfQuantum.getText() == null || tfQuantum.getText().isBlank()) {
                return false;
            }

            try {

                int q = Integer.parseInt(tfQuantum.getText());

                if (q <= 0) {
                    return false;
                }

            } catch (NumberFormatException e) {

                return false;

            }

        }

        // Aucun algo sélectionné ?
        if (!cbFifo.isSelected() && !cbPriorite.isSelected() && !cbRR.isSelected()) {
            return false;
        }

        return true;

    }
}
