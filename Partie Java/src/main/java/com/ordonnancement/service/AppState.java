package com.ordonnancement.service;

import com.ordonnancement.model.Resultats;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Antonin Le Donné
 * Singleton responsable du stockage centralisé des résultats produits par
 * l'ordonnanceur Python. L'utilisation de ce singleton garantit qu'un unique
 * jeu de résultats est manipulé dans l'application après l'exécution du
 * programme Python.
 *
 */
public class AppState {

    /*
     * Instance unique du singleton AppState
     */
    private static final AppState instance = new AppState();

    /**
     * Proproité listenable Permet de savoir si l'ordonnancement est terminé
     */
    private final BooleanProperty executionTerminee = new SimpleBooleanProperty(false);

    /*
     * Résultats complets retournés par l'exécution Python
     */
    private Resultats resultats;

    /*
     * Constructeur privé pour empêcher l'instanciation directe.
     */
    private AppState() {
    }

    /**
     * Retourne l'instance unique du singleton AppState.
     *
     * @return L'instance unique de AppState
     */
    public final static AppState getInstance() {
        return instance;
    }

    /**
     * Retourne les résultats actuellement stockés.
     *
     * @return Les résultats de l'ordonnanceur
     * @throws IllegalStateException si aucun résultat n'a encore été enregistré
     */
    public Resultats getResultats() {
        if (resultats == null) {
            throw new IllegalStateException(
                    "Aucun résultat n'a encore été enregistré dans AppState !");
        }
        return resultats;
    }

    /**
     * Définit les résultats retournés par l'exécution du programme Python.
     *
     * @param resultats L'objet contenant l'ensemble des données produites par
     * le programme python
     */
    public void setResultats(Resultats resultats) {
        this.resultats = resultats;
    }

    public BooleanProperty executionTermineeProperty() {
        return executionTerminee;
    }

    public boolean isExecutionTerminee() {
        return executionTerminee.get();
    }

    public void setExecutionTerminee(boolean value) {
        executionTerminee.set(value);
    }

}
