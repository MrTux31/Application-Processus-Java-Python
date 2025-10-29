package com.ordonnancement.model;
/**
 * Représente un processeur pouvant être utilisé par un processus
 * @author ROMA Quentin
 */
public class Processor {

    private final String id;

    /**
     * Constructeur du processeur
     * @param idProcessor : l'id du processeur
     */
    public Processor(String idProcessor){
        this.id = idProcessor;
    }

    public String getId() {
        return id;
    }

}
