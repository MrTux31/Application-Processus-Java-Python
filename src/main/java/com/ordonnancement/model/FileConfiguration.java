package com.ordonnancement.model;

import java.util.List;

/**
 * Classe représentant le fichier de configuration json utilisée dans python.
 * Elle de répertorier toutes les informations nécessaires à la
 * génération du fichier de configuration pour python
 * @author Allemane Axel
 */
public class FileConfiguration {

    private final String fichierProcessus; //Le fichier des processus à ordonnancer
    private final String fichierRessourcesDisponibles;
    private final String fichierMetriquesGlobales;
    private final List<AlgoConfiguration> listeAlgorithmes; //La liste des algorithmes d'ordonnancement qui seront utilisés

    /**
     * Constructeur prenant en paramètres toutes les données nécessaires à la
     * création du fichier de configuration pour python.
     *
     * @param fichierProcessus : Le chemin du fichier des processus à ordonnacer
     * @param fichierMetriquesGlobales : Le chemin du fichier des métriques
     * globales
     * @param fichierRessourcesDisponibles : Le chemin du fichier détaillant les
     * ressources disponibles
     * @param listeAlgorithmes : La liste des différents algo d'ordonnancement
     * qui seront utilisés par python, regroupe également les différents
     * fichiers de sortie / quantum des algos
     */
    public FileConfiguration(String fichierProcessus, String fichierMetriquesGlobales, String fichierRessourcesDisponibles, List<AlgoConfiguration> listeAlgorithmes) {
        //Vérifications de sécurité sur les paramètres
        if (fichierProcessus == null || fichierProcessus.isBlank()) {
            throw new IllegalArgumentException("Le chemin du fichier des processus doit être renseigné (non null et non vide).");
        }

        if (fichierRessourcesDisponibles == null || fichierRessourcesDisponibles.isBlank()) {
            throw new IllegalArgumentException("Le chemin du fichier des ressources disponibles doit être renseigné (non null et non vide).");
        }

        if (fichierMetriquesGlobales == null || fichierMetriquesGlobales.isBlank()) {
            throw new IllegalArgumentException("Le chemin du fichier des métriques globales doit être renseigné (non null et non vide).");
        }
        //Check que la liste algo n'est pas null / vide
        if (listeAlgorithmes == null || listeAlgorithmes.isEmpty()) {
            throw new IllegalArgumentException("La liste des algorithmes doit posséder au moins un algo et ne pas être nulle");
        }
        //Check que la liste des algo ne contient pas d'élément null
        for (AlgoConfiguration algo : listeAlgorithmes) {
            if (algo == null) {
                throw new IllegalArgumentException("Aucun algorithme de la liste ne doit être null");
            }
        }

        this.fichierMetriquesGlobales = fichierMetriquesGlobales.trim();
        this.fichierProcessus = fichierProcessus.trim();
        this.fichierRessourcesDisponibles = fichierRessourcesDisponibles.trim();
        this.listeAlgorithmes = List.copyOf(listeAlgorithmes); //Faire une copie de la liste pour empecher toutes modifs après

    }
    /**
     * Renvoie le chemin du fichier contenant la liste des processus à ordonnancer.
     *
     * @return le chemin du fichier des processus
     */
    public String getFichierProcessus() {
        return fichierProcessus;
    }
    /**
     * Renvoie le chemin du fichier décrivant les ressources disponibles
     * (processeurs, RAM).
     *
     * @return le chemin du fichier des ressources disponibles
     */
    public String getFichierRessourcesDisponibles() {
        return fichierRessourcesDisponibles;
    }

     /**
     * Renvoie la liste des algorithmes d'ordonnancement à exécuter,
     * accompagnés de leurs configurations (fichiers de sortie, quantum, etc.).
     *
     * @return la liste des configurations d'algorithmes
     */
    public List<AlgoConfiguration> getListeAlgorithmes() {
        return this.listeAlgorithmes;
    }

    /**
     * Renvoie le chemin du fichier contenant les métriques globales
     * produites par les ordonnancements.
     *
     * @return le chemin du fichier des métriques globales
     */
    public String getFichierMetriquesGlobales() {
        return fichierMetriquesGlobales;
    }

}
