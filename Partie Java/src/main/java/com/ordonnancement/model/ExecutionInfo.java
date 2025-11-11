package com.ordonnancement.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe permettant de regrouper les informations
 * d'une exécution d'un processus par un algorithme d'ordonnancement
 */
public class ExecutionInfo {
   
    //Calculé par python
    private int dateDebut; //Date de début d'exécution du processus;
    private int dateFin; //Date de fin d'exécution du processus

     private List<Schedule> listeSchedules; //La liste des différentes assignation du processus à un processeur réalisées

    public ExecutionInfo() {
       
        this.dateDebut = -1; //Valeur par défaut (pas encore calculée)
        this.dateFin = -1; //Valeur par défaut (pas encore calculée)
        this.listeSchedules = new ArrayList<>();
    }

    public void setDateDebut(int dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(int dateFin) {
        this.dateFin = dateFin;
    }

    
    


     /**
     * Permet de savoir si le processus est terminé
     * @return true si fini, false sinon
     */
    public boolean isFinished(){
        return dateFin != -1;

    }

    /**
     * Permet d'obtenir la liste des assignation du processus aux différents processeurs
     */
    public List<Schedule> getListSchedules(){
        return this.listeSchedules;
    }

    /**
     * Permet d'ajouter une assignation du processus à un processeur
     * @param s : L'assignation du processus à un processeur sur une période donnée
     */
    public void addSchedule(Schedule s){
        this.listeSchedules.add(s);
    }
    /**
     * Permet de définir la liste des assignations du processus à des processeurs
     * @param listeSchedules : la liste des assignations du processus aux divers processeurs
     */
    public void setSchedules(List<Schedule> listeSchedules){
        this.listeSchedules = listeSchedules;
    }

    public int getDateDebut() {
        return dateDebut;
    }

    public int getDateFin() {
        return dateFin;
    }





    
}
