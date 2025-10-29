package com.ordonnancement.service.parser;

import java.util.List;

/**
 * Interface générique représentant un parseur de fichiers.
 * 
 * Cette interface définit la méthode commune permettant de lire un fichier 
 * et d'en extraire une liste d'objets de type T 
 * 
 *
*/
public interface FileParser<T> {

    /**
     * Permet de parser un fichier pour en extraire des objets de type T
     * @param cheminFichier : Le chemin du fichier à parser
     * @throws Exception : en cas d'erreur de lecture du fichier
     * @return une liste de T
     */
    public List<T> parse(String cheminFichier);

}
