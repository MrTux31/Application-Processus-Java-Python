package com.ordonnancement.model;

import java.util.HashMap;
import java.util.List;

/**
 * Représente un processus exécuté
 * Les données de celui-ci sont fournies dans la list
 * des processus à ordonnancer.
 * 
 * @author ROMA Quentin
 */

public class Process {

    private final String id;
    private final int dateSoumission; //Date à partir de laquelle on veut que le processus soit considéré par l'ordonnanceur
    private final int tempsExecution;
    private final int requiredRam;
    private final int deadline; //Date butoire (la date à laquelle le processus doit être fini)
    private final int priority;

    private HashMap<String,List<Allocation>> allocationsParAlgo; //La liste des allocations processeur de ce processus par différents algo d'ordonnancement (résultats détaillés)
    private HashMap<String,ExecutionInfo> infoExecutionsParAlgo; //La liste des informations des executions sur les différents algos (résultats globaux)
   

    /**
     * Constructeur, permet d'initialiser le processus
     * @param id : L'identifiant du processus
     * @param dateSoumission : La date de soumission du processus
     * @param deadline : La date butoire du processus 
     * @param priority : La priorité du processus
     * @param requiredRam : La quantité de ram nécessaire à l'exécution du processus
     * @param tempsExecution : Le temps d'execution total du processus
     */
    public Process( String id, int dateSoumission, int tempsExecution, int requiredRam, int deadline, int priority ) {
       
        this.dateSoumission = dateSoumission;
        this.deadline = deadline;
        this.id = id;
        this.priority = priority;
        this.requiredRam = requiredRam;
        this.tempsExecution = tempsExecution;
        this.infoExecutionsParAlgo = new HashMap<>();
        this.allocationsParAlgo = new HashMap<>();
    }

    
    public String getId() {
        return id;
    }

    public int getDateSoumission() {
        return dateSoumission;
    }

    public int getTempsExecution() {
        return tempsExecution;
    }

    public int getRequiredRam() {
        return requiredRam;
    }

    public int getDeadline() {
        return deadline;
    }

    public int getPriority() {
        return priority;
    }

    

    /**
     * Permet d'obtenir la liste des différentes allocations réalisées sur l'algo d'ordonnancement spécifié
     */
    public List<Allocation> getAllocations(String nomAlgo){
        return this.allocationsParAlgo.get(nomAlgo);
    }

    /**
     * Permet d'ajouter toutes les allocation processeur au processus sur l'algo d'ordonnancement spécifié
     * @param a : L'allocation processeur
     */
    // Ajouter toutes les allocations pour un algo
    public void setAllocations(String algo, List<Allocation> allocations) {
        allocationsParAlgo.put(algo, allocations);
    }
    
    /**
     * Permet de récupérer toutes les allocations processeur réalisées du processus pour touts les algos
     */
    public HashMap<String,List<Allocation>> getAllAllocations(){
        return this.allocationsParAlgo;
    }
    

    /**
     * Permet d'obtenir la liste des différentes info executions réalisées sur différents algos d'ordonnancement
     */
    public ExecutionInfo getExecutionInfo(String nomAlgo){
        return this.infoExecutionsParAlgo.get(nomAlgo);
    }

    /**
     * Permet d'ajouter une info execution du processus sur un algo d'ordonnancement différent
     * @param e : L'info d'execution du processus sur un algo d'ordonnancement
     */
    public void addExecution(String nomAlgo,ExecutionInfo e){
        this.infoExecutionsParAlgo.put(nomAlgo,e);
    }
    
    /**
     * Permet de récupérer toutes les info executions du processus par algos d'ordonnancement
     * @param e : L'info d'execution du processus sur un algo d'ordonnancement
     */
    public HashMap<String,ExecutionInfo> getAllExecutions(){
        return this.infoExecutionsParAlgo;
    }


    





}
