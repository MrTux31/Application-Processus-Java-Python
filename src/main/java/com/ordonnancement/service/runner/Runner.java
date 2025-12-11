package com.ordonnancement.service.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.model.Metrics;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.service.AppState;
import com.ordonnancement.service.parser.metrics.MetricsParser;
import com.ordonnancement.service.parser.process.DetailedResultProcessParser;
import com.ordonnancement.service.parser.process.GlobalResultProcessParser;
import com.ordonnancement.service.parser.process.InitialProcessParser;
import com.ordonnancement.service.python.PythonLauncher;

import javafx.application.Platform;


/**
 * Classe permettant de lancer la création du fichier de configuration, lancer
 * python, récupérer les résultats et les stocker
 * @author ROMA Quentin
 */
public class Runner {

    /**
     * Exécute Runner de manière asynchrone pour ne pas bloquer JavaFX.
     *
     * @param fileConfig Configuration du fichier
     * @param destinationConfigJson Chemin du fichier JSON de config à créer
     * @param callback : L'action à exécuter après l'excecution du runner.
     * @param onException : action appelée en cas d'exception, également exécutée sur le thread JavaFX
     */
    public static void runAsync(FileConfiguration fileConfig,
            String destinationConfigJson, Runnable callBack, Consumer<Exception> onException) {

        Thread thread = new Thread(() -> {
            try {
                // Exécution synchrone du Runner
                Resultats resultats = run(fileConfig, destinationConfigJson);
                AppState.getInstance().setResultats(resultats); //Enregistre les résultats dans le singleton
                
                //On lance le callback quand on peut
                Platform.runLater(() -> {callBack.run(); }
                );

            } catch (Exception e) { //Si une exception survient
                if (onException != null) {
                    // Callback exception sur le thread JavaFX
                    Platform.runLater(() -> onException.accept(e)); //Faire remonter exécution
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Réalise les différentes étapes : lance
     * Python - parse les fichiers résultats - retourne les résultats obtenus
     * après l'exxécution de python
     *
     * @param fileConfiguration : Les paramètres du fichier de config
     * @param destinationFichierConfig: La destination du fichier de
     * configuration à utiliser
     * @return Resultats : les résultats obtenus
     * @throws RunnerException Si une erreur de lancement survient
     */
    private static Resultats run(FileConfiguration fileConfiguration, String destinationFichierConfig) throws RunnerException {

       
        Path cheminAppPython = getCheminAppPython();

        //Lancement python, execution script 
        PythonLauncher.runPythonScript(cheminAppPython, destinationFichierConfig);

        //Après exécution python : 
        List<AlgoConfiguration> listeAlgos = fileConfiguration.getListeAlgorithmes(); //On récupère les liste des algorithmes qui ont été exécutés

        //Créer le parser de fichier pour les processus initiaux
        InitialProcessParser parserFichierProcessus = new InitialProcessParser();
        //Parser le fichier et récupérer la liste des processus
        List<Process> processusInitiaux = parserFichierProcessus.parse(fileConfiguration.getFichierProcessus());
        //Créer le parser de fichier pour les résultats globaux
        GlobalResultProcessParser parserFichierResultGlobaux = new GlobalResultProcessParser(processusInitiaux);
        //Créer le parser de fichier pour les résultats détaillés
        DetailedResultProcessParser parserFichierResultDetailed = new DetailedResultProcessParser(processusInitiaux);

        //Pour chaque algorithme d'ordonnancement executé
        for (AlgoConfiguration algo : listeAlgos) {
            //Récupération des fichiers de résultats pour chaque algorithme
            String resultDetailed = algo.getFichierResultatsDetailles();
            String resultGlobal = algo.getFichierResultatsGlobaux();

            //Parse le fichier des résultats globaux et met à jour la liste des processus
            parserFichierResultGlobaux.parse(resultGlobal, algo.getNomAlgorithme());
            //Parse le fichier des résultats détaillés et met à jour la liste des processus
            parserFichierResultDetailed.parse(resultDetailed, algo.getNomAlgorithme());
        }

        //Créer le parser des métriques
        MetricsParser parserMetrics = new MetricsParser();
        //Parser le fichier des métriques et récupérer la liste des métriques
        List<Metrics> listeMetriques = parserMetrics.parse(fileConfiguration.getFichierMetriquesGlobales());

        if (processusInitiaux.isEmpty() || listeMetriques.isEmpty()) { //Si aucun processus / metriques récupérés
            throw new RunnerException("Vous n'avez exécuté aucun processus !");
        }
        //On return les résultats obtenus après l'exécution du script python
        return new Resultats(processusInitiaux, listeMetriques);

        //TO DO : Stocker gloabalement ces résultats dans un SINGLETON : ResultatsManager pour pouvoir y
        //accéder depuis n'importe où dans l'app.
    }

    /**
     * Permet d'obtenir le chemin du fichier python depuis l'emplacement du JAR
     *
     * @return le chemin de l'executable python
     * @throws RunnerException Si le chemin est innexistant
     */
    private static Path getCheminAppPython() throws RunnerException {

        // Path vers le fichier python
        Path pythonPath = Paths.get("python/appProcess.py");
        //Vérification du chemin
        if (!Files.exists(pythonPath)) {
            throw new RunnerException("Le script Python est introuvable : " + pythonPath.toAbsolutePath()
                    + "\nVérifiez que le dossier 'python' existe et que 'appProcess.py' est présent.");
        }

        return pythonPath;

    }

}
