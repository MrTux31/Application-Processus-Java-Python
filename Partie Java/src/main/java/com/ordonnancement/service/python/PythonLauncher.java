package com.ordonnancement.service.python;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * Classe permettant de lancer le script python
 */

public class PythonLauncher {

    /**
     * Exécute un script Python avec un fichier de configuration en paramètre.
     *
     * @param cheminScript chemin du script Python
     * @param cheminConfig fichier de configuration JSON à passer en argument
     */
    public static void runPythonScript(Path cheminScript, String cheminConfig){

        //TO DO : Faire en sorte que ca marche pour linux: python3 et faire en sorte que ca marche sur windows avec juste "python" : attention variable path non configurée

        String commande[] = {"C:\\Users\\anton\\AppData\\Local\\Programs\\Python\\Python312\\python.exe", cheminScript.toString(), cheminConfig}; //Commande à exécuter pour lancer le script python
        ProcessBuilder builder = new ProcessBuilder(commande); //Construction de la commande
        
        try {
            //Démarrage du processus
            Process process = builder.start();

            // Variable pour stocker les erreurs
            StringBuilder erreurs = new StringBuilder(); //Stringbuilder pour stocker les lignes d'erreurs de manière plus performante

            //Tentative de lecture des erreurs
            try (BufferedReader readerErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) { //process.getErrorStream() pour lire la sortie d'erreur du processus
                String ligneErreur; //Stocke une ligne d'erreur

                while ((ligneErreur = readerErr.readLine()) != null) { //Tant qu'il reste des erreurs à lire
                    erreurs.append(ligneErreur); //Stocker les différentes erreurs
                }
            }

            int codeRetour = process.waitFor(); //Attente de la fin du script
            if (codeRetour != 0) { //Si il a rencontré une erreur
                throw new PythonException("Le script Python a rencontré une erreur\n" + erreurs);
            }

        } catch (IOException e) {
            throw new PythonException("Impossible d’exécuter le script Python : " + cheminScript, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PythonException("Exécution du script Python interrompue.", e);
        }
    }
}
