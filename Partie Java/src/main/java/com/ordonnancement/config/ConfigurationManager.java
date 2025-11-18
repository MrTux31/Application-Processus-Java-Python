package com.ordonnancement.config;

import java.nio.file.Path;

import com.ordonnancement.model.FileConfiguration;

/**
 * Singleton responsable de la gestion du fichier de configuration de
 * l'ordonnanceur. Permet d'assurer qu'une seule configuration est utilisée dans
 * l'application.
 */
public class ConfigurationManager {

    private static final ConfigurationManager instance = new ConfigurationManager(); //Instance du singleton
    private FileConfiguration fileConfiguration; //L'objet de configuration du fichier actuellement utilisé
    private Path cheminFichierConfiguration; //L'emplacement du fichier

    /**
     * Constructeur privé
     */
    private ConfigurationManager() {

    }

    /**
     * Méthode permettant de récupérer l'instance du singleton
     * ConfigurationManager
     *
     * @return L'instance unique de ConfigurationManager
     */
    public static final ConfigurationManager getInstance() {
        return instance;
    }

    /**
     * Permet de définir la configuration de l'ordonnanceur utilisée
     *
     * @param fileConfiguration : La configuration
     */
    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    /**
     * Permet de récupérer la configuration de l'ordonnanceur utilisée
     *
     * @return fileConfiguration : La configuration utilisée par l'ordonnanceur
     */
    public FileConfiguration getFileConfiguration() {
        if (fileConfiguration == null) {
            throw new IllegalStateException("Aucune configuration n'est encore définie !");
        }
        return this.fileConfiguration;
    }

    /**
     * Permet d'obtenir la localisation du fichier de configuration
     *
     * @return
     */
    public Path getCheminFichierConfig() {
        if (this.cheminFichierConfiguration == null) {
            throw new IllegalStateException("Le chemin du fichier de configuration n'est pas défini !");
        }
        return this.cheminFichierConfiguration;
    }

    /**
     * Définit l'emplacement du fichier de configuration.
     *
     * @param cheminFichierConfiguration chemin du fichier config
     */
    public void setCheminFichierConfig(Path cheminFichierConfiguration) {
        this.cheminFichierConfiguration = cheminFichierConfiguration;
    }
}
