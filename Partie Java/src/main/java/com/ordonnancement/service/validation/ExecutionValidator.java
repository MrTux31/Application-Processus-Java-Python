package com.ordonnancement.service.validation;

import java.util.HashMap;
import java.util.List;

import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Schedule;
import com.ordonnancement.service.parser.FileParsingException;

/**
 * Classe permetant une vérification minimale des executions réalisées par un processus
 * 
 */
public class ExecutionValidator {

    /**
     * Permet de vérifier si les ExecutionsInfo d'un Process sont cohérentes
     *
     * @param p : Le Process
     * 
     * processeur)
     */
    public static void valider(Process p) {
        HashMap<String, ExecutionInfo> executionsParAlgo = p.getAllExecutions();

        for (ExecutionInfo execution : executionsParAlgo.values()) { //pour chaque execution sur un algo
            
            //Vérification des dates de début et de fin de l'execution
            if (execution.getDateDebut() != -1 && execution.getDateFin() != -1) {
                if (execution.getDateDebut() > execution.getDateFin()) {
                    throw new FileParsingException("Le processus " + p.getId() + " a une date de début supérieure à la date de fin");
                }
                if((execution.getDateFin() - execution.getDateDebut()) < p.getTempsExecution()){
                    throw new FileParsingException("Le processus " + p.getId() + " a une date de début  : "+execution.getDateDebut() +" et de fin "+execution.getDateFin()+ " inchohérente. "+
                    "Ce n'est pas assez pour un temps d'execution de total "+p.getTempsExecution());

                }

            }


            List<Schedule> listeSchedules = execution.getListSchedules(); //Récupérer la liste des assignation du processus à un processeu
            if (!listeSchedules.isEmpty()) { //Si les assignations sont présentes dans la liste
                int tempsTotalExecution = 0;
                //Vérification de la cohérence de chaque Schedule du processus
                for (Schedule s : listeSchedules) {

                    tempsTotalExecution += s.getDateFinExecution() - s.getDateDebutExecution(); //On fait la somme des différents temps d'allocation du processus
                    if (s.getDateDebutExecution() < execution.getDateDebut()) {
                        throw new FileParsingException("Le processus " + p.getId()
                                + " a une date de début d'exécution d'une assignation(" + s.getDateDebutExecution()
                                + ") antérieure à sa date de début globale (" + execution.getDateDebut() + ")");
                    }

                    if (s.getDateFinExecution() > execution.getDateFin()) {
                        throw new FileParsingException("Le processus " + p.getId()
                                + " a une date de fin d'exécution d'une assignation (" + s.getDateFinExecution()
                                + ") postérieure à sa date de fin globale (" + execution.getDateFin() + ")");
                    }

                    if (s.getDateDebutExecution() > execution.getDateFin()) {
                        throw new FileParsingException("Le processus " + p.getId()
                                + " a une date de début d'exécution d'une assignation (" + s.getDateDebutExecution()
                                + ") postérieure à sa date de fin globale (" + execution.getDateFin() + ")");
                    }
                    if (s.getDateFinExecution() < execution.getDateDebut()) {
                        throw new FileParsingException("Le processus " + p.getId()
                                + " a une date de fin d'exécution d'une assignation (" + s.getDateFinExecution()
                                + ") antérieure à sa date de début globale (" + execution.getDateDebut() + ")");
                    }

                    if (s.getDateDebutExecution() < 0) {
                        throw new FileParsingException("Le processus " + p.getId() + " a une date de début d'assignation du processus à un processeur inférieure à 0");

                    }

                    if (s.getDateFinExecution() < 0) {
                        throw new FileParsingException("Le processus " + p.getId() + " a une date de fin d'assignation du processus à un processeur inférieure à 0");

                    }

                }

                // Vérification du temps total d'exécution
                if (tempsTotalExecution != p.getTempsExecution()) {
                    throw new FileParsingException("Le processus " + p.getId()
                            + " a un temps total d'exécution (" + tempsTotalExecution
                            + ") différent du temps prévu (" + p.getTempsExecution() + ")");
                }

            }

        }

    }

}
