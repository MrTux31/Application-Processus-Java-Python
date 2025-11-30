package com.ordonnancement.service.parser.metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ordonnancement.model.Metrics;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.validation.FileValidator;

/**
 * Classe permettant de parser le fichier CSV des métriques globales.
 *
 * Cette classe lit un fichier CSV contenant les métriques globales calculées
 * pour chaque algorithme d'ordonnancement. Chaque ligne correspond aux valeurs
 * moyennes calculées pour un algorithme donné.
 *
 * Format attendu du CSV :
 *
 * algo,tempsAttenteMoyen,tempsReponseMoyen,makespan
 * ROUND ROBIN,0.0,5.0,9
 * FIFO,0.0,5.0,9
 * PRIORITE,0.0,5.0,9
 *
 */
public class MetricsParser {

    /**
     * Permet de parser le fichier CSV listant les métriques globales par algorithme
     * d'ordonnancement.
     *
     * @param cheminFichier chemin vers le fichier CSV à parser
     * @return une liste d'objets Metrics correspondant aux algorithmes analysés
     * @throws FileParsingException si le fichier est vide, mal formé ou si les
     *                              valeurs numériques sont invalides
     */
    public List<Metrics> parse(String cheminFichier) {

        FileValidator.verifierCheminFichier(cheminFichier); // Vérification de l'existence du fichier

        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {

            List<Metrics> liste = new ArrayList<>();

            // Lecture de la première ligne (en-tête)
            String ligne = reader.readLine();
            if (ligne == null) {
                throw new FileParsingException("Fichier CSV vide : " + cheminFichier);
            }

            // Vérification de la validité de l'en-tête
            checkHeader(ligne);

            // Lecture des lignes de données
            while ((ligne = reader.readLine()) != null) {
                if (ligne.trim().isEmpty()) continue; // Ignorer les lignes vides
                parseLine(ligne, liste); // Conversion et ajout à la liste
            }

            return liste;

        } catch (IOException e) {
            throw new FileParsingException("Impossible de lire le fichier : " + cheminFichier, e);
        } catch (NumberFormatException e) {
            throw new FileParsingException("Format numérique invalide dans le fichier CSV : " + cheminFichier, e);
        }
    }

    /**
     * Vérifie que la ligne d'en-tête correspond exactement aux colonnes attendues.
     *
     * @param header ligne du CSV contenant les noms des colonnes
     * @throws FileParsingException si les colonnes ne correspondent pas
     */
    private void checkHeader(String header) {
        String[] colonnesAttendues = {"algo", "tempsAttenteMoyen", "tempsReponseMoyen", "makespan"};
        String[] colonnes = header.split(",");

        if (colonnes.length != colonnesAttendues.length) {
            throw new FileParsingException("Nombre de colonnes incorrect dans le fichier des métriques globales");
        }

        for (int i = 0; i < colonnesAttendues.length; i++) {
            if (!colonnes[i].trim().equals(colonnesAttendues[i])) {
                throw new FileParsingException(
                        "Colonne " + colonnesAttendues[i] + " introuvable ou mal placée dans le fichier des métriques globales");
            }
        }
    }

    /**
     * Convertit une ligne du CSV en objet Metrics et l’ajoute à la liste des métriques.
     *
     * @param ligne ligne du CSV contenant les valeurs
     * @param liste liste dans laquelle ajouter le nouvel objet Metrics
     * @throws FileParsingException si la ligne contient moins de 4 colonnes ou des données invalides
     */
    private void parseLine(String ligne, List<Metrics> liste) {
        String[] valeurs = ligne.split(",");
        if (valeurs.length < 4) {
            throw new FileParsingException("Ligne CSV incomplète dans le fichier des métriques globales : " + ligne);
        }

        String nomAlgorithme = valeurs[0].trim();
        double tempsAttenteMoyen = Double.parseDouble(valeurs[1].trim());
        double tempsReponseMoyen = Double.parseDouble(valeurs[2].trim());
        int makespan = Integer.parseInt(valeurs[3].trim());

        Metrics m = new Metrics(nomAlgorithme, tempsReponseMoyen, tempsAttenteMoyen, makespan);
        liste.add(m);
    }
}
