package com.ordonnancement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.FileConfiguration;
import com.ordonnancement.model.Metrics;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.model.Schedule;
import com.ordonnancement.service.runner.Runner;

public class Main {
    public static void main(String[] args) {
        
        //Démo création du fichier de configuration + récupération des résultats python////////////////////////////////////////////

       
        //Création des algos qui vont être utilisés par l'ordonnanceur
        AlgoConfiguration algo1 = new AlgoConfiguration("ROUND ROBIN",  "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rDetailedROUNDROBIN.json", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rGlobauxROUNDROBIN.json", 9);
        AlgoConfiguration algo2 = new AlgoConfiguration("FIFO", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rDetailedFIFO.json", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rGlobauxFIFO.json", null);
        //Ajouts de ces algos dans une liste
        List<AlgoConfiguration> liste = new ArrayList<>();
        liste.add(algo1);
        liste.add(algo2);
        //Création de l'objet FileConfig représentant le fichier de configuration
        FileConfiguration fileConfig = new FileConfiguration("C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\processInitiaux.json", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\metrics.json","C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\Ressources", liste);
       
        //Lancer l'execution / écriture fichier config + récup des résultats de python
        Resultats resultats = Runner.run(fileConfig,"C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\config.json");



        //Affichage résultats

        System.out.println("--------------------------------------------Affichage processus--------------------------------------------");
        //Test affichage des processus : 
        afficherProcessus(resultats.getListeProcessus());
        System.out.println("\n");
        System.out.println("--------------------------------------------Affichage Metrics--------------------------------------------");

        //Test affichage des Métriques : 
        afficherMetrics(resultats.getListeMetrics());



    }




/**
 * Affiche tous les processus et leurs informations détaillées,
 * y compris les exécutions par algorithme d'ordonnancement.
 */
public static void afficherProcessus(List<Process> processusInitiaux) {

    System.out.println("=======================================================================");
    System.out.println("                        LISTE DES PROCESSUS");
    System.out.println("=======================================================================");

    for (Process p : processusInitiaux) {

        HashMap<String,ExecutionInfo> executions = p.getAllExecutions();

        if(!executions.isEmpty()){

        }

        System.out.println("\n------------------------------------------------------------");
        System.out.printf("Processus %d%n", p.getId());
        System.out.println("------------------------------------------------------------");
        System.out.printf("Date de soumission : %d%n", p.getDateSoumission());
        System.out.printf("Temps d'exécution  : %d%n", p.getTempsExecution());
        System.out.printf("RAM requise        : %d%n", p.getRequiredRam());
        System.out.printf("Deadline            : %d%n", p.getDeadline());
        System.out.printf("Priorité            : %d%n", p.getPriority());
        System.out.println();

        

        // Parcours des résultats par algorithme
        for (Map.Entry<String, ExecutionInfo> entry : executions.entrySet()) {
            String nomAlgo = entry.getKey();
            ExecutionInfo info = entry.getValue();

            System.out.println("------------------------------------------------------------");
            System.out.println("Algorithme : " + nomAlgo);
            System.out.println("------------------------------------------------------------");

            // Affichage des infos globales
            System.out.printf("  Date début exécution : %d%n", info.getDateDebut());
            System.out.printf("  Date fin exécution   : %d%n", info.getDateFin());
            

            // Affichage des exécutions détaillées
            List<Schedule> schedules = info.getListSchedules();
            if (schedules == null || schedules.isEmpty()) {
                System.out.println("  Aucune exécution détaillée.");
            } else {
                System.out.println("  Détails des exécutions :");
                for (Schedule s : schedules) {
                    System.out.printf(
                        "     - CPU %-6s | Début : %3d | Fin : %3d%n",
                        s.getProcessor().getId(),
                        s.getDateDebutExecution(),
                        s.getDateFinExecution()
                    );
                }
            }
        }
    }

    System.out.println("\n=======================================================================");
    System.out.println("                         FIN DE L’AFFICHAGE");
    System.out.println("=======================================================================");
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