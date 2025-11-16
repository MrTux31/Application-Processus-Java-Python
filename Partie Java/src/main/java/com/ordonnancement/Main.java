package com.ordonnancement;

import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.model.Metrics;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.service.runner.Runner;
import com.ordonnancement.ui.controller.GanttProcessorController;
import com.ordonnancement.util.ProcessUtils;

public class Main {
    public static void main(String[] args) {
        




        //Démo création du fichier de configuration + récupération des résultats python////////////////////////////////////////////

       
        //Création des algos qui vont être utilisés par l'ordonnanceur
        AlgoConfiguration algo1 = new AlgoConfiguration("ROUND ROBIN",  "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rDetailedROUNDROBIN.csv", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rGlobauxROUNDROBIN.csv", 2);
        AlgoConfiguration algo2 = new AlgoConfiguration("FIFO", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rDetailedFIFO.csv", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rGlobauxFIFO.csv", null);
        //Ajouts de ces algos dans une liste
        List<AlgoConfiguration> liste = new ArrayList<>();
        liste.add(algo1);
        liste.add(algo2);
        //Création de l'objet FileConfig représentant le fichier de configuration
        FileConfiguration fileConfig = new FileConfiguration("C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\processusInitiaux.csv", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\fichierMetriquesGlobales.csv","C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\Ressources.json", liste);
       
        try {
            //Lancer l'execution / écriture fichier config + récup des résultats de python
            Resultats resultats = Runner.run(fileConfig,"C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\config.json");
            //Affichage résultats

            System.out.println("--------------------------------------------Affichage processus--------------------------------------------");
            //Test affichage des processus : 
            affichage(resultats.getListeProcessus());
            System.out.println("\n");
            System.out.println("--------------------------------------------Affichage Metrics--------------------------------------------");

            //Test affichage des Métriques : 
            afficherMetrics(resultats.getListeMetrics());
            

            GanttProcessorController.runApp(resultats);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage()); // Affiche juste le message principal
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


    public static void affichage(List<Process> processusInitiaux){

        for(Process p : processusInitiaux){

            System.out.println("\n------------------------------------------------------------");
            System.out.printf("Processus %s%n", p.getId());
            System.out.println("------------------------------------------------------------");
            System.out.printf("Date de soumission : %d%n", p.getDateSoumission());
            System.out.printf("Temps d'exécution  : %d%n", p.getTempsExecution());
            System.out.printf("RAM requise        : %d%n", p.getRequiredRam());
            System.out.printf("Deadline            : %d%n", p.getDeadline());
            System.out.printf("Priorité            : %d%n", p.getPriority());
            System.out.println();

            List<String> algos = ProcessUtils.getNomAlgos(p); //Récupération des différents algos qui ont executé le processus

            for(String nom : algos){ //Parcours des différents algos qui ont executé le processus
                System.out.println("------------------------------------------------------------");
                System.out.println("Algorithme : " + nom);
                System.out.println("------------------------------------------------------------");

                ExecutionInfo info = ProcessUtils.getExecution(p, nom); //Récupération de l'execution de ce processus pour cet algo la

                // Affichage des infos globales
                System.out.printf("  Date début exécution : %d%n", info.getDateDebut());
                System.out.printf("  Date fin exécution   : %d%n", info.getDateFin());

                List<Allocation> listeAssignationProcessus = ProcessUtils.getAllocations(p, nom); //On récupère les assignations pour ce processus et cet algo
                
                System.out.println("  Détails des exécutions :");
                for (Allocation s : listeAssignationProcessus) {
                    System.out.printf(
                        "     - CPU %-6s | Début : %3d | Fin : %3d%n",
                        s.getProcessor(),
                        s.getDateDebutExecution(),
                        s.getDateFinExecution()
                    );
                }

            }

        }


    }


}