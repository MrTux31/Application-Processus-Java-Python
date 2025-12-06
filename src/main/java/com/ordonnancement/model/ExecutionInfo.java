package com.ordonnancement.model;


/**
 * Classe permettant de regrouper les informations
 * d'une exécution d'un processus par un algorithme d'ordonnancement
 * @author Ribeiro--Vaur Nino 
 */
public class ExecutionInfo {
   
    //Calculé par python
    private final int dateDebut; //Date de début d'exécution du processus;
    private final int dateFin; //Date de fin d'exécution du processus
    private final int usedRam; //Qte de ram utilisée durant l'execution du processus
    /**
     * Constructeur
     * @param dateDebut : la date de début de l'execution
     * @param dateFin : la date de fin de l'execution
     * @param usedRam : la quantité de ram utilisée
     */
    public ExecutionInfo(int dateDebut, int dateFin, int usedRam) {
       
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.usedRam = usedRam;
    }

    /**
     * Renvoie la date de début d'exécution du processus.
     *
     * @return la date de début d'exécution
     */
    public int getDateDebut() {
        return dateDebut;
    }
    /**
     * Renvoie la quantité de RAM utilisée par le processus durant son exécution.
     *
     * @return la RAM utilisée
     */
    public int getUsedRam(){
        return this.usedRam;
    }

    /**
     * Renvoie le makeSpan
     * @return le makespan
     */
    public int getDateFin() {
        return dateFin;
    }





    
}
