package com.ordonnancement.model;
/**
 * Classe permettant représentant un algorithme 
 * d'ordonnancement qui va etre executé par python.
 * Permet de regrouper toutes les informations concernant cet algorithme.
 */
public class AlgoConfiguration {

    private final String nomAlgorithme; //Le non de l'algo d'ordonnacement
    private final String fichierResultatsDetailles; //Chemin du fichier des résultats détaillés de cet algo
    private final String fichierResultatsGlobaux;//Chemin du fichier des résultats globaux de cet algo
    private final Integer quantum; //null si non nécessaire pour l'algorithme
    
    /**
     * Constructeur pour configurer un algorithme d'ordonnancement
     * @param nomAlgorithme : Le nom de l'algorithme d'ordonnancement
     * @param fichierResultatsDetailles : Le chemin du fichier des résultats détaillés de l'algo
     * @param fichierResultatsGlobaux : Le chemin du fichier des résultats globaux de l'algo
     * @param quantum
     */
    public AlgoConfiguration(String nomAlgorithme, String fichierResultatsDetailles, String fichierResultatsGlobaux, Integer quantum) {
        //Vérifications de sécurité sur les paramètres
        if (nomAlgorithme == null || nomAlgorithme.isBlank()){
            throw new IllegalArgumentException("Le nom de l'algorithme est obligatoire.");
        }
        if (fichierResultatsDetailles == null || fichierResultatsDetailles.isBlank()){
            throw new IllegalArgumentException("Le fichier de résultats détaillés est obligatoire pour " + nomAlgorithme);
        }
        if (fichierResultatsGlobaux == null || fichierResultatsGlobaux.isBlank()){
            throw new IllegalArgumentException("Le fichier de résultats globaux est obligatoire pour " + nomAlgorithme);
        }
        if(quantum!= null && quantum <= 0){
            throw new IllegalArgumentException("Un quantum peut etre null s'il n'y en a pas, sinon il doit être supérieur ou égal à 1.");
        }

        this.nomAlgorithme = nomAlgorithme.trim();
        this.fichierResultatsDetailles = fichierResultatsDetailles.trim();
        this.fichierResultatsGlobaux = fichierResultatsGlobaux.trim();
        this.quantum = quantum;
    }

    public String getNomAlgorithme() {
        return nomAlgorithme;
    }

    public String getFichierResultatsDetailles() {
        return fichierResultatsDetailles;
    }

    public String getFichierResultatsGlobaux() {
        return fichierResultatsGlobaux;
    }

    public Integer getQuantum() {
        return quantum;
    }



     

}
