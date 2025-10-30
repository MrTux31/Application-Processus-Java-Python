package com.ordonnancement.service.parser.implementation.process.strategie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.model.Processor;
import com.ordonnancement.model.Schedule;
import com.ordonnancement.service.parser.FileParserStrategy;
import com.ordonnancement.service.parser.FileParsingException;

/**
 * Stratégie de parsing JSON pour les objets Process.
 * 
 * Cette classe lit un fichier JSON contenant les résultats détaillés de l'ordonnacement
 * Elle permet de stocker dans chaque Process les différentes assignations du processus à un processeur donné 
 * (enregistrement des objets Schedule dans la liste du Process)
 * 
 * 
 */

public class DetailedResultProcessParser implements FileParserStrategy<Process>{
    
    private List<Process> listeProcessus;

    /**
     * 
     * @param listeProcessus
     */
    public DetailedResultProcessParser(List<Process> listeProcessus){
        this.listeProcessus = listeProcessus;
    }

    /**
     * Permet de parser le fichier décrivant les résultats détaillés de l'ordonnancement
     * et d'en extraire une liste d'objets Process
     * @return la liste des Process
     * @throws Exception : si le format du fichier est incorrect
     * 
     * Format attendu :
    *
    * {
    *   "nomAlgorithme": "FIFO",
    *   "processus": [
    *     {
    *       "idProcessus": 1,
    *       "executions": [
    *         {"dateDebut": 5, "dateFin": 7, "idCpu": "CPU1"},
    *         {"dateDebut": 7, "dateFin": 10, "idCpu": "CPU2"}
    *       ]
    *     },
    *     {
    *       "idProcessus": 2,
    *       "executions": [
    *         {"dateDebut": 9, "dateFin": 13, "idCpu": "CPU1"}
    *       ]
    *     }
    *   ]
    * }
     */

 
    @Override
    public List<Process> parse(String cheminFichier) {
        
        try {
            
        
            //Création d'un Map pour accéder rapidement au processus en fonction de son ID
            Map<Integer,Process> mapProcessus = new HashMap<>();
            for(Process p : listeProcessus){
                mapProcessus.put(p.getId(),p);
            }
            
            String contenu = new String(Files.readAllBytes(Paths.get(cheminFichier))); //Lire le contenu du fichier 

            JSONObject racine = new JSONObject(contenu); // L'objet racine du JSON

            //Récupération du nom de l'algorithme
            String nomAlgo = racine.getString("nomAlgorithme");

            JSONArray tableauJson = racine.getJSONArray("processus"); //Un tableau JSON à partir du contenu du fichier
            

            for(int i = 0; i<tableauJson.length(); i++){ //Parcours de chaque élément du tableau JSON (itère sur des processus représentés en JSON)
                List<Schedule> listeSchedule = new ArrayList<>(); //Liste des assignations pour le processus actuel
                
                //Récupération du processus actuel
                JSONObject objetProcessusJSON = tableauJson.getJSONObject(i); 


                int idProcessus =  objetProcessusJSON.getInt("idProcessus"); //Récupération de l'id du processus actuel

                //Récupération du tableau listant les différentes assignations du processus sur un processeur
                JSONArray tableauExecutions = objetProcessusJSON.getJSONArray("executions"); 

                for(int j = 0; j<tableauExecutions.length();j++){ //Parcours des différentes executions du processus actuel (le ième)
                    JSONObject objetScheduleJSON = tableauExecutions.getJSONObject(j);  //Récupération de l'execution j du processus i
                    
                     
                    Processor p = new Processor(objetScheduleJSON.getString("idCpu")); //Création du processeur avec son id
                    Schedule s = new Schedule(p, objetScheduleJSON.getInt("dateDebut"),objetScheduleJSON.getInt("dateFin"));//Création du schedule sur le processus concerné et pour les dates début et fin spécifiées
                    listeSchedule.add(s); //On ajoute le l'assignement à la liste

                }

                Process p = mapProcessus.get(idProcessus); //Récupérer le processus auquel correspond l'id
                if(p!= null){//Au cas où aucun processus avec cet id n'existe
                    
                    ExecutionInfo infoExecution = p.getExecutionInfo(nomAlgo); //On récup l'execution pour cet algorithme dans le processus
                    if(infoExecution == null){ //Si elle est null, on la crée, elle n'a pas encore été insérée
                        infoExecution = new ExecutionInfo(); //On crée l'execution info pour ce processus
                        p.addExecution(nomAlgo, infoExecution); //On l'ajoute l'execution dans le processus
                    }
                    infoExecution.setSchedules(listeSchedule); //On ajoute les différents schedules réalisés dans l'execution
                        
                }
                else{ //Si p est null, cela veux dire que le processus qu'on tente de récupérer n'a pas été déclaré dans la liste des processus initiale.
                    throw new FileParsingException("Incohérence détectée : le processus " + idProcessus + 
                                   " dans les résultats détaillés n'existe pas dans la liste initiale.");
                }
                
            }
            
            return listeProcessus;
        } catch (IOException e) {
        throw new FileParsingException("Impossible de lire le fichier : " + cheminFichier, e);
        } catch (JSONException e) {
            throw new FileParsingException("Le fichier n'est pas un JSON valide : " + cheminFichier, e);
        }
    
    
    }
    

}
