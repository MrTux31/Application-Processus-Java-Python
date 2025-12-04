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

    private final HashMap<String,List<Allocation>> allocationsParAlgo; //La liste des allocations processeur de ce processus par différents algo d'ordonnancement (résultats détaillés)
    private final HashMap<String,ExecutionInfo> infoExecutionsParAlgo; //La liste des informations des executions sur les différents algos (résultats globaux)
   

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

    /**
     * Renvoie l'identifiant du processus.
     *
     * @return l'identifiant du processus
     */
    public String getId() {
        return id;
    }
    /**
     * Renvoie la date de soumission du processus à l'ordonnanceur.
     *
     * @return la date de soumission
     */
    public int getDateSoumission() {
        return dateSoumission;
    }
    /**
     * Renvoie le temps total d'exécution demandé par le processus.
     *
     * @return le temps d'exécution
     */
    public int getTempsExecution() {
        return tempsExecution;
    }

    /**
     * Renvoie la quantité de RAM nécessaire à l'exécution du processus.
     *
     * @return la RAM requise
     */
    public int getRequiredRam() {
        return requiredRam;
    }
    /**
     * Renvoie la date butoir à laquelle le processus doit être terminé.
     *
     * @return la deadline
     */
    public int getDeadline() {
        return deadline;
    }
    /**
     * Renvoie la priorité du processus.
     *
     * @return la priorité
     */
    public int getPriority() {
        return priority;
    }

    

    /**
     * Renvoie la liste des allocations processeur associées à l'algorithme spécifié.
     *
     * @param nomAlgo le nom de l'algorithme
     * @return la liste des allocations pour cet algorithme, ou null si aucune n'existe
     */
    public List<Allocation> getAllocations(String nomAlgo){
        return this.allocationsParAlgo.get(nomAlgo);
    }

    /**
     * Définit la liste complète des allocations processeur pour un algorithme donné.
     *
     * @param algo le nom de l'algorithme d'ordonnancement
     * @param allocations la liste des allocations associées
     */
    // Ajouter toutes les allocations pour un algo
    public void setAllocations(String algo, List<Allocation> allocations) {
        allocationsParAlgo.put(algo, allocations);
    }
    
    /**
     * Renvoie l'ensemble des allocations processeur du processus,
     * classées par algorithme d'ordonnancement.
     *
     * @return une map { nomAlgo → liste d'allocations }
     */
    public HashMap<String,List<Allocation>> getAllAllocations(){
        return this.allocationsParAlgo;
    }
    

     /**
     * Renvoie les informations d'exécution associées à l'algorithme spécifié.
     *
     * @param nomAlgo le nom de l'algorithme
     * @return l'information d'exécution, ou null si absente
     */
    public ExecutionInfo getExecutionInfo(String nomAlgo){
        return this.infoExecutionsParAlgo.get(nomAlgo);
    }

     /**
     * Ajoute une information d'exécution pour un algorithme d'ordonnancement donné.
     *
     * @param nomAlgo le nom de l'algorithme
     * @param e  l'information d'exécution associée
     */
    public void addExecution(String nomAlgo,ExecutionInfo e){
        this.infoExecutionsParAlgo.put(nomAlgo,e);
    }
    
    /**
     * Renvoie l'ensemble des informations d'exécution du processus
     * pour tous les algorithmes d'ordonnancement.
     *
     * @return une map { nomAlgo → ExecutionInfo }
     */
    public HashMap<String,ExecutionInfo> getAllExecutions(){
        return this.infoExecutionsParAlgo;
    }


    





}
