package com.ordonnancement.service.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;

/**
 * Classe qui s'occupe d'écrire le fichier de
 * configuration de l'ordonnanceur pour python en JSON
 */

public class ConfigurationWriter {


    /**
     * Permet de lancer l'écriture du fichier de configuration
     * Génère un fichier JSON contenant toutes les infos du fichier
     * de configuration dans la destination spécifiée.
     * @param destination : La destination du fichier de configuration
     */

    public void writeConfiguration(FileConfiguration configuration,String destination){
        //Vérifications concernant la validité de la destination
        if(destination == null || destination.isBlank()){ //si on ne fournit aucune destination
            throw new ConfigurationWriterException("Le chemin du fichier de destination doit être spécifié");
        }
        File file = new File(destination.trim()); //Si la destination est un dossier
        if (file.isDirectory()) {
            throw new ConfigurationWriterException("Le chemin fourni est un dossier, pas un fichier : " + destination);
        }
        if(!destination.contains(".json") && !destination.contains(".JSON")){ //Vérification du format du fichier
            throw new ConfigurationWriterException("Le fichier de destination ne contient pas l'extension JSON.");
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls() // Pour que Python ait toujours les mêmes clés (ajoute les éléments nulls au json)
        .create(); //Creation du JSON builder


        creerDossiersParentsFichier(destination); // Création des dossiers parents nécessaires

        //Création de l'arborescence des dossiers avec les dossiers et les sous dossiers correspondants pour les résultats de chaque algos
        for(AlgoConfiguration a : configuration.getListeAlgorithmes()){
            String cheminFichierResultatsDetailles = a.getFichierResultatsDetailles();
            String cheminFichierResultatsGlobaux = a.getFichierResultatsGlobaux();
            creerDossiersParentsFichier(cheminFichierResultatsDetailles);
            creerDossiersParentsFichier(cheminFichierResultatsGlobaux);
        }
        String fichierConfig = configuration.getFichierMetriquesGlobales(); //Création des dossiers parent pour le ficher des metriques globales
        creerDossiersParentsFichier(fichierConfig);

        //Try with ressources pour fermer automatiquement le writer en cas d'erreur
        try (FileWriter writer = new FileWriter(destination.trim())) { //L'objet FileWriter permet d'écrire dans le fichier de destination et le créer si il n'existe pas encore
            gson.toJson(configuration, writer); //Conversion en JSON de l'objet FileConfiguration, écrit dans la destination
        
        
        
        
        } catch (IOException e) {
            throw new ConfigurationWriterException("Erreur d'écriture du fichier JSON : " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ConfigurationWriterException("Aucune configuration n’a été initialisée dans le ConfigurationManager.", e);
        }

    }
    /**
     * Permet de créer les dossiers parents du fichier dont la destination est spécifiée
     * Si les dossiers existent déjà, rien n'est fait
     * @param destination le chemin complet du fichier JSON
     * @throws ConfigurationWriterException si la création des dossiers échoue
     */
    private void creerDossiersParentsFichier(String destination){
        File file = new File(destination);  // Représente le fichier final
        File parentDir = file.getParentFile(); // Récupère le dossier parent du fichier
        if(parentDir != null && !parentDir.exists()){  // Si le dossier parent n'existe pas
            if (!parentDir.mkdirs()) { // Tente de créer tous les dossiers manquants
                throw new ConfigurationWriterException(
                    "Impossible de créer le dossier : " + parentDir.getAbsolutePath());
            }
            System.out.println("Creation du dossier parent : " + parentDir.getAbsolutePath());
        }

    }


}
