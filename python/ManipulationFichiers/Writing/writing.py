import sys
import csv
from pathlib import Path

def enregistrer_resultats(nom_algo,processus, infos_allocations_processeur,params_algos):
    """
    Enregistre les résultats de l'ordonnancement dans deux fichiers CSV : global et détaillé.

    Paramètres :
    ----------

    nom_algo : str
        Le nom de l'algorithme ex:  "ROUND ROBIN"

    processus : list[dict]
        Liste des processus terminés. Exemple :
        [
            {"idProcessus": "1", "dateSoumission": 0, "dateDebut": 0, "dateFin": 6,
             "requiredRam": 1024, "usedRam": None, "tempsExecution": 6,
             "deadline": 20, "priority": 3, "tempsTotalExecution": 6, "tempsRestQuantum": 0},
            {"idProcessus": "2", "dateSoumission": 0, "dateDebut": 0, "dateFin": 3,
             "requiredRam": 512, "usedRam": None, "tempsExecution": 3,
             "deadline": 15, "priority": 1, "tempsTotalExecution": 3, "tempsRestQuantum": 0}
        ]

    infos_allocations_processeur : list[dict]
        Liste des allocations CPU détaillées. Exemple :
        [
            {"idProcessus": "1", "dateDebut": 0, "dateFin": 2, "idProcesseur": "CPU1"},
            {"idProcessus": "2", "dateDebut": 0, "dateFin": 2, "idProcesseur": "CPU2"},
            {"idProcessus": "1", "dateDebut": 2, "dateFin": 4, "idProcesseur": "CPU1"}
        ]

    params_algos : dict
        Paramètres de l'algorithme. Exemple :
        {
          'fichierResultatsDetailles': Path('Resultats/RoundRobin'),
                'fichierResultatsGlobaux': Path('Resultats/RoundRobin'),
                'quantum': 2
        }
    """
    
    fichier_detaille = params_algos["fichierResultatsDetailles"]
    fichier_global = params_algos["fichierResultatsGlobaux"]
    
    # Vérifie que le dossier parent existe bien
    if not fichier_detaille.parent.exists() or not fichier_global.parent.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print(f"Chemins de fichiers de résultats incorrects pour l'algo {nom_algo}",file=sys.stderr)
        sys.exit(12)
    
    try :
        #Enregistrer le fichier d'informations globales des processus---------------------
        with open(fichier_global, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=["idProcessus", "dateSoumission", "dateDebut","dateFin","requiredRam","usedRam"])
            writer.writeheader()  # écrit la première ligne (les noms de colonnes)
            for p in processus: #Pour chaque processus, on enregistre la ligne dans le csv
                writer.writerow({ 
                    "idProcessus": p["idProcessus"],
                    "dateSoumission": p["dateSoumission"],
                    "dateDebut": p["dateDebut"],
                    "dateFin": p["dateFin"],  
                    "requiredRam": p["requiredRam"],
                    "usedRam": p["usedRam"]
                })

        #Enregistrer le fichier d'informations détaillées des processus---------------------
        with open(fichier_detaille, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=["idProcessus", "dateDebut","dateFin","idProcesseur"])
            writer.writeheader()  # écrit la première ligne (les noms de colonnes)
            writer.writerows(infos_allocations_processeur)
    #Si le chemin du fichier est incorrect
    except FileNotFoundError as e:
        print(f"Erreur d'enregistrement des fichiers de résultats pour {nom_algo} : {e}", file=sys.stderr)
        sys.exit(10)
    #Si il n'y a pas de permissions d'écritures dans la destination
    except PermissionError as e:
        print(f"Erreur d'enregistrement des fichiers de résultats pour {nom_algo}, des permissions sont manquantes : {e}", file=sys.stderr)
        sys.exit(11)
    except Exception:
        print(f"Erreur d'enregistrement inconnue des fichiers de résultats pour {nom_algo}", file=sys.stderr)
        sys.exit(11)


def enregistrerMetriques(chemin_fichier : Path, metriques : list[dict]):
    
    """
    Enregistre les métriques globales des algorithmes d’ordonnancement dans un fichier CSV.

    Paramètres
    ----------
    chemin_fichier : Path
        Exemple : Path('Resultats/fichierMetriquesGlobales.csv')
        Chemin complet du fichier CSV où enregistrer les métriques.

    metriques : list[dict]
        Exemple :
        [
            {'algo': 'ROUND ROBIN', 'tempsAttenteMoyen': 0.0, 'tempsReponseMoyen': 5.0, 'makespan': 9}
        ]
        Chaque dictionnaire contient les métriques calculées pour un algorithme :
        - 'algo' : nom de l’algorithme
        - 'tempsAttenteMoyen' : moyenne des temps d’attente
        - 'tempsReponseMoyen' : moyenne des temps de réponse
        - 'makespan' : date de fin du dernier processus

    Vérifie que le dossier parent du fichier existe et gère les erreurs d’écriture.
    Termine avec des erreurs si des problèmes sont détectés.
    """
    # Vérifie que le dossier parent existe bien
    if not chemin_fichier.parent.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print("Chemins du fichier de métriques incorrect",file=sys.stderr)
        sys.exit(12)
    
    try :
       #Enregistrer les métriques
        with open(chemin_fichier, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=["algo", "tempsAttenteMoyen", "tempsReponseMoyen","makespan"])
            writer.writeheader()  # écrit la première ligne (les noms de colonnes)
            #Pour chaque processus, on enregistre la ligne dans le csv
            writer.writerows(metriques)

    #Si le chemin du fichier est incorrect
    except FileNotFoundError as e:
        print(f"Erreur d'enregistrement du fichier des métriques : {e}", file=sys.stderr)
        sys.exit(10)
    #Si il n'y a pas de permissions d'écritures dans la destination
    except PermissionError as e:
        print(f"Erreur d'enregistrement du fichier des métriques, des permissions sont manquantes : {e}", file=sys.stderr)
        sys.exit(11)
    except Exception:
        print("Erreur d'enregistrement inconnue du fichier des métriques", file=sys.stderr)
        sys.exit(11)