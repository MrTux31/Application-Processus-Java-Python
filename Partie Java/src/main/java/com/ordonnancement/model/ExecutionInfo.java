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


    public ExecutionInfo() {
       
        this.dateDebut = -1; //Valeur par défaut (pas encore calculée)
        this.dateFin = -1; //Valeur par défaut (pas encore calculée)
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

    public int getDateDebut() {
        return dateDebut;
    }

    /**
     * Renvoie le makeSpan
     */
    public int getDateFin() {
        return dateFin;
    }





    
}
