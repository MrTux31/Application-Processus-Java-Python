package com.ordonnancement.service.parser.config;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ordonnancement.model.AlgoConfiguration;
import com.ordonnancement.model.FileConfiguration;

public class ConfigParser {

      /**
     * Parse un fichier JSON en un objet FileConfiguration à l'aide de Gson.
     *
     * @param cheminFichierJson chemin du fichier JSON
     * @return un FileConfiguration entièrement construit
     * @throws IOException si le fichier est introuvable ou illisible
     */
    public static FileConfiguration parse(String cheminFichierJson) {

        Gson gson = new Gson();
        JsonObject root;

        // Lecture du fichier JSON en tant qu'objet
        try (FileReader reader = new FileReader(cheminFichierJson)) {
            root = gson.fromJson(reader, JsonObject.class);
        

        if (root == null) {
            throw new IllegalArgumentException("Le fichier JSON est vide ou invalide.");
        }

        // Champs obligatoires
        String fichierProcessus = getRequiredString(root, "fichierProcessus");
        String fichierRessources = getRequiredString(root, "fichierRessourcesDisponibles");
        String fichierMetriques = getRequiredString(root, "fichierMetriquesGlobales");

        // Récupération de la liste des algorithmes
        JsonArray listeAlgoNode = root.getAsJsonArray("listeAlgorithmes");
        if (listeAlgoNode == null || listeAlgoNode.size() == 0) {
            throw new IllegalArgumentException("La liste des algorithmes doit être présente et non vide.");
        }

        List<AlgoConfiguration> listeAlgorithmes = new ArrayList<>();

        for (JsonElement elem : listeAlgoNode) {
            JsonObject algoObj = elem.getAsJsonObject();

            String nomAlgorithme = getRequiredString(algoObj, "nomAlgorithme");
            String fichierDet = getRequiredString(algoObj, "fichierResultatsDetailles");
            String fichierGlob = getRequiredString(algoObj, "fichierResultatsGlobaux");

            Integer quantum = null;
            JsonElement quantumNode = algoObj.get("quantum");
            if (quantumNode != null && !quantumNode.isJsonNull()) {
                quantum = quantumNode.getAsInt();
            }

            listeAlgorithmes.add(
                    new AlgoConfiguration(nomAlgorithme, fichierDet, fichierGlob, quantum)
            );
        }

        // Construction finale
        return new FileConfiguration(
                fichierProcessus,
                fichierMetriques,
                fichierRessources,
                listeAlgorithmes
        );
        }catch(Exception e){
            throw new IllegalArgumentException("Impossible de charger le fichier de configuration existant : \n"+e.getMessage());
        }

    }

    /**
     * Récupère un champ String obligatoire d'un JsonObject.
     */
    private static String getRequiredString(JsonObject obj, String key) {
        JsonElement element = obj.get(key);

        if (element == null || element.isJsonNull() || element.getAsString().isBlank()) {
            throw new IllegalArgumentException("Le champ \"" + key + "\" est obligatoire et ne peut pas être vide.");
        }

        return element.getAsString();
    }


}
