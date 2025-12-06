package com.ordonnancement.model;

import java.util.List;

/**
 * Classe permetant de stocker les résultats obtenus après
 * l'exécution du script python et d'y accéder facilement
 * @author ROMA Quentin
 */

public class Resultats {


    private final List<Process> listeProcessus;
    private final List<Metrics> listeMetriques;
    /**
     * Constructeur
     * @param listeProcessus : la liste des processus après ordonnancement
     * @param listeMetriques : la liste des métriques après ordonanncement
     */
    public Resultats(List<Process> listeProcessus, List<Metrics> listeMetriques){
        this.listeProcessus = listeProcessus;
        this.listeMetriques = listeMetriques;
    }

    /**
     * Renvoie la liste des processus chargés, avec leurs allocations
     * et informations d'exécution pour chaque algorithme.
     *
     * @return la liste des processus
     */
    public List<Process> getListeProcessus(){
        return this.listeProcessus;
    }
    /**
     * Renvoie la liste des métriques globales calculées pour chaque
     * algorithme d'ordonnancement.
     *
     * @return la liste des métriques
     */
    public List<Metrics> getListeMetrics(){
        return this.listeMetriques;
    }


}
