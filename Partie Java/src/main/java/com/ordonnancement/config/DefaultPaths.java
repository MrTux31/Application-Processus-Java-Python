package com.ordonnancement.config;
/**
 * Enum utilisé pour sauvegarder les chemins par défaut des fichiers (entrée / sortie) pour le fichier de configuration
 *  @author Allemane Axel
 */
public enum DefaultPaths {

    /**
     * Chemin du fichier des processus initiaux par défaut
     */
    PROCESS_INIT("python/Settings/processusInitiaux.csv"), //Les processus initiaux
    /**
     * Chemin du ficher des ressources par défaut
     */
    RESSOURCES_INIT("python/Settings/ressources.json"), //Le fichier des ressources dispos
    /**
     * Chemin du fichiers des métriques par défaut
     */
    METRIQUES_GLOBAL("python/Resultats/Metriques/MetriquesGlobales.csv"), //Le fichier des métriques globales
    // ===================== FIFO =====================
    /**
     * Chemin des résultats détaillés de l'algo FIFO par défaut
     */
    R_DETAILLED_FIFO("python/Resultats/Fifo/rDetailled.csv"),
    /**
     * Chemin des résultats globaux de l'algo FIFO par défaut
     */
    R_GLOBAL_FIFO("python/Resultats/Fifo/rGlobaux.csv"),

    // ===================== PRIORITÉ =====================
    /**
     * Chemin des résultats détaillés de l'algo Priorité par défaut
     */
    R_DETAILLED_PRIORITE("python/Resultats/Priorite/rDetailled.csv"),
    /**
     * Chemin des résultats globaux de l'algo Priorité par défaut
     */
    R_GLOBAL_PRIORITE("python/Resultats/Priorite/rGlobaux.csv"),

    // ===================== ROUND ROBIN =====================
     /**
     * Chemin des résultats détaillés de l'algo Round Robin par défaut
     */
    R_DETAILLED_RR("python/Resultats/RoundRobin/rDetailled.csv"),
     /**
     * Chemin des résultats globaux de l'algo Round Robin par défaut
     */
    R_GLOBAL_RR("python/Resultats/RoundRobin/rGlobaux.csv");


    private final String path;

    DefaultPaths(String p) { this.path = p; }

    public String getPath() { return path; }
}