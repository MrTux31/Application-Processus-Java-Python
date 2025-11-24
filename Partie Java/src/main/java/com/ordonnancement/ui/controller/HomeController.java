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

public class HomeController {

    @FXML
    private Button btnStart;
    @FXML
    private Button btnConfig;
    private AppMainFrameController appMainFrameController;
    

    /**
     * S'occupe de démarre l'exécution du programme python avec la configuration fournie
     */
    @FXML
    private void doLancerExecution() {
        // AncienMain.lancerExecution();//TO DO : A SUPPRIMER, TEMPORAIRE CAR ATTENTE DE LA PARTIE CONFIGURATION

        try {
            //Récupérer les infos sur le fichier de config dans le singleton
            String fichierConfig = ConfigurationManager.getInstance().getCheminFichierConfig();
            //on charge la configuration depuis le fichier de configuration précédent
            ConfigurationManager.getInstance().loadConfiguration(); //On parse le json
            FileConfiguration configuration = ConfigurationManager.getInstance().getFileConfiguration(); //On récup l'objet
            
        
            //Lancer l'execution / écriture fichier config + récup des résultats de python
            Runner.runAsync(configuration,
                    fichierConfig,
                    () -> {
                        afficherResumeExecution();
                        AncienMain.AfficherResultats(); // TO DO : A SUPPRIMER
                    },
                    e -> { //Si une exception arrive lors de l'execution
                            AlertUtils.showError("Erreur", e.getMessage(), btnStart.getParent().getScene().getWindow());
                            e.printStackTrace();
                        
                        });
                
        }catch(FileParsingException e){
            AlertUtils.showError("Erreur de configuration", e.getMessage(),btnStart.getParent().getScene().getWindow());
        }
        catch(Exception e){
            AlertUtils.showError("Erreur",e.getMessage(),btnStart.getParent().getScene().getWindow());
        }

    }

    



    /**
     * Affiche une fenetre d'information pour dire que l'execution est terminée
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

        AlertUtils.showInfo("Fin de l'execution", sb.toString(), null);

    }

    @FXML
    private void doAfficherConfig(){
        appMainFrameController.doAfficherConfig();
    }





    public void setAppMainFrameController(AppMainFrameController appMainFrameController) {
        this.appMainFrameController=appMainFrameController;
    }

}
