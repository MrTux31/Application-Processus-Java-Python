package com.ordonnancement.service.parser.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.validation.FileValidator;
import com.ordonnancement.service.validation.ProcessValidator;

/**
 * Cette classe lit un fichier CSV contenant les exécutions de chaque processus
 * sur un processeur donné et met à jour les Process existants avec les objets
 * Schedule correspondants.
 *
 * Le CSV doit avoir le format suivant :
 *
 * idProcessus,dateDebut,dateFin,idProcesseur
 * 2,0,2,CPU1
 * 1,0,2,CPU2
 * 2,2,3,CPU1
 * 1,2,4,CPU2
 * 3,3,5,CPU1
 * ...
 *
 * Chaque ligne correspond à une exécution partielle d'un processus sur un CPU.
 */
public class DetailedResultProcessParser {

    private List<Process> listeProcessus;

    /**
     *
     * @param listeProcessus
     */
    public DetailedResultProcessParser(List<Process> listeProcessus) {
        this.listeProcessus = listeProcessus;
    }

   
    /**
     * Parse le fichier CSV décrivant les résultats détaillés d'un algorithme et
     * met à jour chaque Process avec ses schedules correspondants.
     *
     * @param cheminFichier chemin vers le fichier CSV
     * @param nomAlgorithme : le nom de l'algorithme ayant réalisé l'ordonnancement
     * @return la liste des Process mise à jour avec les schedules
     * @throws FileParsingException si le fichier est vide, mal formé, ou si un
     *                              processus du CSV n'existe pas dans la liste initiale
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
                parseLine(ligne, mapProcessus, nomAlgorithme);      // Conversion et ajout au Process correspondant
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
        String[] colonnesAttendues = {"idProcessus", "dateDebut", "dateFin", "idProcesseur"};
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
     * Convertit une ligne du CSV en Schedule et met à jour le Process correspondant.
     *
     * @param ligne du CSV contenant les valeurs des colonnes
     * @param mapProcessus map des Process pour retrouver le Process par son ID
     * @param nomAlgorithme : le nom de l'algorithme ayant réalisé l'ordonnancement
     * @throws FileParsingException si la ligne contient moins de 4 colonnes ou si le Process est introuvable
     */
    private void parseLine(String ligne, Map<String, Process> mapProcessus, String nomAlgorithme) {
        String[] valeurs = ligne.split(",");
        if (valeurs.length < 4) {
            throw new FileParsingException("Ligne CSV incomplète : " + ligne);
        }

        String idProcessus = valeurs[0].trim();
        int dateDebut = Integer.parseInt(valeurs[1].trim());
        int dateFin = Integer.parseInt(valeurs[2].trim());
        String idProcesseur = valeurs[3].trim();

        // Récupération du Process correspondant
        Process p = mapProcessus.get(idProcessus);
        if (p == null) {
            throw new FileParsingException(
                    "Incohérence détectée : le processus " + idProcessus
                            + " dans les résultats CSV n'existe pas dans la liste initiale.");
        }
        
    
        // Création de l'allocation pour cette ligne et ajout à l'ExecutionInfo
        Allocation a = new Allocation(p,idProcesseur, dateDebut, dateFin);


        // Récupérer la liste actuelle pour l'algorithme
        List<Allocation> listeAlloc = p.getAllocations(nomAlgorithme);
        if (listeAlloc == null) { //Si la liste est nulle, alors on la crée
            listeAlloc = new ArrayList<>();
            p.setAllocations(nomAlgorithme, listeAlloc); //On défini cette liste comme la liste des allocations de ce processus pour cet algo
        }

        listeAlloc.add(a); //Ajout de l'allocation à la liste
    }
}
