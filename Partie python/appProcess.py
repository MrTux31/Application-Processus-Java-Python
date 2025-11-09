import sys
import Parsing.parsing
from pathlib import Path
import Verifications.verifications
import Algos.roundRobin
import Algos.fifo
import Metriques.metriques


def charger_donnes():
    """
    Retourne un dictionnaire contenant toutes les données nécessaires :
    {
        "processus": liste des dictionnaires représentant les processus,
        "ressources": {
            "processeurs": liste des IDs,
            "nb_processeurs": int,
            "ram_tot": int
        },
        "algos": dictionnaire des algos à exécuter,
        "metriques": chemin du fichier de métriques
    }
    """
    #Récupération du fichier de configuration
    cheming_config = Path(sys.argv[1]) #Utilisation de Path pouir représenter le chemin de manière sécurisée, permet de tester son existance
    config = Parsing.parsing.parser_fichier_config(cheming_config) #Récupération de toutes les données du fichier de config
    
    #Récupérer le chemin de chaque fichiers
    fichier_processus = config[0]
    fichier_ressources = config[1]
    fichier_metriques = config[2]
    dict_algos = config[3] #Récupérer le dictionnaire des algos à exécuter

    #Lire les éléments des fichiers de données et les convertir en structures de données
    liste_processus = Parsing.parsing.parser_fichier_processus(fichier_processus)
    dict_ressources = Parsing.parsing.parser_fichier_ressources(fichier_ressources)

    #On lance une vérification des divers processus et des ressources récupérés dans les fichiers lus.
    Verifications.verifications.verifierRessources(dict_ressources) #Vérification des ressources
    Verifications.verifications.verifierProcessus(liste_processus,dict_ressources["ram_tot"]) #Vérification des processus
    Verifications.verifications.verifierAlgos(dict_algos) #Vérification des algos d'ordonnancement

    return {"processus" : liste_processus,"ressources" :dict_ressources, "algos":dict_algos, "metriques" : fichier_metriques}





def main():

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
                    metriques_round_robin = Algos.roundRobin.round_robin(donnees["algos"][algo], donnees["processus"], donnees["ressources"],donnees["metriques"]) 
                    metriques_moyennes.append(metriques_round_robin)
                case "FIFO":
                    Algos.fifo.fifo( 
                        donnees["algos"][algo],
                        donnees["processus"],
                        donnees["ressources"],
                        donnees["metriques"]
                    )

                case "PRIORITE":
                    pass
                    
                case _:
                    print("Algo inconnu : ",algo, file=sys.stderr)
                    sys.exit(3)
        #Affichage des métriques moyennes
        print("Métriques globales pour les différents algorithmes :")
        print("----------------------------------------------------")
        for m in metriques_moyennes:
            print(f"- {m["algo"]} | Temps d'attente moyen : {m["tempsAttenteMoyen"]} | Temps reponse moyen : {m["tempsReponseMoyen"]} | Makespan : {m["makespan"]}")    

        Metriques.metriques.enregistrerMetriques(donnees["metriques"],metriques_moyennes)
#Début du programme
if __name__ == "__main__":
    try:
        main()
    except SystemExit: #On attrape les sys.exit()
        raise #On les affiche
    except Exception as e:
        print("Erreur inattendue lors de l'exécution du programme.", e, file=sys.stderr)
        sys.exit(99)
