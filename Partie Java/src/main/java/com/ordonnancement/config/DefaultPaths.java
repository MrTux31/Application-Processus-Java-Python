package com.ordonnancement.config;
/**
 * Enum utilisé pour sauvegarder les chemins par défaut des fichiers (entrée / sortie) pour le fichier de configuration
 */
public enum DefaultPaths {

    PROCESS_INIT("python/Settings/processusInitiaux.csv"), //Les processus initiaux
    RESSOURCES_INIT("python/Settings/ressources.json"), //Le fichier des ressources dispos
    METRIQUES_GLOBAL("python/Settings/fichierMetriquesGlobales.csv"), //Le fichier des métriques globales
    // ===================== FIFO =====================
    R_DETAILLED_FIFO("python/Resultats/FIFO/rDetailled.csv"),
    R_GLOBAL_FIFO("python/Resultats/FIFO/rGlobaux.csv"),

    // ===================== PRIORITÉ =====================
    R_DETAILLED_PRIORITE("python/Resultats/Priorite/rDetailled.csv"),
    R_GLOBAL_PRIORITE("python/Resultats/Priorite/rGlobaux.csv"),

    // ===================== ROUND ROBIN =====================
    R_DETAILLED_RR("python/Resultats/RoundRobin/rDetailled.csv"),
    R_GLOBAL_RR("python/Resultats/RoundRobin/rGlobaux.csv");


    private final String path;

    DefaultPaths(String p) { this.path = p; }

    public String getPath() { return path; }
}