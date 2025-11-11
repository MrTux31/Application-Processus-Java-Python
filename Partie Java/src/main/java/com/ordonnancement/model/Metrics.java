package com.ordonnancement.model;

/**
 * Représente les métriques globales obtenues par algorithme d'ordonnacement
 * @author ROMA Quentin
 */

public class Metrics {
    private final String nomAlgo;
    private final double tempsReponseMoyen; //Temps réponse = délai entre la soumission du processus et le moment où il commence à s'executer pour la première fois
    private final double tempsAttenteMoyen; //Attente = le processus est pret mais il attends de disposer d'un processeur pour s'executer
    private final int makespan; //Le makespan : la date de fin du dernier processus

    /**
     * Constructeur des métriques
     * @param nomAlgorithme : Le nom de l'algorithme utilisé pour l'ordonnancement
     * @param tempsReponseMoyen : Temps réponse = délai entre la soummussion du processus et le moment où il commence à s'executer pour la première fois
     * @param tempsAttenteMoyen : Attente = le processus est pret mais il attends de disposer d'un processeur pour s'executer
     * @param makespan : La date de fin du dernier processus
     */
    public Metrics(String nomAlgorithme, double tempsReponseMoyen, double tempsAttenteMoyen, int makespan){
        this.nomAlgo = nomAlgorithme;
        this.tempsAttenteMoyen = tempsAttenteMoyen;
        this.tempsReponseMoyen = tempsReponseMoyen;
        this.makespan = makespan;

    }



    // /** 
    //  * Méthode permettant de calculer toutes les moyennes et la date de fin (makespan)
    //  */
    // private void calculerMetrics(List<Process> listeProcessus){
    //     double tempsAttenteMoyenTotal = 0;
    //     int tempsReponseTotal = 0;
    //     int dateFinTemporaire = 0;

    //     //Pour chacun des processus de la liste
    //     for(Process p : listeProcessus){
    //         int tempsAttenteProcessus = 0;
    //         tempsReponseTotal += p.getDateDebut() - p.getDateSoumission(); //On calcule le délais entre le moment où le processus est soumis et le moment où il démarre réllement.

    //         if(p.getDateFin() > dateFinTemporaire){ //On cherche la date de fin du dernier processus
    //             dateFinTemporaire = p.getDateFin();
    //         }

    //         List<Schedule> listeSchedules = p.getListSchedules(); //Récupération de la liste des assignation du processus aux processeurs
    //         //Pour chacune des assignation des processus de la liste
    //         for(int i = 0; i< listeSchedules.size(); i++){
    //             if(i == 0){ //Cas spécial si c'est la première assignation
    //                 // Temps entre la soumission du processus et le début de sa première exécution
    //                 tempsAttenteProcessus += listeSchedules.get(i).getDateDebutExecution() - p.getDateSoumission(); //(date de début - date de soumission)
    //             }
    //             else{
    //                 //Le temps d'attente correspond au délai entre deux assignations du processus. On prends la date de début de l'assignation i 
    //                 //et on soustrait à la date de fin de l'assignation i-1
    //                 tempsAttenteProcessus += listeSchedules.get(i).getDateDebutExecution() - listeSchedules.get(i-1).getDateFinExecution(); 
    //             }   
    //         }
    //         if(!listeSchedules.isEmpty()){ //Sécurité
    //             //On fait la moyenne des temps d'attente entre chaque Schedule du Processus actuel
    //             tempsAttenteMoyenTotal += (double) tempsAttenteProcessus / listeSchedules.size(); 
    //         }
      
    //     }

    //     //Calcul des moyennes
    //     this.tempsReponseMoyen = (double) tempsReponseTotal/listeProcessus.size(); 
    //     this.tempsAttenteMoyen = (double) tempsAttenteMoyenTotal/listeProcessus.size(); //On prends la moyenne des temps d'attente et on divise par le nombre de processus
        
    //     //On enregistre la bonne date de fin
    //     this.makespan = dateFinTemporaire;
    // }

    /**
     * Permet de récupérer le temps de réponse moyen
     * Temps réponse = délai entre la soummussion du processus et le moment où il commence à s'executer pour la première fois
     */
    public double getTempsReponseMoyen() {
        return tempsReponseMoyen;
    }

    /**
    * Permet de récupérer le temps d'attente moyen
    * Attente = le processus est pret mais il attends de disposer d'un processeur pour s'executer 
    */
    public double getTempsAttenteMoyen() {
        return tempsAttenteMoyen;
    }

    /**
     * Permet de récupérer le makespan
     * Le makespan : la date de fin du dernier processus executé
     */
    public int getMakespan() {
        return makespan;
    }

    /**
     * Permet de récupérer le nom de l'algorithme d'ordonnacement utilisé
     */
    public String getNomAlgorithme(){
        return this.nomAlgo;
    }


    


}
