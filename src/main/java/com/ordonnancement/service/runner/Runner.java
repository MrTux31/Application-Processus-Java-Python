package com.ordonnancement.service.runner;
import java.util.List;

import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.model.Metrics;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.service.configuration.ConfigurationWriter;
import com.ordonnancement.service.parser.metrics.MetricsParser;
import com.ordonnancement.service.parser.process.ProcessParser;
import com.ordonnancement.service.parser.process.strategie.DetailedResultProcessParser;
import com.ordonnancement.service.parser.process.strategie.GlobalResultProcessParser;
import com.ordonnancement.service.parser.process.strategie.InitialProcessParser;

/**
 * Classe permettant de lancer la création du fichier de configuration,
 * lancer python, récupérer les résultats et les stocker
 */

public class Runner {


    /**
     * Réalise les différentes étapes : 
     * - écrit le fichier de config
     * - lance Python
     * - parse les fichiers résultats
     * - retourne les résultats obtenus après l'exxécution de python
     * @param fileConfiguration : Les paramètres du fichier de config
     * @param destinationFichierConfig: La destination du fichier de configuration à créer
     * @return Resultats : les résultats obtenus
     */
    public static Resultats run(FileConfiguration fileConfiguration, String destinationFichierConfig){
        
        //Création du configuration writer
        ConfigurationWriter writer = new ConfigurationWriter();
      
        //Créer le fichier JSON de configuration
        writer.writeConfiguration(fileConfiguration,destinationFichierConfig);
        
        
        //Lancement python, execution script ( à faire)
        // TO DO : lancement du script python depuis le service -> PythonLauncher (à créer)
        //
        
        
        
        
        //Après exécution python : 
    
        List<AlgoConfiguration> listeAlgos = fileConfiguration.getListeAlgorithmes(); //On récupère les liste des algorithmes qui ont été exécutés
       
        //Créer le parser de fichier pour les processus initiaux
        ProcessParser parserFichierProcessus = new ProcessParser(new InitialProcessParser());
        //Parser le fichier et récupérer la liste des processus
        List<Process> processusInitiaux = parserFichierProcessus.parse(fileConfiguration.getFichierProcessus());
        //Créer le parser de fichier pour les résultats globaux
        ProcessParser parserFichierResultGlobaux = new ProcessParser(new GlobalResultProcessParser(processusInitiaux));
         //Créer le parser de fichier pour les résultats détaillés
        ProcessParser parserFichierResultDetailed = new ProcessParser(new DetailedResultProcessParser(processusInitiaux));


        //Pour chaque algorithme d'ordonnancement executé
        for(AlgoConfiguration algo : listeAlgos){
            //Récupération des fichiers de résultats pour chaque algorithme
            String resultDetailed = algo.getFichierResultatsDetailles();
            String resultGlobal = algo.getFichierResultatsGlobaux();
        
            //Parse le fichier des résultats globaux et met à jour la liste des processus
            parserFichierResultGlobaux.parse(resultGlobal);
            //Parse le fichier des résultats détaillés et met à jour la liste des processus
            parserFichierResultDetailed.parse(resultDetailed);
        }


         //Créer le parser des métriques
        MetricsParser parserMetrics = new MetricsParser();
        //Parser le fichier des métriques et récupérer la liste des métriques
        List<Metrics> listeMetriques = parserMetrics.parse(fileConfiguration.getFichierMetriquesGlobales());
        
        
        //On return les résultats obtenus après l'exécution du script python
        return new Resultats(processusInitiaux, listeMetriques);
        

    }


}
