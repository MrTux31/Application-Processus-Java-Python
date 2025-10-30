package com.ordonnancement.service.validation;

import java.util.List;

import com.ordonnancement.model.Process;
import com.ordonnancement.model.Schedule;
import com.ordonnancement.service.parser.FileParsingException;

/**
 * Classe permetant une vérification minimale des assignations processus a un
 * processeur
 */
public class ScheduleValidator {

    /**
     * Permet de vérifier si les Schedules d'un Process sont cohérents
     *
     * @param p : Le Process
     * @param listeSchedules : Sa liste de Schedules (ses assignations
     * processeur)
     */
    public static void valider(Process p, List<Schedule> listeSchedules) {

        int tempsTotalExecution = 0;
        if (!listeSchedules.isEmpty()) { //Si les assignations sont présentes dans la liste
        //Vérification de la cohérence de chaque Schedule du processus
            for (Schedule s : listeSchedules) {

                tempsTotalExecution += s.getDateFinExecution() - s.getDateDebutExecution(); //On fait la somme des différents temps d'allocation du processus
                if (s.getDateDebutExecution() < p.getDateDebut()) {
                    throw new FileParsingException("Le processus " + p.getId()
                            + " a une date de début d'exécution d'une assignation(" + s.getDateDebutExecution()
                            + ") antérieure à sa date de début globale (" + p.getDateDebut() + ")");
                }

                if (s.getDateFinExecution() > p.getDateFin()) {
                    throw new FileParsingException("Le processus " + p.getId()
                            + " a une date de fin d'exécution d'une assignation (" + s.getDateFinExecution()
                            + ") postérieure à sa date de fin globale (" + p.getDateFin() + ")");
                }

                if (s.getDateDebutExecution() > p.getDateFin()) {
                    throw new FileParsingException("Le processus " + p.getId()
                            + " a une date de début d'exécution d'une assignation (" + s.getDateDebutExecution()
                            + ") postérieure à sa date de fin globale (" + p.getDateFin() + ")");
                }
                if (s.getDateFinExecution() < p.getDateDebut()) {
                    throw new FileParsingException("Le processus " + p.getId()
                            + " a une date de fin d'exécution d'une assignation (" + s.getDateFinExecution()
                            + ") antérieure à sa date de début globale (" + p.getDateDebut() + ")");
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
