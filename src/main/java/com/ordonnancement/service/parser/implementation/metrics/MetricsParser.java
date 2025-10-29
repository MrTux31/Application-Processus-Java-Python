package com.ordonnancement.service.parser.implementation.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ordonnancement.model.Metrics;
import com.ordonnancement.service.parser.FileParser;
import com.ordonnancement.service.parser.FileParsingException;
/**
 * Classe permettant de parser le fichier "métriques globales"
 * 
 */
public class MetricsParser implements FileParser<Metrics>{

    /**
     * Permet de parser les fichiers listant les métriques globales par algo d'ordonnancement 
     * @param cheminFichier : le chemin du fichier à parser
     * @return La liste des métriques du fichiers
     * @throws Exception : si le format du fichier est incompatible
     * 
     * Format attendu : 
    * [
    *   {"nomAlgorithme": "Round-Robin", "tempsReponseMoyen": 5.3, "tempsAttenteMoyen": 4.6, "makespan": 40},
    *   {"nomAlgorithme": "FIFO", "tempsReponseMoyen": 2.3, "tempsAttenteMoyen": 5.4, "makespan": 30}
    * ]
     */

    @Override
    public List<Metrics> parse(String cheminFichier){

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


        try {
            List<Metrics> liste = new ArrayList<>();
        
            String contenu = new String(Files.readAllBytes(Paths.get(cheminFichier))); //Lire le contenu du fichier 

            JSONArray tableauJson = new JSONArray(contenu); //Un tableau JSON à partir du contenu du fichier
            
            for(int i = 0; i<tableauJson.length(); i++){ //Parcours de chaque élément du tableau JSON (itère sur des métriques pour chaque algo représentés en JSON)

                JSONObject objetMetrics = tableauJson.getJSONObject(i); //Récupération de l'élément actuel
                
                //Lecure des données de l'objet JSON et création d'un objet Metrics
                Metrics m = new Metrics(objetMetrics.getString("nomAlgorithme"),objetMetrics.getDouble("tempsReponseMoyen"),objetMetrics.getDouble("tempsAttenteMoyen"),objetMetrics.getInt("makespan"));
                liste.add(m); //On ajoute le processus créé à la liste

        }


        return liste;
        }catch (IOException e) {
        throw new FileParsingException("Impossible de lire le fichier : " + cheminFichier, e);
        } catch (JSONException e) {
            throw new FileParsingException("Le fichier n'est pas un JSON valide : " + cheminFichier, e);
        }
        
    }


}
