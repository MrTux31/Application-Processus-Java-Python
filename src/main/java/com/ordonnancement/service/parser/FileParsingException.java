package com.ordonnancement.service.parser;

/**
 * Exception générique pour les erreurs liées au parsing de fichiers.
 * 
 * Utilisée par toutes les stratégies de parsing (FileParserStrategy)
 * pour signaler un problème de lecture, de format ou d'accès.
 */
public class FileParsingException extends RuntimeException {

    public FileParsingException(String message) {
        super(message);
    }

    public FileParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}