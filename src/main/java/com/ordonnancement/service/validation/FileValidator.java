package com.ordonnancement.service.validation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ordonnancement.service.parser.FileParsingException;

/**
 * Classe qui permet de vérifier que le chemin spécifié d'un fichier à lire est 
 * correct
 */
public class FileValidator {

    /**
     * Permet de vérifier que le chemin d'un fichier qu'on veut lire est correct
     * @param cheminFichier : le chemin du fichier à vérifier
     * @throws FileParsingException : si le chemin du fichier est incorrect
     */
    public static void verifierCheminFichier(String cheminFichier) {
        if (cheminFichier == null || cheminFichier.isBlank()) {
            throw new FileParsingException("Le chemin du fichier à parser doit être spécifié.");
        }

        Path path = Paths.get(cheminFichier);

        if (!Files.exists(path)) {
            throw new FileParsingException("Le fichier spécifié n'existe pas : " + cheminFichier);
        }

        if (Files.isDirectory(path)) {
            throw new FileParsingException("Le chemin spécifié pointe vers un dossier, pas un fichier : " + cheminFichier);
        }
    }
}
