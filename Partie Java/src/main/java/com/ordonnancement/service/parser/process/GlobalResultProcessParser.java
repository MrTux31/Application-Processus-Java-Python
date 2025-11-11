package com.ordonnancement.service.parser.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.validation.FileValidator;
import com.ordonnancement.service.validation.ProcessValidator;

/**
 * Cette classe lit un fichier CSV contenant les résultats globaux de l'ordonnancement.
 * Permet de compléter les objets Process : en mettant à jour leur date de début et leur date de fin,
 * ainsi que la RAM utilisée.
 *
 * Format attendu du CSV :
 *
 * idProcessus,dateSoumission,dateDebut,dateFin,requiredRam,usedRam
 * 2,0,0,3,1024,1024
 * 1,0,0,6,1024,1024
 * 3,3,3,9,1024,1024
 *
 */
public class GlobalResultProcessParser {

    private List<Process> listeProcessus;

    /**
     * Constructeur
     *
     * @param listeProcessus liste initiale des processus
     */
    public GlobalResultProcessParser(List<Process> listeProcessus) {
        this.listeProcessus = listeProcessus;
    }

    /**
     * Permet de parser le fichier CSV décrivant les résultats globaux de l'ordonnancement
     * et de mettre à jour chaque Process avec les informations globales : dates et RAM utilisée.
     *
     * @param cheminFichier chemin vers le fichier CSV
     * @param nomAlgorithme le nom de l'algorithme ayant réalisé l'ordonnancement
     * @return la liste des Process mise à jour
     * @throws FileParsingException si le fichier est vide, mal formé ou si un Process du CSV
     *                              n'existe pas dans la liste initiale
     */
    public List<Process> parse(String cheminFichier, String nomAlgorithme) {
        FileValidator.verifierCheminFichier(cheminFichier); //Vérification de l'existance du fichier
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {

            // Création d'une map pour retrouver rapidement les processus par leur ID
            Map<String, Process> mapProcessus = new HashMap<>();
            for (Process p : listeProcessus) {
                mapProcessus.put(p.getId(), p);
            }

            // Lecture de la première ligne (en-tête)
            String ligne = reader.readLine();
            if (ligne == null) {
                throw new FileParsingException("Fichier CSV vide : " + cheminFichier);
            }

            // Vérification des colonnes attendues
            checkHeader(ligne);

            // Lecture des lignes de données
            while ((ligne = reader.readLine()) != null) {
                if (ligne.trim().isEmpty()) continue; // Ignorer les lignes vides
                parseLine(ligne, mapProcessus, nomAlgorithme); // Conversion et mise à jour du Process
            }
            
            ProcessValidator.valider(listeProcessus); //Validation de la cohérence des processus parsés
            return listeProcessus;

        } catch (IOException e) {
            throw new FileParsingException("Impossible de lire le fichier : " + cheminFichier, e);
        } catch (NumberFormatException e) {
            throw new FileParsingException(
                    "Format numérique invalide dans le fichier CSV : " + cheminFichier, e);
        }
    }

    /**
     * Vérifie que la ligne d'en-tête correspond exactement aux colonnes attendues.
     *
     * @param header ligne du CSV contenant les noms des colonnes
     * @throws FileParsingException si les colonnes ne correspondent pas
     */
    private void checkHeader(String header) {
        String[] colonnesAttendues = {"idProcessus", "dateSoumission", "dateDebut", "dateFin", "requiredRam", "usedRam"};
        String[] colonnes = header.split(",");

        if (colonnes.length != colonnesAttendues.length) {
            throw new FileParsingException("Nombre de colonnes incorrect dans le CSV");
        }

        for (int i = 0; i < colonnesAttendues.length; i++) {
            if (!colonnes[i].trim().equals(colonnesAttendues[i])) {
                throw new FileParsingException(
                        "Colonne " + colonnesAttendues[i] + " introuvable dans le CSV");
            }
        }
    }

    /**
     * Convertit une ligne du CSV en mise à jour globale du Process.
     *
     * @param ligne ligne du CSV contenant les valeurs des colonnes
     * @param mapProcessus map des Process pour retrouver le Process par son ID
     * @param nomAlgorithme nom de l'algorithme ayant réalisé l'ordonnancement
     * @throws FileParsingException si la ligne contient moins de 6 colonnes ou si le Process est introuvable
     */
    private void parseLine(String ligne, Map<String, Process> mapProcessus, String nomAlgorithme) {
        String[] valeurs = ligne.split(",");
        if (valeurs.length < 6) {
            throw new FileParsingException("Ligne CSV incomplète : " + ligne);
        }

        String idProcessus = valeurs[0].trim();
        int dateDebut = Integer.parseInt(valeurs[2].trim());
        int dateFin = Integer.parseInt(valeurs[3].trim());

        // Récupération du Process correspondant
        Process p = mapProcessus.get(idProcessus);
        if (p == null) {
            throw new FileParsingException(
                    "Incohérence détectée : le processus " + idProcessus
                            + " dans le CSV n'existe pas dans la liste initiale.");
        }

        // Récupération ou création de l'ExecutionInfo pour cet algorithme
        ExecutionInfo execInfo = p.getExecutionInfo(nomAlgorithme);
        if (execInfo == null) {
            execInfo = new ExecutionInfo();
            p.addExecution(nomAlgorithme, execInfo);
        }

        // Mise à jour des informations globales
        execInfo.setDateDebut(dateDebut);
        execInfo.setDateFin(dateFin);
    }
}
