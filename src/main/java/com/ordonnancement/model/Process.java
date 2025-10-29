package com.ordonnancement.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un processus exécuté
 * Les données de celui-ci sont fournies dans la list
 * des processus à ordonnancer.
 * 
 * @author ROMA Quentin
 */

public class Process {

    private final int id;
    private final int dateSoumission; //Date à partir de laquelle on veut que le processus soit considéré par l'ordonnanceur
    private final int tempsExecution;
    private final int requiredRam;
    private final int deadline; //Date butoire (la date à laquelle le processus doit être fini)
    private final int priority;

    private List<Schedule> listeSchedules; //La liste des différentes assignation du processus à un processeur réalisées


    //Calculé par python
    private int dateDebut; //Date de début d'exécution du processus;
    private int dateFin; //Date de fin d'exécution du processus

    /**
     * Constructeur, permet d'initialiser le processus
     * @param id : L'identifiant du processus
     * @param dateSoumission : La date de soumission du processus
     * @param deadline : La date butoire du processus 
     * @param priority : La priorité du processus
     * @param requiredRam : La quantité de ram nécessaire à l'exécution du processus
     * @param tempsExecution : Le temps d'execution total du processus
     */
    public Process( int id, int dateSoumission, int tempsExecution, int requiredRam, int deadline, int priority ) {
        this.dateDebut = -1; //Valeur par défaut (pas encore calculée)
        this.dateFin = -1; //Valeur par défaut (pas encore calculée)
        this.dateSoumission = dateSoumission;
        this.deadline = deadline;
        this.id = id;
        this.priority = priority;
        this.requiredRam = requiredRam;
        this.tempsExecution = tempsExecution;
        this.listeSchedules = new ArrayList<>();
    }

    public void setDateDebut(int dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(int dateFin) {
        this.dateFin = dateFin;
    }

    public int getId() {
        return id;
    }

    public int getDateSoumission() {
        return dateSoumission;
    }

    public int getTempsExecution() {
        return tempsExecution;
    }

    public int getRequiredRam() {
        return requiredRam;
    }

    public int getDeadline() {
        return deadline;
    }

    public int getPriority() {
        return priority;
    }

    public int getDateDebut() {
        return dateDebut;
    }

    public int getDateFin() {
        return dateFin;
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

    


    





}
