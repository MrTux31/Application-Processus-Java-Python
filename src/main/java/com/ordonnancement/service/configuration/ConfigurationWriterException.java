package com.ordonnancement.service.configuration;

/**
 * Exception spécifique pour la classe ConfigurationWriter
 */
public class ConfigurationWriterException extends RuntimeException{

    public ConfigurationWriterException(String message, Throwable cause) {
            super(message, cause);
        }

    public ConfigurationWriterException(String message) {
            super(message);
        }

}
