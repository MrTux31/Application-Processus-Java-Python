package com.ordonnancement.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe permettant de regrouper les informations
 * d'une exécution d'un processus par un algorithme d'ordonnancement
 */
public class ExecutionInfo {
   
    //Calculé par python
    private final int dateDebut; //Date de début d'exécution du processus;
    private final int dateFin; //Date de fin d'exécution du processus
    private final int usedRam; //Qte de ram utilisée durant l'execution du processus

    public ExecutionInfo(int dateDebut, int dateFin, int usedRam) {
       
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.usedRam = usedRam;
    }


    public int getDateDebut() {
        return dateDebut;
    }

    public int getUsedRam(){
        return this.usedRam;
    }

    /**
     * Renvoie le makeSpan
     */
    public int getDateFin() {
        return dateFin;
    }





    
}
