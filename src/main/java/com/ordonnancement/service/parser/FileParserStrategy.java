package com.ordonnancement.service.parser;

import java.util.List;
/**
 * Interface définissant une stratégie de parsing pour un type de fichier donné.
 * 
 * Implémente le pattern Strategy : chaque implémentation fournit une 
 * méthode de parsing spécifique à un format de fichier (CSV, JSON, clé:valeur, etc.).

 */
public interface FileParserStrategy<T> {

    /**
     * Parse un fichier selon le format géré par la stratégie.
     *
     * @param cheminFichier le chemin du fichier à parser
     * @return une liste d'objets de type T
     * @throws Exception si une erreur de lecture ou de parsing survient
     */
    public List<T> parse(String cheminFichier);


}
