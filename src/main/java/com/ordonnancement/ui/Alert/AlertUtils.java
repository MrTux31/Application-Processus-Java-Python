package com.ordonnancement.ui.Alert;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
     * Affiche une boîte de dialogue d'erreur. La boîte de dialogue est centrée
     * par rapport à sa fenêtre parente.
     *
     * @param title le titre de la boîte de dialogue
     * @param message le message d'erreur à afficher
     * @param owner la fenêtre parente par rapport à laquelle centrer la boîte
     * de dialogue
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

    /**
     * Affiche une boîte de dialogue d'information. La boîte de dialogue est
     * centrée par rapport à sa fenêtre parente.
     *
     * @param title le titre de la boîte de dialogue
     * @param message le message d'information à afficher
     * @param owner la fenêtre parente par rapport à laquelle centrer la boîte
     * de dialogue
     */
    public static void showInfo(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
            DialogUtils.centerDialog((Stage) alert.getDialogPane().getScene().getWindow(), owner);
        }
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue de confirmation centrée par rapport à une
     * fenêtre parente. Cette méthode bloque l'exécution jusqu'à ce que
     * l'utilisateur choisisse une option.
     *
     * @param title le titre de la boîte de dialogue
     * @param message le message d'information à afficher
     * @param owner la fenêtre parente par rapport à laquelle centrer la boîte
     * de dialogue (peut être null)
     * @return true si l'utilisateur a cliqué sur "OK", false sinon
     */
    public static boolean showConfirmation(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (owner != null) {
            alert.initOwner(owner);
            DialogUtils.centerDialog((Stage) alert.getDialogPane().getScene().getWindow(), owner);
        }

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

}
