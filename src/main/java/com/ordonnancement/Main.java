package com.ordonnancement;

import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.model.Metrics;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Schedule;
import com.ordonnancement.service.configuration.ConfigurationWriter;
import com.ordonnancement.service.parser.FileParser;
import com.ordonnancement.service.parser.implementation.metrics.MetricsParser;
import com.ordonnancement.service.parser.implementation.process.ProcessParser;
import com.ordonnancement.service.parser.implementation.process.strategie.DetailedResultProcessParser;
import com.ordonnancement.service.parser.implementation.process.strategie.GlobalResultProcessParser;
import com.ordonnancement.service.parser.implementation.process.strategie.InitialProcessParser;

public class Main {
    public static void main(String[] args) {
        
        //Démo création du fichier de configuration////////////////////////////////////////////

        ConfigurationManager manager = ConfigurationManager.getInstance(); //Récup l'instance du configuration manager

        //Création des algos qui vont être utilisés par l'ordonnanceur
        AlgoConfiguration algo1 = new AlgoConfiguration("ROUND ROBIN",  "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\ResultDetailles", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\ResultGlobaux", 9);
        AlgoConfiguration algo2 = new AlgoConfiguration("FIFO", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\ResultDetailles2", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\ResultGlobaux2", null);
        //Ajouts de ces algos dans une liste
        List<AlgoConfiguration> liste = new ArrayList<>();
        liste.add(algo1);
        liste.add(algo2);
        //Création de l'objet FileConfig représentant le fichier de configuration
        FileConfiguration fileConfig = new FileConfiguration("C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\config.json", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\Metriques","C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\Ressources", liste);
        //On enregistre cet objet dans le manager
        manager.setFileConfiguration(fileConfig);
        //Création du configuration writer
        ConfigurationWriter writer = new ConfigurationWriter();
        //Créer le fichier JSON de configuration
        writer.writeConfiguration("C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\config.json");

        //Démo Parsing/////////////////////////////////////////////////////////////////////

       //Créer le parser de fichier pour les processus initiaux
        FileParser parserFichierProcessus = new ProcessParser(new InitialProcessParser());
        //Parser le fichier et récupérer la liste des processus
        List<Process> processusInitiaux = parserFichierProcessus.parse("C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\processInitiaux.json");

        
        //Créer le parser de fichier pour les résultats détaillés
        FileParser parserFichierResultDetailed = new ProcessParser(new DetailedResultProcessParser(processusInitiaux));
        //Parse le fichier des résultats détaillés et met à jour la liste des processus
        parserFichierResultDetailed.parse("C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rDetailed.json");

        //Créer le parser de fichier pour les résultats globaux
        FileParser parserFichierResultGlobaux = new ProcessParser(new GlobalResultProcessParser(processusInitiaux));
        //Parse le fichier des résultats globaux et met à jour la liste des processus
        parserFichierResultGlobaux.parse("C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rGlobaux.json");

        //Créer le parser des métriques
        FileParser parserMetrics = new MetricsParser();
        //Parser le fichier des métriques et récupérer la liste des métriques
        List<Metrics> metriques = parserMetrics.parse("\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\metrics.json");

        System.out.println("--------------------------------------------Affichage processus--------------------------------------------");
        //Test affichage des processus : 
        afficherProcessus(processusInitiaux);
        System.out.println("\n");
        System.out.println("--------------------------------------------Affichage Metrics--------------------------------------------");

        //Test affichage des Métriques : 
        afficherMetrics(metriques);



    }

    // Méthode utilitaire pour afficher les processus
    public static void afficherProcessus(List<Process> processusInitiaux) {
        
        for (Process p : processusInitiaux) {
            System.out.println("ID | Soumission | Début | Fin | Exécution | RAM Req | Deadline | Priorité");
            System.out.println("--------------------------------------------------------------------------");
            System.out.printf("%2d | %9d | %5d | %3d | %9d | %7d | %8d | %7d%n",
                    p.getId(),
                    p.getDateSoumission(),
                    p.getDateDebut(),
                    p.getDateFin(),
                    p.getTempsExecution(),
                    p.getRequiredRam(),
                    p.getDeadline(),
                    p.getPriority()
            );
            if (!p.getListSchedules().isEmpty()) {
                System.out.println("  Schedules:");
                for (Schedule s : p.getListSchedules()) {
                    System.out.print("Processeur"+ s.getProcessor().getId() +" Date debut : " + s.getDateDebutExecution() + " Date fin : " + s.getDateFinExecution()+"\n");
                }
            }
        }
    }

    public static void afficherMetrics(List<Metrics> metricsList) {
       
        for (Metrics m : metricsList) {
             System.out.println("Algorithme       | Temps Réponse | Temps Attente | Makespan");
            System.out.println("-----------------------------------------------------------");
            System.out.printf("%-15s | %13.2f | %13.2f | %7d%n",
                    m.getNomAlgorithme(),
                    m.getTempsReponseMoyen(),
                    m.getTempsAttenteMoyen(),
                    m.getMakespan()
            );
        }
    }


}