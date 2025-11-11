package com.ordonnancement.model;

import java.util.List;

/**
 * Classe permetant de stocker les résultats obtenus après
 * l'exécution du script python et d'y accéder facilement
 */

public class Resultats {


    private final List<Process> listeProcessus;
    private final List<Metrics> listeMetriques;

    public Resultats(List<Process> listeProcessus, List<Metrics> listeMetriques){
        this.listeProcessus = listeProcessus;
        this.listeMetriques = listeMetriques;
    }

    public List<Process> getListeProcessus(){
        return this.listeProcessus;
    }

    public List<Metrics> getListeMetrics(){
        return this.listeMetriques;
    }


}
