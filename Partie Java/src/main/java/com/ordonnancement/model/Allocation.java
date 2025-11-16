package com.ordonnancement.model;

/**
 * Représente l'allocation d'un processeur à un processus,
 * sur une prériode donnée
 * @author ROMA Quentin
 */
public class Allocation {
    private final Process processus; //Processus exécuté
    private final String idProcessor; //Le processus utilisée durant l'allocation
    private final int dateDebutExecution; //Date de début d'éxecution sur le processeur.
    private final int dateFinExecution; //Date de fin d'execution sur le processeur.

    /**
     * Constructeur de l'allocation
     * @param processus : Le processus executé lors de l'allocation
     * @param processor : Le processeur alloué
     * @param dateDebutExecution : la date de début de l'allocation
     * @param dateFinExecution : la date de fin de l'allocation
     */
    public Allocation(Process processus, String idProcessor,int dateDebutExecution,int dateFinExecution){
        this.idProcessor = idProcessor;
        this.processus = processus;
        this.dateDebutExecution = dateDebutExecution;
        this.dateFinExecution = dateFinExecution;
    }

    /**
     * Permet d'obtenir le processeur qui à traité le processus
     */
    public String getProcessor(){
        return this.idProcessor;
    }

    /**
     * Permet d'obtenir la date de début d'éxecution sur le processeur.
     */
    public int getDateDebutExecution() {
        return dateDebutExecution;
    }
    /**
     * Permet d'obtenir la date de fin d'exécution sur le processeur
     */
    public int getDateFinExecution() {
        return dateFinExecution;
    }

    public String getIdProcessus(){
        return this.processus.getId();
    }

    public Process getProcessus(){
        return this.processus;
    }



}
