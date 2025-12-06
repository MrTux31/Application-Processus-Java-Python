package com.ordonnancement.model;

/**
 * Représente les métriques globales obtenues par algorithme d'ordonnacement
 * @author Antonin Le Donné
 */

public class Metrics {
    private final String nomAlgo;
    private final double tempsReponseMoyen; //Temps réponse = délai entre la soumission du processus et le moment où il commence à s'executer pour la première fois
    private final double tempsAttenteMoyen; //Attente = le processus est pret mais il attends de disposer d'un processeur pour s'executer
    private final int makespan; //Le makespan : la date de fin du dernier processus

    /**
     * Constructeur des métriques
     * @param nomAlgorithme : Le nom de l'algorithme utilisé pour l'ordonnancement
     * @param tempsReponseMoyen : Temps réponse = délai entre la soummussion du processus et le moment où il commence à s'executer pour la première fois
     * @param tempsAttenteMoyen : Attente = le processus est pret mais il attends de disposer d'un processeur pour s'executer
     * @param makespan : La date de fin du dernier processus
     */
    public Metrics(String nomAlgorithme, double tempsReponseMoyen, double tempsAttenteMoyen, int makespan){
        this.nomAlgo = nomAlgorithme;
        this.tempsAttenteMoyen = tempsAttenteMoyen;
        this.tempsReponseMoyen = tempsReponseMoyen;
        this.makespan = makespan;

    }

    /**
     * Permet de récupérer le temps de réponse moyen
     * Temps réponse = délai entre la soummussion du processus et le moment où il commence à s'executer pour la première fois
     */
    public double getTempsReponseMoyen() {
        return tempsReponseMoyen;
    }

    /**
    * Permet de récupérer le temps d'attente moyen
    * Attente = le processus est pret mais il attends de disposer d'un processeur pour s'executer 
    */
    public double getTempsAttenteMoyen() {
        return tempsAttenteMoyen;
    }

    /**
     * Permet de récupérer le makespan
     * Le makespan : la date de fin du dernier processus executé
     */
    public int getMakespan() {
        return makespan;
    }

    /**
     * Permet de récupérer le nom de l'algorithme d'ordonnacement utilisé
     */
    public String getNomAlgorithme(){
        return this.nomAlgo;
    }


    


}
