package com.ordonnancement.service.gantt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.gantt.IGanttTask;
import com.ordonnancement.model.gantt.impl.CpuTask;
import com.ordonnancement.util.ProcessUtils;

/**
 * Classe service pour le controller GanttProcessorController
 * Service permettant de gérer les algorithmes d'ordonnancement et de générer les tâches Gantt correspondantes.
 * Fournit les données nécessaires au contrôleur pour afficher le diagramme de Gantt.
 * @author ROMA Quentin
 */


public class GanttProcessorService {
    private String nomAlgo;
    private List<String> algosDispos;
    private final List<Process> processus;
    private int dateFinMax;
    private List<IGanttTask> listeTachesGantt;
    private List<String> allCpus; //Tous les cpus dispos

    /**
     * Construteur
     * @param processus La liste des processus
     */
    public GanttProcessorService(List<Process> processus) {
        this.processus = processus;
        trouverAlgosDisponibles();
        trouverTousLesCpus();
        
    }
    /**
     * Change l'algorithme courant,
     * recharge les CPUs disponibles et les tâches Gantt associées.
     *
     * @param nomAlgo Nom de l'algorithme à sélectionner.
     */
    public void changerAlgo(String nomAlgo){
        this.nomAlgo = nomAlgo;
        rechargerTachesGantt();
    };

    /**
     * Retourne la liste des tâches Gantt générées pour l'algorithme courant.
     *
     * @return Liste des IGanttTask pour l'algorithme courant.
     */
    public List<IGanttTask> getTachesGantt(){
        return this.listeTachesGantt;
    };
    
    /**
     * Retourne la date de fin maximale parmi toutes les tâches Gantt.
     *
     * @return Date de fin maximale des allocations.
     */
    public int getDateFinMax(){
        return this.dateFinMax;
    };
    
    


    /**
     * Retourne renvoie la liste des tous les cpus pour tous les algos
     *
     * @return Listede tous les cpus
     */
    public List<String> getAllCpus(){
        return allCpus;
    };



    /**
     * Recharge la liste des tâches Gantt pour l'algorithme courant et calcule la date de fin maximale.
     * Met à jour les attributs internes listeTachesGantt et dateFinMax.
     */
    private void rechargerTachesGantt(){
        //Récupérer liste des allocations pour l'algo
        List<Allocation> allocations = ProcessUtils.getAllocations(processus, nomAlgo); //Liste qui va garder les allocations de TOUS les processus
        //Liste qui va stocker les taches du gantt
        List<IGanttTask>tachesGantt = new ArrayList<>();
        int max = 0;
        //Pour chaque allocation, conversion en IGanttTask
        for (Allocation a : allocations) {
            IGanttTask tache = new CpuTask(a); //(CPU task car gantt par CPU en Y)
            tachesGantt.add(tache);
            
            if (a.getDateFinExecution() > max) {
                    max = a.getDateFinExecution();
            }
        }
        //Rechargement avec les nouvelles valeurs
        this.dateFinMax = max;
        this.listeTachesGantt = tachesGantt;
       
    }


    /**
     * Retourne la liste des de tous les noms d'algorithmes ayant été exécuté par python
     * @return Liste des noms d'algorithmes.
     */
    public List<String> getNomAlgosDisponibles(){
        
        return this.algosDispos;
    };

    private void trouverAlgosDisponibles(){
        List<AlgoConfiguration> algos = ConfigurationManager.getInstance().getFileConfiguration().getListeAlgorithmes(); //Récupération de la liste des algorithmes exécutés dans le singleton
        algosDispos = new ArrayList<>();
        for (AlgoConfiguration algo : algos) { //On récup le nom de chaque algo exécuté
            algosDispos.add(algo.getNomAlgorithme()); 

        }
    }

    /**
     * Permet de trouver tous les cpus pour tous les algos
     * @return Liste de tous les cpus
     */
    private void trouverTousLesCpus(){
        Set<String> cpus = new HashSet<>();
        for(String algo : algosDispos){
            cpus.addAll(ProcessUtils.getAllCpus(processus, algo));
        }
        this.allCpus =  new ArrayList<>(cpus);
        Collections.sort(allCpus);
        
    }


}
