package com.ordonnancement.model;

import java.util.HashMap;

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

    private HashMap<String,ExecutionInfo> executionsParAlgo; //La liste des executions de ce processus par différents algo d'ordonnancement

   

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
        
        this.executionsParAlgo = new HashMap<>();
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
     * Permet d'obtenir la liste des différentes executions réalisées sur différents algos d'ordonnancement
     */
    public ExecutionInfo getExecutionInfo(String nomAlgo){
        return this.executionsParAlgo.get(nomAlgo);
    }

    /**
     * Permet d'ajouter une execution du processus sur un algo d'ordonnancement différent
     * @param e : L'execution du processus sur un algo d'ordonnancement
     */
    public void addExecution(String nomAlgo,ExecutionInfo e){
        this.executionsParAlgo.put(nomAlgo,e);
    }
    
    /**
     * Permet de récupérer toutes les executions du processus par algos d'ordonnancement
     * @param e : L'execution du processus sur un algo d'ordonnancement
     */
    public HashMap<String,ExecutionInfo> getAllExecutions(){
        return this.executionsParAlgo;
    }
    


    





}
