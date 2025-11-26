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

//     public static void lancerExecution() {

//         //Démo création du fichier de configuration + récupération des résultats python////////////////////////////////////////////
//         //Ne pas se fier a cette récupértion de chemins c'est teporaire pour les tests
//         Path resultsDir = Paths.get("python/Resultats");

//         AlgoConfiguration algo1 = new AlgoConfiguration(
//                 "Round Robin",
//                 resultsDir.resolve("RoundRobin/rDetailedROUNDROBIN.csv").toString(),
//                 resultsDir.resolve("RoundRobin/rGlobauxROUNDROBIN.csv").toString(),
//                 2
//         );

//         AlgoConfiguration algo2 = new AlgoConfiguration(
//                 "FIFO",
//                 resultsDir.resolve("Fifo/rDetailedFIFO.csv").toString(),
//                 resultsDir.resolve("Fifo/rGlobauxFIFO.csv").toString(),
//                 null
//         );

//         AlgoConfiguration algo3 = new AlgoConfiguration(
//                 "Priorite",
//                 resultsDir.resolve("Priorite/rDetailedPriorite.csv").toString(),
//                 resultsDir.resolve("Priorite/rGlobauxPriorite.csv").toString(),
//                 null
//         );

//         Path settingsDir = Paths.get("python/Settings");

//         ////////////

       
//         // //Création des algos qui vont être utilisés par l'ordonnanceur
//         // AlgoConfiguration algo1 = new AlgoConfiguration("ROUND ROBIN",  "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rDetailedROUNDROBIN.csv", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rGlobauxROUNDROBIN.csv", 2);
//         // AlgoConfiguration algo2 = new AlgoConfiguration("FIFO", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rDetailedFIFO.csv", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\rGlobauxFIFO.csv", null);
        
        
//         //Ajouts de ces algos dans une liste
//         List<AlgoConfiguration> liste = new ArrayList<>();
//         liste.add(algo1);
//         liste.add(algo2);
//         liste.add(algo3);
//         //Création de l'objet FileConfig représentant le fichier de configuration
//         //FileConfiguration fileConfig = new FileConfiguration("C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\processusInitiaux.csv", "C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\fichierMetriquesGlobales.csv","C:\\Users\\Quentin\\Documents\\SAE\\Tests fichiers JSON\\Ressources.json", liste);

//         FileConfiguration fileConfig = new FileConfiguration(
//                 settingsDir.resolve("processusInitiaux.csv").toString(),
//                 settingsDir.resolve("fichierMetriquesGlobales.csv").toString(),
//                 settingsDir.resolve("ressources.json").toString(),
//                 liste
//         );

//         ConfigurationManager.getInstance().setFileConfiguration(fileConfig);
//         ConfigurationManager.getInstance().setCheminFichierConfig(settingsDir.resolve("config.json"));
// // //Lancer l'execution / écriture fichier config + récup des résultats de python
// // Runner.runAsync(fileConfig,
// //         settingsDir.resolve("config.json").toString(),
// //         () -> {
// //             AncienMain.AfficherResultats();
// //         });
// }

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
