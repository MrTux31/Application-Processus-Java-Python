package com.ordonnancement.ui;

import com.ordonnancement.AncienMain;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Classe principale de l'application de gestion de tournois.
 * Charge l'interface graphique principale et gère le cycle de vie 
 * de l'application JavaFX.
 */

public class OrdonnancementApp extends Application {

    /** 
    * Fenêtre principale de l'application JavaFX.
    * Utilisée pour afficher les différentes vues (scènes) de l'application.
    */
    private Stage primaryStage;
    


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        try {
            //Charger la vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AppMainFrame.fxml"));
            //Créer un border pane dans lequel on met la vue
            BorderPane mainFrame = loader.load();
            Scene scene = new Scene(mainFrame, 800, 600);
            //On met la scene dans le stage
            primaryStage.setScene(scene);
            primaryStage.show();

            //TO DO : ENLEVER, ESSAI TEMPORAIRE
            AncienMain.lancerExecution();

        } catch (Exception e) {
            e.printStackTrace();
        
        }
    
    }

    /**
     * Lancement de l'application.
     */
    public static void runApp(String[] args) {
        launch(args);
    }

    /**
     * Retourne la fenêtre principale.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }





}
