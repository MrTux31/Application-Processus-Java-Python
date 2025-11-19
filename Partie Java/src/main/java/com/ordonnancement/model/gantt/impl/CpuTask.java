package com.ordonnancement.model.gantt.impl;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.gantt.IGanttTask;

/**
 * Représente une tâche affichée dans un Gantt par CPU.
 * Chaque tâche correspond à une allocation d'un processus sur un CPU.
 * @author ROMA Quentin
 */
public class CpuTask implements IGanttTask {

    private final Allocation allocation;

    /**
     * Constructeur.
     * 
     * @param allocation : l'allocation réalisée sur un CPU
     */
    public CpuTask(Allocation allocation){
        this.allocation = allocation;
    }

    /**
     * Retourne l'identifiant du processus.
     * 
     * @return identifiant du processus
     */
    @Override
    public String getId() {
        return allocation.getIdProcessus();
    }

   /**
     * Retourne le CPU sur lequel la tâche est exécutée.
     * 
     * @return nom du CPU
     */
    @Override
    public String getCategorie() {
       return allocation.getProcessor();
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

    /**
     * Retourne un identifiant utilisé pour générer la couleur du rectangle.
     * 
     * @return entier représentant la couleur
     */
    @Override
    public int getColorId() {
        return allocation.hashCode();
    }
}
