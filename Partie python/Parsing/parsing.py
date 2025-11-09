import csv 
import json
import sys
from pathlib import Path
def parser_fichier_config(chemin_fichier):

    # Vérifie que le fichier existe
    if not chemin_fichier.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print(f"Le fichier de configuration {chemin_fichier} est innexistant.",file=sys.stderr)
        sys.exit(2)
    try:
        # Lecture et parsing du JSON
        with open(chemin_fichier, "r", encoding="utf-8") as f:
            #Tentative d'ouverture du JSON
            config = json.load(f)
       

    
        #Récupération du chemin de chaque fichiers
        fichier_processus = Path(config["fichierProcessus"].strip())  #Tentative de conversion des chaines en Path (si cela échoue, déclenche direct exception)
        fichier_ressources = Path(config["fichierRessourcesDisponibles"].strip())
        fichier_metriques = Path(config["fichierMetriquesGlobales"].strip())
        
        dict_algos = {}
        # Récupération de la liste des algorithmes à exécuter
        for algo in config["listeAlgorithmes"]:
            dict_algos[algo["nomAlgorithme"]] = {"fichierResultatsDetailles" : Path(algo["fichierResultatsDetailles"]), #Tentative de conversion des chaines en Path (si cela échoue, déclenche direct exception)
            "fichierResultatsGlobaux": Path(algo["fichierResultatsGlobaux"]),
            "quantum": algo["quantum"]}

    except json.JSONDecodeError:
        print("Erreur d'ouverture du fichier de configuration", file=sys.stderr)
        sys.exit(4)

    except KeyError as e: #Si une clé est manquante dans le fichier de config
        print(f"Champ manquant dans le fichier de configuration : {e}", file=sys.stderr)
        sys.exit(5)
    except Exception:
            # Attrape toutes les erreurs inconnues
            print("Erreur inattendue lors du parsing du fichier de configuration", file=sys.stderr)
            sys.exit(99)
    
    return fichier_processus,fichier_ressources,fichier_metriques,dict_algos


#Utilisé pour parser le fichier des processus
def parser_fichier_processus(chemin_fichier):
    # Vérifie que le fichier existe
    if not chemin_fichier.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print(f"Le fichier {chemin_fichier} est innexistant.",file=sys.stderr)
        sys.exit(2)
    
    try:
        liste_elements = []
        with open(chemin_fichier, newline='') as fichier: #Ouvrir le fichier CSV
            for ligne in csv.DictReader(fichier): #Pour chaque ligne dans le fichier CSV
                liste_elements.append(ligne) #Ajout d'un dictionnaire (représentant la ligne du CSV courante) dans la liste 
    except Exception:
            # Attrape toutes les erreurs inconnues
            print("Erreur inattendue lors du parsing du fichier des processus", file=sys.stderr)
            sys.exit(99)

    return liste_elements    

#Utilisé pour parser le fichier des ressources
def parser_fichier_ressources(chemin_fichier):
     # Vérifie que le fichier existe
    if not chemin_fichier.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print(f"Le fichier de ressources {chemin_fichier} est innexistant.",file=sys.stderr)
        sys.exit(2)
    try:
        # Lecture et parsing du JSON
        with open(chemin_fichier, "r", encoding="utf-8") as f:
            #Tentative d'ouverture du JSON
            ressources = json.load(f)
       
        
            #Récupération des différentes données
            nb_processeurs = int(ressources["nombreTotalProcesseurs"])
            ram_totale = int(ressources["ramTotale"])
            liste_processeurs =  [processeur["idProcesseur"] for processeur in ressources["processeurs"]]
        
    except json.JSONDecodeError:
        print("Erreur d'ouverture du fichier de ressources", file=sys.stderr)
        sys.exit(4)
    except KeyError as e: #Si une clé est manquante dans le fichier des ressources
        print(f"Champ manquant dans le fichier des ressources : {e.args[0]}", file=sys.stderr)
        sys.exit(6)

    except Exception:
        # Attrape toutes les erreurs inconnues
        print("Erreur inattendue lors du parsing du fichier de ressources", file=sys.stderr)
        sys.exit(99)

    return {"processeurs" : liste_processeurs, "nb_processeurs" :nb_processeurs, "ram_tot" :ram_totale,}

