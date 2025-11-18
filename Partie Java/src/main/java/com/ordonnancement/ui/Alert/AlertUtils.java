package com.ordonnancement.ui.Alert;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Classe utilitaire pour la gestion des alertes JavaFX.
 *
 */
public class AlertUtils {
    /**
     * Constructeur sans paramètres privé
     */
    private AlertUtils() {
        // Constructeur privé pour empêcher l'instanciation
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur.
     * La boîte de dialogue est centrée par rapport à sa fenêtre parente.
     * 
     * @param title le titre de la boîte de dialogue
     * @param message le message d'erreur à afficher
     * @param owner la fenêtre parente par rapport à laquelle centrer la boîte de dialogue
     */
    public static void showError(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
            DialogUtils.centerDialog((Stage) alert.getDialogPane().getScene().getWindow(), owner);
        }
        alert.showAndWait();
    }
}