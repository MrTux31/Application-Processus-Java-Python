package com.ordonnancement.ui.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.service.configuration.ConfigurationWriter;
import com.ordonnancement.ui.Alert.AlertUtils;

import javafx.application.Platform;
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
    private final String destination = ConfigurationManager.getInstance().getCheminFichierConfig(); //Dossier où sera sauvegardé la config
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

        Platform.runLater(() -> preremplir()); //Charger le fichier de configuration.
    }

    public void setAppMainFrameController(AppMainFrameController appMainFrameController) {
        this.appMainFrameController = appMainFrameController;
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

    
   
    new ConfigurationWriter().writeConfiguration(fileConfig, destination); //On écrase la config précédente
    System.out.println("Configuration enregistrée !");
    ConfigurationManager.getInstance().setFileConfiguration(fileConfig);
    AlertUtils.showInfo("Enregistrement"," Enregistrement effectué, retour à l'accueil ...", btnEnregistrer.getScene().getWindow());
    goHome();
    
    
    }

   private void goHome() {
        appMainFrameController.afficherHome();
    }


    private void preremplir() {
        try {
            ConfigurationManager.getInstance().loadConfiguration(); //Charger la configuration déjà existante
            FileConfiguration conf = ConfigurationManager.getInstance().getFileConfiguration(); //Récupérer l'objet FileConfiguration

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

        } catch (Exception e) { //Si il y a une erreur de parsing, on affiche l'erreur et on met des champs vides à compléter.
            AlertUtils.showError("Erreur","Impossible de charger la configuration existante.\nRemplissez les champs pour créer une nouvelle configuration correcte." , btnEnregistrer.getScene().getWindow());

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
