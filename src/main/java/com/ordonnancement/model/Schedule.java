package com.ordonnancement.model;

/**
 * Représente l'assignation d'un processus à un processeur donnée,
 * sur une prériode donnée
 * @author ROMA Quentin
 */
public class Schedule {
   
    private final Processor processor; //Le processus utilisée durant l'assignation du processus
    private final int dateDebutExecution; //Date de début d'éxecution sur le processeur.
    private final int dateFinExecution; //Date de fin d'execution sur le processeur.

    /**
     * Constructeur du Schedule
     * @param processor : Le processeur utilisé durant l'assignation du processus
     * @param dateDebutExecution : la date de début de l'execution de l'assignation
     * @param dateFinExecution : la date de fin de l'execution de l'assignation
     */
    public Schedule(Processor processor,int dateDebutExecution,int dateFinExecution){
        this.processor = processor;
        this.dateDebutExecution = dateDebutExecution;
        this.dateFinExecution = dateFinExecution;
    }

    /**
     * Permet d'obtenir le processeur qui à traité le processus
     */
    public Processor getProcessor(){
        return this.processor;
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



}
