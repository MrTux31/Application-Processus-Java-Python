package com.ordonnancement.ui.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.service.configuration.ConfigurationWriter;
import com.ordonnancement.service.configuration.ConfigurationWriterException;
import com.ordonnancement.ui.Alert.AlertUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ConfigController {

    @FXML private Label metriquesLabel;
    @FXML private Label labelProcessPath;
    @FXML private Label labelRessourcesPath;
    @FXML private VBox algoContainer;
    @FXML private Button btnEnregistrer;

    @FXML private CheckBox cbFifo;
    @FXML private CheckBox cbPriorite;
    @FXML private CheckBox cbRR;
    @FXML private TextField tfQuantum;
    @FXML private TitledPane tpResultats;

    private final Label fifoDet = new Label("Aucun");
    private final Label fifoGlob = new Label("Aucun");
    private final Label prioriteDet = new Label("Aucun");
    private final Label prioriteGlob = new Label("Aucun");
    private final Label rrDet = new Label("Aucun");
    private final Label rrGlob = new Label("Aucun");

    private final String destination = ConfigurationManager.getInstance().getCheminFichierConfig();
    private AppMainFrameController mainController;

    @FXML
    private void initialize() {
        if (tfQuantum != null) tfQuantum.setDisable(true);
        if (btnEnregistrer != null) btnEnregistrer.setDisable(true);

        cbFifo.selectedProperty().addListener((obs,o,n) -> refreshUI());
        cbPriorite.selectedProperty().addListener((obs,o,n) -> refreshUI());
        cbRR.selectedProperty().addListener((obs,o,n) -> {
            if (tfQuantum != null) tfQuantum.setDisable(!n);
            refreshUI();
        });
        if (tfQuantum != null) tfQuantum.textProperty().addListener((obs,o,n) -> refreshUI());

        Platform.runLater(this::reloadExistingConfig);
    }

    public void setAppMainFrameController(AppMainFrameController c) {
        this.mainController = c;
    }

    @FXML
    private void choisirDossierMetriques() {
        Window owner = getOwnerWindow();
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choisir le dossier métriques");
        if (metriquesLabel != null && metriquesLabel.getText() != null && !metriquesLabel.getText().equals("Aucun dossier")) {
            File prev = new File(metriquesLabel.getText());
            if (prev.exists()) dc.setInitialDirectory(prev.isDirectory() ? prev : prev.getParentFile());
        }
        File chosen = dc.showDialog(owner);
        if (chosen != null) {
            metriquesLabel.setText(chosen.getAbsolutePath());
            refreshUI();
        }
    }

    @FXML
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
    private void doAnnuler() {
        Window owner = getOwnerWindow();
        if (owner != null) owner.hide();
        if (mainController != null) mainController.afficherHome();
    }

    @FXML
    private void doValider() {
        Window owner = getOwnerWindow();
        List<AlgoConfiguration> algos = new ArrayList<>();
        try {
            if (cbFifo.isSelected()) algos.add(new AlgoConfiguration("FIFO", fifoDet.getText(), fifoGlob.getText(), null));
            if (cbPriorite.isSelected()) algos.add(new AlgoConfiguration("PRIORITE", prioriteDet.getText(), prioriteGlob.getText(), null));
            if (cbRR.isSelected()) {
                if (tfQuantum.getText() == null || tfQuantum.getText().isBlank()) {
                    AlertUtils.showError("Erreur", "Vous avez choisi Round Robin mais aucun quantum n'a été saisi.", owner);
                    return;
                }
                int q;
                try { q = Integer.parseInt(tfQuantum.getText()); if(q<=0) throw new NumberFormatException(); }
                catch(NumberFormatException ex) {
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
            AlertUtils.showError("Erreur", "Le dossier métriques n'est pas renseigné.", owner);
            return;
        }

        File metDir = new File(metriquesLabel.getText());
        if (!metDir.exists() && !metDir.mkdirs()) {
            AlertUtils.showError("Erreur", "Impossible de créer le dossier Métriques : " + metDir.getAbsolutePath(), owner);
            return;
        }

        FileConfiguration fileConfig;
        try {
            fileConfig = new FileConfiguration(
                labelProcessPath.getText(),
                new File(metDir,"metriquesGlobales.csv").getAbsolutePath(),
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
            if (mainController != null) mainController.afficherHome();
        } catch (ConfigurationWriterException e) {
            AlertUtils.showError("Erreur", "Impossible d'enregistrer : " + e.getMessage(), owner);
        }
    }

    private Window getOwnerWindow() {
        if (btnEnregistrer != null && btnEnregistrer.getScene() != null)
            return btnEnregistrer.getScene().getWindow();
        if (labelProcessPath != null && labelProcessPath.getScene() != null)
            return labelProcessPath.getScene().getWindow();
        return null;
    }

    private void refreshUI() {
        VBox content = new VBox(12);
        content.setStyle("-fx-padding:10;");

        // Algorithmes sélectionnés
        if (cbFifo.isSelected()) content.getChildren().add(buildAlgoBlock("FIFO", fifoDet, fifoGlob));
        if (cbPriorite.isSelected()) content.getChildren().add(buildAlgoBlock("PRIORITE", prioriteDet, prioriteGlob));
        if (cbRR.isSelected()) content.getChildren().add(buildAlgoBlock("ROUND ROBIN", rrDet, rrGlob));

        tpResultats.setContent(content);
        tpResultats.setVisible(!content.getChildren().isEmpty());
        tpResultats.setManaged(!content.getChildren().isEmpty());

        btnEnregistrer.setDisable(!canSave());
    }


    private VBox buildAlgoBlock(String nom, Label det, Label glob) {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding:10; -fx-border-color:#ddd; -fx-border-radius:6;");
        Label title = new Label(nom);
        title.setStyle("-fx-font-weight:bold;");
        box.getChildren().add(title);

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);

        Button bDet = new Button("Choisir...");
        bDet.setOnAction(e -> {
            File chosen = chooseDirectoryForLabel(det);
            if (chosen != null) det.setText(chosen.getAbsolutePath());
            refreshUI();
        });
        g.add(new Label("Résultats détaillés :"), 0, 0);
        g.add(bDet, 1, 0);
        g.add(det, 2, 0);

        Button bGlob = new Button("Choisir...");
        bGlob.setOnAction(e -> {
            File chosen = chooseDirectoryForLabel(glob);
            if (chosen != null) glob.setText(chosen.getAbsolutePath());
            refreshUI();
        });
        g.add(new Label("Résultats globaux :"), 0, 1);
        g.add(bGlob, 1, 1);
        g.add(glob, 2, 1);

        box.getChildren().add(g);
        return box;
    }

    private File chooseDirectoryForLabel(Label ref) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choisir dossier");
        if (ref != null && ref.getText() != null && !ref.getText().startsWith("Aucun")) {
            File prev = new File(ref.getText());
            if (prev.exists()) dc.setInitialDirectory(prev.isDirectory() ? prev : prev.getParentFile());
        }
        return dc.showDialog(getOwnerWindow());
    }

    private void reloadExistingConfig() {
        try {
            ConfigurationManager.getInstance().loadConfiguration();
            FileConfiguration conf = ConfigurationManager.getInstance().getFileConfiguration();
            if (conf == null) {
                if (labelProcessPath != null) labelProcessPath.setText("Aucun fichier");
                if (labelRessourcesPath != null) labelRessourcesPath.setText("Aucun fichier");
                if (metriquesLabel != null) metriquesLabel.setText("Aucun dossier");
                refreshUI();
                return;
            }

            if (labelRessourcesPath != null && conf.getFichierRessourcesDisponibles() != null)
                labelRessourcesPath.setText(conf.getFichierRessourcesDisponibles());

            if (labelProcessPath != null && conf.getFichierProcessus() != null)
                labelProcessPath.setText(conf.getFichierProcessus());

            if (metriquesLabel != null && conf.getFichierMetriquesGlobales() != null) {
                File metParent = new File(conf.getFichierMetriquesGlobales()).getParentFile();
                if (metParent != null) metriquesLabel.setText(metParent.getAbsolutePath());
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
            AlertUtils.showError("Erreur", "Impossible de charger la configuration existante.", getOwnerWindow());
        }
    }

    private boolean canSave() {
        if (labelProcessPath == null || labelRessourcesPath == null || metriquesLabel == null) return false;
        if (labelProcessPath.getText() == null || labelProcessPath.getText().startsWith("Aucun")) return false;
        if (labelRessourcesPath.getText() == null || labelRessourcesPath.getText().startsWith("Aucun")) return false;
        if (metriquesLabel.getText() == null || metriquesLabel.getText().startsWith("Aucun")) return false;
        return cbFifo.isSelected() || cbPriorite.isSelected() || cbRR.isSelected();
    }
}
