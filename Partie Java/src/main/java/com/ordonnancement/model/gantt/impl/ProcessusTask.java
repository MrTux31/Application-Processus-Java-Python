package com.ordonnancement.model.gantt.impl;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.gantt.IGanttTask;

/**
 * Représente une tâche affichée dans un Gantt par processus.
 * Chaque tâche correspond à une allocation d'un CPU par processus.
 * 
 * @author Olivencia Eliot
 */
public class ProcessusTask implements IGanttTask {

    private final Allocation allocation;
    private final int colorId;
    private final String category;

    /**
     * Constructeur par défaut. La catégorie sera l'ID du processus.
     * 
     * @param allocation : l'allocation réalisée sur un CPU
     */
    public ProcessusTask(Allocation allocation) {
        this(allocation, allocation.getIdProcessus());
    }

    /**
     * Constructeur avec catégorie personnalisée.
     * 
     * @param allocation : l'allocation réalisée sur un CPU
     * @param category   : le nom de la catégorie à afficher (ex: "P1")
     */
    public ProcessusTask(Allocation allocation, String category) {
        this.allocation = allocation;
        this.category = category;
        this.colorId = allocation.getProcessus().hashCode();
    }

    /**
     * Retourne le CPU sur lequel la tâche est exécutée.
     * 
     * @return identifiant du CPU
     */
    @Override
    public String getId() {
        return allocation.getProcessor();
    }

    /**
     * Retourne l'identifiant du processus.
     * 
     * @return identifiant du processus
     */
    @Override
    public String getCategorie() {
        return category;
    }

    /**
     * Retourne la date de début d'exécution de la tâche.
     * 
     * @return date de début en unité de temps
     */
    @Override
    public int getDateDebut() {
        return allocation.getDateDebutExecution();
    }

    /**
     * Retourne la date de fin d'exécution de la tâche.
     * 
     * @return date de fin en unité de temps
     */
    @Override
    public int getDateFin() {
        return allocation.getDateFinExecution();
    }

}
