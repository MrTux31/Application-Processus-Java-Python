import sys
from pathlib import Path
import Verifications.verifications
import Algos.roundRobin
import Algos.fifo
import Algos.priorite
import ManipulationFichiers.Writing.writing
import ManipulationFichiers.Parsing.parsing


def charger_donnes():
    """
    Charge et vérifie toutes les données nécessaires à l’ordonnancement.

    Lit le fichier de configuration, puis charge :
    - la liste des processus à ordonnancer (CSV),
    - les ressources disponibles (JSON),
    - les algorithmes à exécuter et leurs paramètres,
    - le chemin du fichier de métriques globales.

    Effectue également les vérifications de cohérence sur les données chargées.

    Retour
    ------
    dict
        Dictionnaire contenant toutes les données nécessaires à l’ordonnancement.

    Exemple de retour :
    -----------------
    {
        'processus': [
            {'idProcessus': '1', 'dateSoumission': '0', 'tempsExecution': '6', 'requiredRam': '1024', 'deadline': '20', 'priority': '3'},
            {'idProcessus': '2', 'dateSoumission': '0', 'tempsExecution': '3', 'requiredRam': '1024', 'deadline': '15', 'priority': '1'},
            {'idProcessus': '3', 'dateSoumission': '3', 'tempsExecution': '6', 'requiredRam': '512', 'deadline': '15', 'priority': '1'}
        ],
        'ressources': {'processeurs': ['CPU1', 'CPU2'], 'nb_processeurs': 2, 'ram_tot': 8000},
        'algos': {
            'ROUND ROBIN': {'fichierResultatsDetailles': Path('Resultats/RoundRobin/rDetailedROUNDROBIN.csv'),
                            'fichierResultatsGlobaux': Path('Resultats/RoundRobin/rGlobauxROUNDROBIN.csv'),
                            'quantum': 2}
        },
        'metriques': Path('Resultats/fichierMetriquesGlobales.csv')
    }
    """
    #Récupération du fichier de configuration
    cheming_config = Path(sys.argv[1]) #Utilisation de Path pouir représenter le chemin de manière sécurisée, permet de tester son existance
    config = ManipulationFichiers.Parsing.parsing.parser_fichier_config(cheming_config) #Récupération de toutes les données du fichier de config
    
    #Récupérer le chemin de chaque fichiers
    fichier_processus = config[0]
    fichier_ressources = config[1]
    fichier_metriques = config[2]
    dict_algos = config[3] #Récupérer le dictionnaire des algos à exécuter

    #Lire les éléments des fichiers de données et les convertir en structures de données
    liste_processus = ManipulationFichiers.Parsing.parsing.parser_fichier_processus(fichier_processus)
    dict_ressources = ManipulationFichiers.Parsing.parsing.parser_fichier_ressources(fichier_ressources)

    #On lance une vérification des divers processus et des ressources récupérés dans les fichiers lus.
    Verifications.verifications.verifierRessources(dict_ressources) #Vérification des ressources
    Verifications.verifications.verifierProcessus(liste_processus,dict_ressources["ram_tot"]) #Vérification des processus
    Verifications.verifications.verifierAlgos(dict_algos) #Vérification des algos d'ordonnancement

    return {"processus" : liste_processus,"ressources" :dict_ressources, "algos":dict_algos, "metriques" : fichier_metriques}





def main():
    """
    Point d’entrée principal du programme Python d’ordonnancement.

    Lit le fichier de configuration passé en argument, charge toutes les données 
    nécessaires (processus, ressources, algorithmes), puis exécute chaque 
    algorithme d’ordonnancement demandé (Round Robin, FIFO, Priorité, etc.).

    À la fin de l’exécution :
    - affiche les métriques globales de chaque algorithme (temps moyen, makespan…)
    - enregistre les métriques globales dans le fichier défini dans la configuration.

    En cas d’erreur (chemin manquant, algo inconnu, etc.), 
    affiche un message sur stderr et quitte.

    Exemple d’exécution
    -------------------
    $ python appProcess.py config.json

    Exemple de sortie
    -----------------
    Métriques globales pour les différents algorithmes :
    ----------------------------------------------------
    - ROUND ROBIN | Temps d'attente moyen : 4.0 | Temps reponse moyen : 2.5 | Makespan : 18
    """
    if len(sys.argv) < 2 :
        print("Chemin du fichier de configuration manquant !", file=sys.stderr)
        sys.exit(1)
    else:
        #Récupération des données 
        donnees = charger_donnes()
        metriques_moyennes = []

        #Parcours du dictionnaire contenant les algos d'ordonnancement à exécuter
        for algo in donnees["algos"]:

            #Vérification de la correspondance du nom de l'algo, pour l'executer
            match algo.strip().upper():  #Enlever espaces et forcer les majuscules sur le nom d'algo
                case "ROUND ROBIN":
                    #On exécute le round robin (passage en params des paramètres de l'algo : chemins fichiers sortie et Quantum)
                    #Récupération de ses métriques (moyennes)
                    metriques_round_robin = Algos.roundRobin.round_robin(donnees["algos"][algo], donnees["processus"], donnees["ressources"]) 
                    metriques_moyennes.append(metriques_round_robin)
                case "FIFO":
                    #On exécute le fifo (passage en params des paramètres de l'algo : chemins fichiers sortie
                    #Récupération de ses métriques (moyennes)
                    metriques_fifo = Algos.fifo.fifo(
                    donnees["algos"][algo],
                    donnees["processus"],
                    donnees["ressources"],
                    donnees["metriques"])
                    metriques_moyennes.append(metriques_fifo)

                case "PRIORITE":
                    metrique_priorite = Algos.priorite.priorite(
                    donnees["algos"][algo],
                    donnees["processus"],
                    donnees["ressources"],
                    donnees["metriques"]
                    )
                    metriques_moyennes.append(metrique_priorite)
                    
                case _:
                    print("Algo inconnu : ",algo, file=sys.stderr)
                    sys.exit(3)
        #Affichage des métriques moyennes
        print("Métriques globales pour les différents algorithmes :")
        print("----------------------------------------------------")
        for m in metriques_moyennes:
           print(f"- {m['algo']} | Temps d'attente moyen : {m['tempsAttenteMoyen']} | Temps reponse moyen : {m['tempsReponseMoyen']} | Makespan : {m['makespan']}")   

        ManipulationFichiers.Writing.writing.enregistrerMetriques(donnees["metriques"],metriques_moyennes)
#Début du programme
if __name__ == "__main__":
    try:
        main()
    except SystemExit: #On attrape les sys.exit()
        raise #On les affiche
    except Exception as e:
        print("Erreur inattendue lors de l'exécution du programme.", e, file=sys.stderr)
        sys.exit(99)
