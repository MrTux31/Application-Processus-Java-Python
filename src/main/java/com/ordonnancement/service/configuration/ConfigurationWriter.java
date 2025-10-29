package com.ordonnancement.service.configuration;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ordonnancement.config.ConfigurationManager;

/**
 * Classe qui s'occupe d'écrire le fichier de
 * configuration de l'ordonnanceur pour python en JSON
 */

public class ConfigurationWriter {

    public void writeConfiguration(String destination) throws Exception{

        ConfigurationManager manager = ConfigurationManager.getInstance(); //Récupération de l'instance du manager
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); //Creation du JSON builder

        //Try with ressources pour fermer automatiquement le writer en cas d'erreur
        try (FileWriter writer = new FileWriter(destination)) {
            gson.toJson(manager.getFileConfiguration(), writer); //Conversion en JSON de l'objet FileConfiguration, écrit dans la destination
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'écrire le fichier JSON de configuration.", e);
        }

    }


}
