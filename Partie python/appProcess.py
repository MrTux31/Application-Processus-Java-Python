import sys
import csv 
import json
from pathlib import Path

import roundRobin


def parser_fichier_config(chemin_fichier):

    # Vérifie que le fichier existe
    if not chemin_fichier.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print(f"Le fichier de configuration {chemin_fichier} est innexistant.",file=sys.stderr)
        sys.exit(2)

    # Lecture et parsing du JSON
    with open(chemin_fichier, "r", encoding="utf-8") as f:
        try: #Tentative d'ouverture du JSON
            config = json.load(f)
        except json.JSONDecodeError as e:
            print(f"Erreur d'ouverture du fichier de configuration : {e}", file=sys.stderr)
            sys.exit(4)

    try:
        #Récupération du chemin de chaque fichiers
        fichier_processus = config["fichierProcessus"]
        fichier_ressources = config["fichierRessourcesDisponibles"]
        fichier_metriques = config["fichierMetriquesGlobales"]
        
        dict_algos = {}
        # Récupération de la liste des algorithmes à exécuter
        for algo in config["listeAlgorithmes"]:
            dict_algos[algo["nomAlgorithme"]] = {"fichierResultatsDetailles" : algo["fichierResultatsDetailles"],
            "fichierResultatsGlobaux": algo["fichierResultatsGlobaux"],
            "quantum": algo["quantum"]}
    except KeyError as e: #Si une clé est manquante dans le fichier de config
        print(f"Champ manquant dans le fichier de configuration : {e.args[0]}", file=sys.stderr)
        sys.exit(5)
    
    return fichier_processus,fichier_ressources,fichier_metriques,dict_algos


#Utilisé pour parser le fichier des processus
def parser_fichier_processus(chemin_fichier):
    # Vérifie que le fichier existe
    if not chemin_fichier.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print(f"Le fichier {chemin_fichier} est innexistant.",file=sys.stderr)
        sys.exit(2)
    
    liste_elements = []
    with open(chemin_fichier, newline='') as fichier: #Ouvrir le fichier CSV
        for ligne in csv.DictReader(fichier): #Pour chaque ligne dans le fichier CSV
            liste_elements.append(ligne) #Ajout d'un dictionnaire (représentant la ligne du CSV courante) dans la liste 
            
    return liste_elements    

#Utilisé pour parser le fichier des ressources
def parser_fichier_ressources(chemin_fichier):
     # Vérifie que le fichier existe
    if not chemin_fichier.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print(f"Le fichier de ressources {chemin_fichier} est innexistant.",file=sys.stderr)
        sys.exit(2)

    # Lecture et parsing du JSON
    with open(chemin_fichier, "r", encoding="utf-8") as f:
        try: #Tentative d'ouverture du JSON
            ressources = json.load(f)
        except json.JSONDecodeError as e:
            print(f"Erreur d'ouverture du fichier de ressources : {e}", file=sys.stderr)
            sys.exit(4)
        try:
            #Récupération des différentes données
            nb_processeurs = ressources["nombreTotalProcesseurs"]
            ram_totale = ressources["ramTotale"]
            liste_processeurs =  [processeur["idProcesseur"] for processeur in ressources["processeurs"] ]
        except KeyError as e: #Si une clé est manquante dans le fichier des ressources
            print(f"Champ manquant dans le fichier des ressources : {e.args[0]}", file=sys.stderr)
            sys.exit(6)

        #Gestion incohérences
        if len(liste_processeurs) != nb_processeurs: #Si le nb de processeurs est incohérent
            print("Erreur dans le fichier des ressources, il y a un nombre total de CPU différent du nombre réel", file=sys.stderr)
            sys.exit(7)
        if len(liste_processeurs) == 0:
            print("Erreur dans le fichier des ressources, il n'y a aucun CPU spécifié", file=sys.stderr)
            sys.exit(8)
        return {"processeurs" : liste_processeurs, "nb_processeurs" :nb_processeurs, "ram_tot" :ram_totale,}


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
    config = parser_fichier_config(cheming_config) #Récupération de toutes les données du fichier de config
    
    #Récupérer le chemin de chaque fichiers
    fichier_processus = Path(config[0])
    fichier_ressources = Path(config[1])
    fichier_metriques = Path(config[2])
    dict_algos = config[3] #Récupérer le dictionnaire des algos à exécuter

    #Lire les éléments des fichiers de données et les convertir en structures de données
    liste_processus = parser_fichier_processus(fichier_processus)
    dict_ressources = parser_fichier_ressources(fichier_ressources)

    return {"processus" : liste_processus,"ressources" :dict_ressources, "algos":dict_algos, "metriques" : fichier_metriques}




#Début du programme


if len(sys.argv) < 2 :
    print("Chemin du fichier de configuration manquant !", file=sys.stderr)
    sys.exit(1)
else:
    #Récupération des données 
    donnees = charger_donnes()
    
    #Parcours du dictionnaire contenant les algos d'ordonnancement à exécuter
    for algo in donnees["algos"]:

        #Vérification de la correspondance du nom de l'algo, pour l'executer
        match algo.strip().upper():  #Enlever espaces et forcer les majuscules sur le nom d'algo
            case "ROUND ROBIN":
                
                roundRobin.round_robin(donnees["algos"][algo], donnees["processus"], donnees["ressources"],donnees["metriques"]) #On exécute le round robin (passage en params des paramètres de l'algo : chemins fichiers sortie et Quantum)
                
            case "FIFO":
                
                pass

            case "PRIORITE" :

                pass
            case _:
                print("Algo inconnu : ",algo, file=sys.stderr)
                sys.exit(3)



