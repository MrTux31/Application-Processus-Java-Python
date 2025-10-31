package com.ordonnancement.service.parser.process.strategie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParserStrategy;
import com.ordonnancement.service.parser.FileParsingException;

/**
 * Stratégie de parsing JSON pour les objets Process.
 * 
 * Cette classe lit un fichier JSON contenant une liste des processus initiale
 * et crée pour chacun un objet à partir des champs :
 * id, dateSoumission, tempsExecution, requiredRam, deadline, priority.
 * 
 */

public class InitialProcessParser implements FileParserStrategy<Process>{
    
    /**
     * Permet de parser le fichier décrivant les processus à exécuter et d'en extraire
     * une liste d'objets Process
     * @return la liste des Process
     * @throws Exception : si le format du fichier est incorrect
     * 
     * Format attendu :
    * 
    * [
    *   {"idProcessus": 1, "dateSoumission": 0, "tempsExecution": 5, "requiredRam": 1024, "deadline": 20, "priority": 3},
    *   {"idProcessus": 2, "dateSoumission": 3, "tempsExecution": 4, "requiredRam": 512, "deadline": 15, "priority": 1}
    * ]
     */
    @Override
    public List<Process> parse(String cheminFichier){
    
      
       try {
            List<Process> liste = new ArrayList<>();
        
            String contenu = new String(Files.readAllBytes(Paths.get(cheminFichier))); //Lire le contenu du fichier 

            JSONArray tableauJson = new JSONArray(contenu); //Un tableau JSON à partir du contenu du fichier
            
            for(int i = 0; i<tableauJson.length(); i++){ //Parcours de chaque élément du tableau JSON (itère sur des processus représentés en JSON)

                JSONObject objetProcessusJSON = tableauJson.getJSONObject(i); //Récupération de l'élément actuel
                
                //Lecure des données de l'objet JSON et création d'un objet Process
                Process p = new Process(objetProcessusJSON.getInt("idProcessus"),objetProcessusJSON.getInt("dateSoumission"),objetProcessusJSON.getInt("tempsExecution"),objetProcessusJSON.getInt("requiredRam"),objetProcessusJSON.getInt("deadline"),objetProcessusJSON.getInt("priority"));
                liste.add(p); //On ajoute le processus créé à la liste

            }

        return liste;
        }catch (IOException e) {
            throw new FileParsingException("Impossible de lire le fichier : " + cheminFichier, e);
        } catch (JSONException e) {
            throw new FileParsingException("Le fichier n'est pas un JSON valide : " + cheminFichier, e);
        }
       
    
    
    
    }
    

}
