
package com.ordonnancement.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
/**
 * Classe utilitaire permettant de récupérer plus facilement des éléments d'un processus
 */
public class ProcessUtils {

    
    /**
     * Récupère le Map des executions d'un processus réalisées par les différents algorithmes
     * @param p : Le processus 
     * @return le map des executions du processus par nom d'algorithme
     */
    public static Map<String, ExecutionInfo> getExecutions(Process p) {
        Map<String, ExecutionInfo> executions = p.getAllExecutions();
        if (executions == null) {
            executions = new HashMap<>();
        }
        return executions;
    }


    /**
     * Permet de récupérer l'execution d'un processus pour un algorithme
     * @param p : Le processus
     * @param algo : Le nom de l'algo d'ordonancement
     * @return l'execution du processus pour le nom de l'algorithme spécifié
     */
    public static ExecutionInfo getExecution(Process p,String algo){
        ExecutionInfo execution = getExecutions(p).get(algo);
        return execution;
    }

    
    /**
     * Récupère toutes les allocations processeur d'un processus pour un algo précis.
     * @param p : Le processus
     * @param algo : Le nom de l'algo d'ordonancement
     * @return la liste des allocations
     */
    public static List<Allocation> getAllocations(Process p, String algo) {
        List<Allocation> allocations = new ArrayList<>();
        List<Allocation> allocationsProcessus = p.getAllocations(algo);
        if(allocationsProcessus != null){ //Evite null pointer
            allocations = allocationsProcessus;
        }
        return allocations; //Retourne toujours une liste
        
    }

    /**
     * Récupère les noms de tous les algorithmes d'ordonnancement
     * qui ont exécuté ce processus.
     * @param p : Le processus 
     * @return la liste des noms d'algorithmes qui ont executé le processus
     */
    public static List<String> getNomAlgos(Process p){
        List<String> nomAlgos = new ArrayList<>();
        Map<String, ExecutionInfo> executions =  getExecutions(p);
        for(String algo : executions.keySet()){
            nomAlgos.add(algo);
        }

        return nomAlgos;
    }

    /**
     * Permet de récupérer les allocations de tous les processus
     * sur un algorithme d'ordonnancement précis
     * @param listeProcessus : tous les processus
     * @param nomAlgo
     * @return la liste de toutes les allocations de tous les processus
     */
    public static List<Allocation> getAllocations(List<Process> listeProcessus,String nomAlgo){
        List<Allocation> listeComplete = new ArrayList<>();
        for(Process p : listeProcessus){ //On parcours tous les processus de la liste
            List<Allocation> allocs = ProcessUtils.getAllocations(p, nomAlgo); //Récupération de toutes les allocations processeur de cet processus
            listeComplete.addAll(allocs); //Ajout à la liste globale des allocations
        }
        return listeComplete;
    }


    public static List<String> getAllCpus(List<Process> listeProcessus, String nomAlgo){

        Set<String> ensembleCpu = new HashSet<>();
        for(Process p : listeProcessus){ //Pour chaque processus
            for(Allocation al : ProcessUtils.getAllocations(p, nomAlgo)){ //Récupération de l'alloc réalisée sur l'algo
                        
            ensembleCpu.add(al.getProcessor()); //Ajout de l'id cpu à l'ensemble
            }

        }
        List<String> cpus = new ArrayList<>(ensembleCpu);
        Collections.sort(cpus); //Trie croissant
        return cpus;

    }
}
