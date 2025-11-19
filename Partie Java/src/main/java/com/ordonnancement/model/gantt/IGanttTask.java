package com.ordonnancement.model.gantt;

/**
 * Interface représentant une tâche à afficher dans un Gantt.
 */
public interface IGanttTask {

    /**
     * Retourne l'identifiant unique de la tâche.
     * 
     * @return identifiant de la tâche
     */
    public String getId();

    /**
     * Retourne la catégorie de la tâche (ex: CPU ou Processus).
     * 
     * @return nom de la catégorie
     */
    public String getCategorie();

    /**
     * Retourne la date de début de la tâche.
     * 
     * @return date de début en unité de temps
     */
    public int getDateDebut();

    /**
     * Retourne la date de fin de la tâche.
     * 
     * @return date de fin en unité de temps
     */
    public int getDateFin();

    /**
     * Retourne un identifiant pour générer la couleur du rectangle.
     * 
     * @return entier utilisé pour la couleur
     */
    public int getColorId();
}
