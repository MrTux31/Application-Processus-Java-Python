package com.ordonnancement.ui.controller;

import com.ordonnancement.AncienMain;
import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.service.AppState;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.runner.Runner;
import com.ordonnancement.ui.Alert.AlertUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controleur pour l'affichage du menu principal
 * @author ROMA Quentin
 */
public class HomeController {

    @FXML
    private Button btnStart;
    @FXML
    private Button btnConfig;
    @FXML
    private Button btnProcessus;
    @FXML
    private Button btnGanttCPU;
    @FXML
    private Button btnGanttProcessus;
    @FXML
    private Button btnComparaisonAlgos;
    private AppMainFrameController appMainFrameController;
    
    /**
     * Initialise le contrôleur après le chargement du FXML.
     * Lie l'état d'exécution aux boutons pour activer/désactiver selon la fin de l'exécution.
     */
    @FXML
    public void initialize() {
        
        //Binding boutons et état d'execution
        btnProcessus.disableProperty().bind(AppState.getInstance().executionTermineeProperty().not()); //.not : si execution terminee = false alors on prends true pour disable
        btnGanttCPU.disableProperty().bind(AppState.getInstance().executionTermineeProperty().not());
        btnGanttProcessus.disableProperty().bind(AppState.getInstance().executionTermineeProperty().not());
        btnComparaisonAlgos.disableProperty().bind(AppState.getInstance().executionTermineeProperty().not());
        


    }

    /**
     * Démarre l'exécution du programme Python avec la configuration actuelle.
     * Récupère les paramètres depuis le fichier de configuration, lance l'exécution
     * et affiche un résumé à la fin.
     */
    @FXML
    private void doLancerExecution() {
        
        try {
            //Récupérer les infos sur le fichier de config dans le singleton
            String fichierConfig = ConfigurationManager.getInstance().getCheminFichierConfig();
            //on charge la configuration depuis le fichier de configuration précédent
            ConfigurationManager.getInstance().loadConfiguration(); //On parse le json
            FileConfiguration configuration = ConfigurationManager.getInstance().getFileConfiguration(); //On récup l'objet
            AppState.getInstance().setExecutionTerminee(false); //pour faire réagir les boutons avec le binding
            //Lancer l'execution / écriture fichier config + récup des résultats de python
            Runner.runAsync(configuration,
                    fichierConfig,
                    () -> {
                        afficherResumeExecution();
                        AppState.getInstance().setExecutionTerminee(true);
                        AncienMain.AfficherResultats(); // TO DO : A SUPPRIMER
                    },
                    e -> { //Si une exception arrive lors de l'execution
                            AlertUtils.showError("Erreur", e.getMessage(), btnStart.getParent().getScene().getWindow());
                            
                        
                        });
                
        }catch(FileParsingException e){
            AlertUtils.showError("Erreur de configuration", e.getMessage(),btnStart.getParent().getScene().getWindow());
        }
        catch(Exception e){
            AlertUtils.showError("Erreur",e.getMessage(),btnStart.getParent().getScene().getWindow());
        }

    }

    



    /**
     * Affiche une fenêtre d'information indiquant que l'exécution est terminée.
     * Affiche également la liste des algorithmes exécutés et le nombre de processus ordonnancés.
     */
    private void afficherResumeExecution() {
        Resultats resultats = AppState.getInstance().getResultats();
        StringBuilder sb = new StringBuilder();
        // Titre
        sb.append("✅ Exécution terminée\n\n");

        // Algorithmes exécutés
        sb.append("Algorithmes exécutés :\n");
        for (AlgoConfiguration algo : ConfigurationManager.getInstance().getFileConfiguration().getListeAlgorithmes()) {
            sb.append("   • ").append(algo.getNomAlgorithme()).append("\n");
        }
        // Statistiques
        sb.append("\nNombre de processus ordonnancés : ")
                .append(resultats.getListeProcessus().size()).append("\n");

        AlertUtils.showInfo("Fin de l'execution", sb.toString(), btnStart.getParent().getScene().getWindow());

    }

    /**
     * Affiche l'interface de configuration dans la fenêtre principale.
     */
    @FXML
    private void doAfficherConfig(){
        appMainFrameController.doAfficherConfig();
    }

    /**
     * Définit le contrôleur principal de l'application pour permettre la communication entre vues.
     * @param appMainFrameController le contrôleur principal de l'application
     */
    public void setAppMainFrameController(AppMainFrameController appMainFrameController) {
        this.appMainFrameController=appMainFrameController;
    }
    /**
     * Affiche la liste des processus dans la fenêtre principale.
     */
    @FXML
    private void doAfficherProcessus() {
        appMainFrameController.doAfficherProcessus();
    }
    /**
     * Affiche le diagramme de Gantt pour les processeurs dans la fenêtre principale.
     */
    @FXML
    private void doAfficherGanttCPU() {
        appMainFrameController.doAfficherGanttCPU();
    }
    /**
     * Affiche le diagramme de Gantt pour les processus dans la fenêtre principale.
     */
    @FXML
    private void doAfficherGanttProcessus() {
        appMainFrameController.doAfficherGanttProcessus();
    }
    /**
     * Affiche la comparaison des algorithmes d'ordonnancement dans la fenêtre principale.
     */
    @FXML
    private void doAfficherComparaisonAlgos() {
        appMainFrameController.doAfficherComparaisonAlgos();
    }

}
