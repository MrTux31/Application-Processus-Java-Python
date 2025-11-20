package com.ordonnancement.service.gantt;

import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.config.ConfigurationManager;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.gantt.IGanttTask;
import com.ordonnancement.model.gantt.impl.CpuTask;
import com.ordonnancement.util.ProcessUtils;

import javafx.collections.FXCollections;

/**
 * Classe service pour le controller GanttProcessorController
 * Service permettant de gérer les algorithmes d'ordonnancement et de générer les tâches Gantt correspondantes.
 * Fournit les données nécessaires au contrôleur pour afficher le diagramme de Gantt.
 */


public class GanttProcessorService {
    private String nomAlgo;
    private final List<Process> processus;
    private List<String> listeCpusDisponibles; //Liste de tous les cpus utilisés dans l'algo d'ordonnancement courant
    private int dateFinMax;
    private List<IGanttTask> listeTachesGantt;

    /**
     * Construteur
     * @param processus La liste des processus
     */
    public GanttProcessorService(List<Process> processus) {
        this.processus = processus;
        this.listeCpusDisponibles = new ArrayList<>();
        
    }
    /**
     * Change l'algorithme courant,
     * recharge les CPUs disponibles et les tâches Gantt associées.
     *
     * @param nomAlgo Nom de l'algorithme à sélectionner.
     */
    public void changerAlgo(String nomAlgo){
        this.nomAlgo = nomAlgo;
        rechargerListeCpu();        
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
     * Retourne la liste des de tous les noms d'algorithmes
     *
     * @return Liste des noms d'algorithmes.
     */
    public List<String> getNomAlgosDisponibles(){
        List<AlgoConfiguration> algos = ConfigurationManager.getInstance().getFileConfiguration().getListeAlgorithmes(); //Récupération de la liste des algorithmes exécutés dans le singleton
        List<String> nomAlgos = FXCollections.observableArrayList();
        for (AlgoConfiguration algo : algos) { //On récup le nom de chaque algo exécuté
            nomAlgos.add(algo.getNomAlgorithme()); 

        }
        return nomAlgos;
    };

    /**
     * Retourne la liste des CPUs disponibles pour l'algorithme courant.
     *
     * @return Liste des CPUs disponibles.
     */
    public List<String> getCpusDisponibles(){
        return this.listeCpusDisponibles;
    };

    /**
     * Recharge la liste des CPUs utilisés pour l'algorithme courant.
     * Met à jour l'attribut interne listeCpusDisponibles.
     */
    private void rechargerListeCpu() {
        //On charge la liste des cpus qui ont été utilisé dans l'algo d'ordonnacement
        listeCpusDisponibles = ProcessUtils.getAllCpus(processus, nomAlgo);
               

    }

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
            if (listeCpusDisponibles.contains(a.getProcessor())) {
                IGanttTask tache = new CpuTask(a); //(CPU task car gantt par CPU en Y)
                tachesGantt.add(tache);
                
            }
            if (a.getDateFinExecution() > max) {
                    max = a.getDateFinExecution();
            }
        }
        //Rechargement avec les nouvelles valeurs
        this.dateFinMax = max;
        this.listeTachesGantt = tachesGantt;
       
    }


}
