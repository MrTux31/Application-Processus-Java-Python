package com.ordonnancement.service.parser.implementation.process.strategie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParserStrategy;
import com.ordonnancement.service.parser.FileParsingException;

/**
 * Stratégie de parsing pour les objets Process.
 * 
 * Cette classe lit un fichier JSON contenant les résultats globaux de l'ordonnancement.
 * Permet de compléter les objets Process : en mettant à jour leur date de début et leur date de fin
 * 
 * 
 */

public class GlobalResultProcessParser implements FileParserStrategy<Process>{
    
    private List<Process> listeProcessus;

    /**
     * 
     * @param listeProcessus
     */
    public GlobalResultProcessParser(List<Process> listeProcessus){
        this.listeProcessus = listeProcessus;
    }


    /**
     * Permet de parser le fichier décrivant les résultats globaux de l'ordonnancement
     * et d'en extraire une liste d'objets Process
     * @return la liste des Process
     * @throws Exception : si le format du fichier est incorrect
     * 
     * Format attendu :
    * 
    * [
    *   {"idProcessus": 1, "dateSoumission": 0, "dateDebut": 5,"dateFin": 8, "requiredRam": 1024, "usedRam":"1024"},
    *   {"idProcessus": 2, "dateSoumission": 3,  "dateDebut": 9,"dateFin": 12,"requiredRam": 1024, "usedRam":"1024"}
    * ]
     */

    @Override
    public List<Process> parse(String cheminFichier) {
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
            //Création d'un Map pour accéder rapidement au processus en fonction de son ID
            Map<Integer,Process> mapProcessus = new HashMap<>();
            for(Process p : listeProcessus){
                mapProcessus.put(p.getId(),p);
            }
            
            String contenu = new String(Files.readAllBytes(Paths.get(cheminFichier))); //Lire le contenu du fichier 

            JSONArray tableauJson = new JSONArray(contenu); //Un tableau JSON à partir du contenu du fichier
            
            for(int i = 0; i<tableauJson.length(); i++){ //Parcours de chaque élément du tableau JSON (itère sur des processus représentés en JSON)

                //Récupération du processus actuel
                JSONObject objetProcessusJSON = tableauJson.getJSONObject(i); 
                int idProcessus =  objetProcessusJSON.getInt("idProcessus"); //Récupération de l'id du processus actuel
                Process p = mapProcessus.get(idProcessus); //Récupérer le processus auquel correspond l'id
                if(p!= null){ //Au cas où aucun processus avec cet id n'existe
                    //On met à jour l'objet processus : les dates de début et de fin
                    p.setDateDebut(objetProcessusJSON.getInt("dateDebut")); 
                    p.setDateFin(objetProcessusJSON.getInt("dateFin"));
                }

            }

            return listeProcessus;
        }catch (IOException e) {
        throw new FileParsingException("Impossible de lire le fichier : " + cheminFichier, e);
        } catch (JSONException e) {
            throw new FileParsingException("Le fichier n'est pas un JSON valide : " + cheminFichier, e);
        }
            
    
    
    }
    

}
