package com.ordonnancement.service.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParsingException;

/**
 * Classe permettant de vérifier si un objet Process construit est cohérent
 * @author ROMA Quentin
 */
public class ProcessValidator {

    /**
     * Permet de vérifier si une liste de Process est cohérente
     *
     * @param processus : la liste de processus à vérifier
     * @throws FileParsingException : Si le processus est incohérent
     */
    public static void valider(List<Process> processus) {
        Set<String> ids = new HashSet<>(); //Un ensemble pour stocker les ID des processus

        for (Process p : processus) {
            if (!ids.add(p.getId())) { //Si le processus est déjà présent
                throw new FileParsingException("Doublon détecté : le processus " + p.getId() + " est déclaré plusieurs fois.");
            }
            if (p.getTempsExecution() <= 0) {
                throw new FileParsingException("Le temps d'exécution du processus " + p.getId() + " doit être > 0");
            }
            if (p.getRequiredRam() <= 0) {
                throw new FileParsingException("La RAM requise du processus " + p.getId() + " doit être > 0");
            }
            if (p.getDeadline() < p.getDateSoumission()) {
                throw new FileParsingException("Le processus " + p.getId() + " a une deadline avant sa soumission.");
            }
            if (p.getPriority() < 0) {
                throw new FileParsingException("Le processus " + p.getId() + " a une priorité <= 0");
            }
           
            ExecutionValidator.valider(p); //Vérifier les assignations du processus
            
        }
    }
}
