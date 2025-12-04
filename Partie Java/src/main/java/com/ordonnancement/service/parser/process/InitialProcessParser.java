package com.ordonnancement.service.parser.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.validation.FileValidator;
import com.ordonnancement.service.validation.ProcessValidator;

/**
 * Cette classe lit un fichier CSV contenant une liste des processus initiale et
 * crée pour chacun un objet à partir des champs : idProcessus, dateSoumission,
 * tempsExecution, requiredRam, deadline, priority.
 *
 * Elle vérifie que le CSV possède bien les colonnes attendues dans le bon
 * ordre. Les lignes vides sont ignorées. Si une ligne a moins de 6 colonnes,
 * une exception est levée.
 * 
 * @author ROMA Quentin
 *
 */
public class InitialProcessParser {

    /**
     * Parse le fichier CSV décrivant les processus à exécuter et retourne une
     * liste d'objets Process.
     *
     * Format attendu :
     * idProcessus,dateSoumission,tempsExecution,requiredRam,deadline,priority
     * 1,0,5,1024,20,3 2,3,4,512,15,1
     *
     * @param cheminFichier chemin vers le fichier CSV
     * @return liste des Process
     * @throws FileParsingException si le fichier est vide, si les colonnes ne
     * correspondent pas ou si le format numérique est incorrect
     */
    

    public List<Process> parse(String cheminFichier) {
        FileValidator.verifierCheminFichier(cheminFichier); //Vérification de l'existance du fichier
       
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {

            List<Process> liste = new ArrayList<>();

            // Lecture de la première ligne (les noms des colonnes)
            String ligne = reader.readLine();
            if (ligne == null) {
                throw new FileParsingException("Première ligne du fichier des processus manquante !");
            }

            // Vérification de l'en-tête
            checkHeader(ligne);

            // Lecture des lignes de données
            while ((ligne = reader.readLine()) != null) {
                if (ligne.trim().isEmpty()) { // Ignorer les lignes vides
                    continue;
                }

                // Conversion de la ligne en Process
                Process p = parseLine(ligne);
                liste.add(p); // Ajout du processus créé
            }

            ProcessValidator.valider(liste); //Validation de la cohérence des processus parsés
            return liste;

        } catch (IOException e) {
            throw new FileParsingException("Impossible de lire le fichier : " + cheminFichier, e);
        } catch (NumberFormatException e) {
            throw new FileParsingException("Format numérique invalide dans le fichier des processus initiaux: " + cheminFichier, e);
        }
    }

    /**
     * Vérifie que la ligne d'en-tête correspond exactement aux colonnes attendues.
     *
     * @param header ligne du CSV contenant les noms des colonnes
     * @throws FileParsingException si les colonnes ne correspondent pas
     */
    private void checkHeader(String header) {
        String nomColonnes = "idProcessus,dateSoumission,tempsExecution,requiredRam,deadline,priority";
        String[] colonnesAttendues = nomColonnes.split(",");
        String[] colonnes = header.split(",");

        if (colonnesAttendues.length != colonnes.length) {
            throw new FileParsingException("Nombre de colonnes incorrect dans le fichier des processus initiaux");
        }

        for (int i = 0; i < colonnesAttendues.length; i++) {
            if (!colonnesAttendues[i].equals(colonnes[i].trim())) {
                throw new FileParsingException(
                        "Colonne " + colonnesAttendues[i] + " introuvable ou mal placée dans le fichier des processus initiaux");
            }
        }
    }

    /**
     * Convertit une ligne du CSV en un objet Process.
     *
     * @param ligne ligne du CSV contenant les valeurs des colonnes
     * @return un objet Process correspondant à la ligne
     * @throws FileParsingException si la ligne contient moins de 6 colonnes
     */
    private Process parseLine(String ligne) {
        String[] valeurs = ligne.split(",");
        if (valeurs.length < 6) {
            throw new FileParsingException("Ligne CSV incomplète dans le fichier des processus initiaux : " + ligne);
        }

        return new Process(
                valeurs[0].trim(),                     // idProcessus
                Integer.parseInt(valeurs[1].trim()),   // dateSoumission
                Integer.parseInt(valeurs[2].trim()),   // tempsExecution
                Integer.parseInt(valeurs[3].trim()),   // requiredRam
                Integer.parseInt(valeurs[4].trim()),   // deadline
                Integer.parseInt(valeurs[5].trim())    // priority
        );
    }
}


