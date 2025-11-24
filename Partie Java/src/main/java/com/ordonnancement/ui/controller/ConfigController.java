package com.ordonnancement.ui.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.service.configuration.ConfigurationWriter;
import com.ordonnancement.ui.Alert.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ConfigController {

    // Labels pour afficher les chemins choisis
    @FXML
    private Label labelRessourcesPath;
    @FXML
    private Label labelProcessPath;
    @FXML
    private Label labelResultatPath;

    // Boutons pour sélectionner les fichiers
    @FXML
    private Button btnRessources;
    @FXML
    private Button btnProcess;
    @FXML
    private Button btnResultat;

    // Algorithmes
    @FXML
    private CheckBox cbFifo;
    @FXML
    private CheckBox cbRR;
    @FXML
    private CheckBox cbPriorite;

    @FXML
    private TextField tfQuantum;

    // Boutons en bas
    @FXML
    private Button btnEnregistrer;
    @FXML
    private Button btnAnnuler;

    private AppMainFrameController appMainFrameController;

    @FXML
    private void initialize() {
        // Initialisation si besoin
        tfQuantum.setDisable(true);
        btnEnregistrer.setDisable(true);

        cbRR.selectedProperty().addListener((obs, oldVal, newVal) -> {
            tfQuantum.setDisable(!newVal);
            checkReadyToSave();

        });
        
        cbFifo.selectedProperty().addListener((obs, oldVal, newVal) -> checkReadyToSave());
        cbPriorite.selectedProperty().addListener((obs, oldVal, newVal) -> checkReadyToSave());
        tfQuantum.textProperty().addListener((obs, oldVal, newVal) -> checkReadyToSave());

        preremplir();
    }

    public void setAppMainFrameController(AppMainFrameController appMainFrameController) {
        this.appMainFrameController = appMainFrameController;
    }

    private File enregistrerSousJson(){
        Stage stage = (Stage) btnEnregistrer.getScene().getWindow();


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier JSON");

        // Filtre d'extension : .json uniquement
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier JSON (*.json)", "*.json")
        );

        // Si un dossier resultat a déjà été choisi -> on l'utilise
        File initialDir = null;
        if (labelResultatPath.getText() != null 
                && !labelResultatPath.getText().equals("Aucun fichier")) {
            File dirFromConfig = new File(labelResultatPath.getText());
            System.out.println(labelResultatPath.getText());
            if (dirFromConfig.exists() && dirFromConfig.isDirectory()) {
                initialDir = dirFromConfig;
            }
        }

        // Sinon -> Home
        if (initialDir == null) {
            initialDir = new File(System.getProperty("user.home"));
        }

        fileChooser.setInitialDirectory(initialDir);

        fileChooser.setInitialFileName("config.json");


        // Fenêtre "Enregistrer sous"
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            // Vérifier que le fichier se termine bien par .json
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.endsWith(".json")) {
                selectedFile = new File(filePath + ".json");
            }
        }
        return selectedFile;

    }

    @FXML
    private void doAnnuler() {
        // Action pour annuler, par exemple vider les champs ou fermer la vue
        goHome();
        System.out.println("Annulation");
    }

    

    private void checkReadyToSave() {
    boolean fichiersOk =
    !labelRessourcesPath.getText().equals("Aucun fichier") &&
    !labelProcessPath.getText().equals("Aucun fichier") &&
    !labelResultatPath.getText().equals("Aucun fichier");

    boolean algoOk = cbFifo.isSelected() || cbRR.isSelected() || cbPriorite.isSelected();

    btnEnregistrer.setDisable(!(fichiersOk && algoOk));
    

}


    @FXML
    private void doValider() {

    // Si Round Robin est coche mais quantum vide -> erreur
    if (cbRR.isSelected() && (tfQuantum.getText() == null || tfQuantum.getText().isBlank())) {
        AlertUtils.showError(
                "Erreur",
                "Vous avez choisi Round Robin mais aucun quantum n'a été saisi.",
                btnEnregistrer.getScene().getWindow()
        );
        return; // On stoppe l'exécution
    }

    if (cbRR.isSelected()) {
        int quantumSelected;
    try {
        quantumSelected = Integer.parseInt(tfQuantum.getText());
        if(quantumSelected <= 0){
            AlertUtils.showError(
                "Erreur",
                "Le quantum doit être un entier naturel non null !",
                btnEnregistrer.getScene().getWindow()
            );
            return;
        }
    } catch (NumberFormatException e) {
        AlertUtils.showError(
                "Erreur",
                "Le quantum doit être un nombre !!!",
                btnEnregistrer.getScene().getWindow()
        );
        return;
    }
}
    
    if (btnEnregistrer.isDisabled()) {
        System.out.println("Impossible d'enregistrer : champs manquants");
        return;
    }

    List<AlgoConfiguration> algos = new ArrayList<>();

    if (cbFifo.isSelected()) {
        algos.add(new AlgoConfiguration(
            "FIFO",
            labelResultatPath.getText() + "\\FIFO\\rDetailedFIFO.csv",
            labelResultatPath.getText() + "\\FIFO\\rGlobauxFIFO.csv",
            null
        ));
    }

    if (cbPriorite.isSelected()) {
        algos.add(new AlgoConfiguration(
            "PRIORITE",
            labelResultatPath.getText() + "\\PRIORITE\\rDetailedPRIORITE.csv",
            labelResultatPath.getText() + "\\PRIORITE\\rGlobauxPRIORITE.csv",
            null
        ));
    }

    if (cbRR.isSelected()) {
        algos.add(new AlgoConfiguration(
            "ROUND ROBIN",
            labelResultatPath.getText() + "\\ROUND ROBIN\\rDetailedROUNDROBIN.csv",
            labelResultatPath.getText() + "\\ROUND ROBIN\\rGlobauxROUNDROBIN.csv",
            Integer.parseInt(tfQuantum.getText())
        ));
    }

    String metriquesFile = labelResultatPath.getText() + "\\Metriques\\metriquesGlobales.csv";

    FileConfiguration fileConfig = new FileConfiguration(
            labelProcessPath.getText(),
            metriquesFile,
            labelRessourcesPath.getText(),
            algos
    );

    File destination = enregistrerSousJson();
    if (destination != null) {
        ConfigurationManager.getInstance().setCheminFichierConfig(destination.toString());
        ConfigurationManager.getInstance().setFileConfiguration(fileConfig);
        new ConfigurationWriter().writeConfiguration(fileConfig, destination.toString());
        System.out.println("Configuration enregistrée !");
        AlertUtils.showInfo("Enregistrement"," Enregistrement effectué, retour à l'accueil ...", null);
        goHome();
    }
    
    }

   private void goHome() {
        appMainFrameController.afficherHome();
    }


    private void preremplir() {
        try {
            FileConfiguration conf = ConfigurationManager.getInstance().getFileConfiguration();

            if (conf == null)
                return; // rien à préremplir

            // --- Chemins des fichiers ---
            if (conf.getFichierRessourcesDisponibles() != null)
                labelRessourcesPath.setText(conf.getFichierRessourcesDisponibles());

            if (conf.getFichierProcessus() != null)
                labelProcessPath.setText(conf.getFichierProcessus());

            if (conf.getFichierMetriquesGlobales() != null) {
                // On récupère juste le dossier parent des résultats
                File parent = new File(conf.getFichierMetriquesGlobales()).getParentFile();
                if (parent != null)
                    labelResultatPath.setText(parent.getAbsolutePath());
            }

            // --- Algorithmes cochés ---
            cbFifo.setSelected(false);
            cbRR.setSelected(false);
            cbPriorite.setSelected(false);
            tfQuantum.setDisable(true);

            if (conf.getListeAlgorithmes() != null) {
                conf.getListeAlgorithmes().forEach(a -> {

                    switch(a.getNomAlgorithme()) {

                        case "FIFO":
                            cbFifo.setSelected(true);
                            break;

                        case "PRIORITE":
                            cbPriorite.setSelected(true);
                            break;

                        case "ROUND ROBIN":
                            cbRR.setSelected(true);
                            if (a.getQuantum() != null) {
                                tfQuantum.setDisable(false);
                                tfQuantum.setText(String.valueOf(a.getQuantum()));
                            }
                            break;
                    }
                });
            }

            checkReadyToSave();

        } catch (Exception e) {
            System.out.println("Aucune configuration précédente");
        }
    }




    private FileChooser creerCorrectFileChooser(String title, String extension, Label labelPath) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);

        // Extension éventuelle
        if (extension != null && !extension.isBlank()) {
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("*." + extension, "*." + extension)
            );
        }

        // Si un fichier a déjà été sélectionné → utiliser son dossier
        if (labelPath.getText() != null && !labelPath.getText().equals("Aucun fichier")) {
            File previous = new File(labelPath.getText());
            File parentDir = previous.isFile() ? previous.getParentFile() : previous;

            if (parentDir != null && parentDir.exists()) {
                chooser.setInitialDirectory(parentDir);
            }
        } else {
            // Sinon → Home
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        return chooser;
    }



    private DirectoryChooser creerCorrectDirectoryChooser(String title, Label labelPath) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);

        if (labelPath.getText() != null && !labelPath.getText().equals("Aucun fichier")) {
            File previous = new File(labelPath.getText());
            if (previous.exists() && previous.isDirectory()) {
                chooser.setInitialDirectory(previous);
            }
        } else {
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        return chooser;
    }




    @FXML
    public void choisirRessources() {

        Stage stage = (Stage) btnRessources.getScene().getWindow();
        FileChooser chooser = creerCorrectFileChooser(
                "Choisir le fichier de ressources :",
                "json",
                labelRessourcesPath
        );

        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            labelRessourcesPath.setText(selected.getAbsolutePath());
            checkReadyToSave();
        }
    }



    @FXML
    public void choisirProcess() {

        Stage stage = (Stage) btnProcess.getScene().getWindow();
        FileChooser chooser = creerCorrectFileChooser(
                "Choisir le fichier des processus initiaux :",
                "csv",
                labelProcessPath
        );

        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            labelProcessPath.setText(selected.getAbsolutePath());
            checkReadyToSave();
        }
    }


    @FXML
    private void choisirDossierResultats() {

        Stage stage = (Stage) btnResultat.getScene().getWindow();
        DirectoryChooser chooser = creerCorrectDirectoryChooser(
                "Choisir le dossier résultat",
                labelResultatPath
        );

        File selected = chooser.showDialog(stage);
        if (selected != null) {
            labelResultatPath.setText(selected.getAbsolutePath());
            checkReadyToSave();
        }
    }



}
