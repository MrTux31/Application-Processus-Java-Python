package com.ordonnancement.model.gantt.impl;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.gantt.IGanttTask;

/**
 * Représente une tâche affichée dans un Gantt par processus.
 * Chaque tâche correspond à une allocation d'un CPU par processus, ou une
 * attente.
 * 
 * @author Olivencia Eliot
 */
public class ProcessusTask implements IGanttTask {

    private final String id;
    private final String category;
    private final int dateDebut;
    private final int dateFin;

    /**
     * Constructeur par défaut à partir d'une allocation.
     * La catégorie sera l'ID du processus.
     * 
     * @param allocation : l'allocation réalisée sur un CPU
     */
    public ProcessusTask(Allocation allocation) {
        this(allocation, allocation.getIdProcessus());
    }

    /**
     * Constructeur avec catégorie personnalisée à partir d'une allocation.
     * 
     * @param allocation : l'allocation réalisée sur un CPU
     * @param category   : le nom de la catégorie à afficher (ex: "P1")
     */
    public ProcessusTask(Allocation allocation, String category) {
        this.id = allocation.getProcessor();
        this.category = category;
        this.dateDebut = allocation.getDateDebutExecution();
        this.dateFin = allocation.getDateFinExecution();
    }

    /**
     * Constructeur manuel pour créer une tâche sans allocation (ex: Attente).
     * 
     * @param id        : Identifiant de la tâche (ex: "EN ATTENTE")
     * @param category  : Catégorie (ex: "P01")
     * @param dateDebut : Date de début
     * @param dateFin   : Date de fin
     */
    public ProcessusTask(String id, String category, int dateDebut, int dateFin) {
        this.id = id;
        this.category = category;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCategorie() {
        return category;
    }

    @Override
    public int getDateDebut() {
        return dateDebut;
    }

    @Override
    public int getDateFin() {
        return dateFin;
    }

}
