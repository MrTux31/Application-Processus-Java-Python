package com.ordonnancement;

import java.util.List;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Metrics;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Resultats;
import com.ordonnancement.service.AppState;
import com.ordonnancement.util.ProcessUtils;

public class AncienMain {

    public static void AfficherResultats() {
        Resultats resultats = AppState.getInstance().getResultats();
        System.out.println("--------------------------------------------Affichage processus--------------------------------------------");
        AncienMain.affichage(resultats.getListeProcessus());
        System.out.println("\n--------------------------------------------Affichage Metrics--------------------------------------------");
        AncienMain.afficherMetrics(resultats.getListeMetrics());
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

    public static void affichage(List<Process> processusInitiaux) {

        for (Process p : processusInitiaux) {

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

            for (String nom : algos) { //Parcours des différents algos qui ont executé le processus
                System.out.println("------------------------------------------------------------");
                System.out.println("Algorithme : " + nom);
                System.out.println("------------------------------------------------------------");

                ExecutionInfo info = ProcessUtils.getExecution(p, nom); //Récupération de l'execution de ce processus pour cet algo la

                // Affichage des infos globales
                System.out.printf("  Date début exécution : %d%n", info.getDateDebut());
                System.out.printf("  Date fin exécution   : %d%n", info.getDateFin());
                System.out.printf("  Ram utilisée   : %d%n", info.getUsedRam());

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
