package com.ordonnancement.service.validation;

import java.util.List;
import java.util.Map;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.util.ProcessUtils;

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

        for (Map.Entry<String, ExecutionInfo> entry : p.getAllExecutions().entrySet()) { //pour chaque execution sur un algo
            
            String algo = entry.getKey();
            ExecutionInfo execution = entry.getValue();


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


            List<Allocation> listeAllocations = ProcessUtils.getAllocations(p,algo); //Récupérer la liste des assignation processeur pour le processus
            if (!listeAllocations.isEmpty()) { //Si les assignations sont présentes dans la liste
                int tempsTotalExecution = 0;
                //Vérification de la cohérence de chaque Schedule du processus
                for (Allocation a : listeAllocations) {

                    tempsTotalExecution += a.getDateFinExecution() - a.getDateDebutExecution(); //On fait la somme des différents temps d'allocation du processus
                    if (a.getDateDebutExecution() < execution.getDateDebut()) {
                        throw new FileParsingException("Le processus " + p.getId()
                                + " a une date de début d'exécution d'une assignation(" + a.getDateDebutExecution()
                                + ") antérieure à sa date de début globale (" + execution.getDateDebut() + ")");
                    }

                    if (a.getDateFinExecution() > execution.getDateFin()) {
                        throw new FileParsingException("Le processus " + p.getId()
                                + " a une date de fin d'exécution d'une assignation (" + a.getDateFinExecution()
                                + ") postérieure à sa date de fin globale (" + execution.getDateFin() + ")");
                    }

                    if (a.getDateDebutExecution() > execution.getDateFin()) {
                        throw new FileParsingException("Le processus " + p.getId()
                                + " a une date de début d'exécution d'une assignation (" + a.getDateDebutExecution()
                                + ") postérieure à sa date de fin globale (" + execution.getDateFin() + ")");
                    }
                    if (a.getDateFinExecution() < execution.getDateDebut()) {
                        throw new FileParsingException("Le processus " + p.getId()
                                + " a une date de fin d'exécution d'une assignation (" + a.getDateFinExecution()
                                + ") antérieure à sa date de début globale (" + execution.getDateDebut() + ")");
                    }

                    if (a.getDateDebutExecution() < 0) {
                        throw new FileParsingException("Le processus " + p.getId() + " a une date de début d'assignation du processus à un processeur inférieure à 0");

                    }

                    if (a.getDateFinExecution() < 0) {
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
