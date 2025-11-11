package com.ordonnancement.service.python;

/**
 * Exception générique pour les erreurs liées a l'exécution du script python.
 * 
 * Utilisée par python launcher pour signaler un problème quelconque de l'exécution de l'application python.
 */
public class PythonException extends RuntimeException {

    public PythonException(String message) {
        super(message);
    }

    public PythonException(String message, Throwable cause) {
        super(message, cause);
    }
}