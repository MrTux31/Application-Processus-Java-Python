package com.ordonnancement.config;

import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.parser.config.ConfigParser;

/**
 * Singleton responsable de la gestion du fichier de configuration de
 * l'ordonnanceur. Permet d'assurer qu'une seule configuration est utilisée dans
 * l'application.
 */
public class ConfigurationManager {

    private static final ConfigurationManager instance = new ConfigurationManager(); //Instance du singleton
    private FileConfiguration fileConfiguration; //L'objet de configuration du fichier actuellement utilisé
    private final String cheminFichierConfiguration = "python/Settings/config.json"; //L'emplacement du fichier

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
     * @return cheminFichierConfiguration : Le chemin de l'emplacement du fichier de configuration
     */
    public String getCheminFichierConfig() {
        return this.cheminFichierConfiguration;
    }

    /**
     * Méthode permettant de charger automatiquement le fichier de configuration existant
     * @throws ConfigParseException
     */
    public void loadConfiguration() throws FileParsingException {
        this.fileConfiguration = ConfigParser.parse(cheminFichierConfiguration);
    }

    
}
