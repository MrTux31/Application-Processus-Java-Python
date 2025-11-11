
package com.ordonnancement.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Schedule;
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
     * Récupère toutes les assignation du processus à un processeur pour un algo précis.
     * @param p : Le processus
     * @param algo : Le nom de l'algo d'ordonancement
     * @return la liste des assignations
     */
    public static List<Schedule> getSchedules(Process p, String algo) {
        ExecutionInfo info = getExecutions(p).get(algo);
        List<Schedule> schedules = new ArrayList<>();
        if(info != null){ //Evite null pointer
            schedules = info.getListSchedules();
        }
        return schedules; //Retourne toujours une liste
        
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
}
