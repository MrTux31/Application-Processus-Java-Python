
import sys
import csv
from pathlib import Path

def tempsReponse(processus : dict) -> int:
    """
    Calcule le temps de réponse d’un processus.

    Paramètres
    ----------
    processus : dict
        Exemple :
        {
            'idProcessus': '2',
            'dateSoumission': 0,
            'tempsExecution': 3,
            'requiredRam': 1024,
            'tempsTotalExecution': 3,
            'tempsRestQuantum': 1,
            'dateDebut': 0,
            'dateFin': 3,
            'usedRam': 1024
        }

    Retour
    ------
    int
        Temps de réponse du processus, calculé comme :
        dateFin - dateSoumission
    """   
    return processus["dateFin"] - processus["dateSoumission"]

def tempsAttente(processus : dict):
    """
    Calcule le temps d’attente d’un processus avant son exécution.

    Paramètres
    ----------
    processus : dict
        Exemple :
        {
            'idProcessus': '2',
            'dateSoumission': 0,
            'tempsExecution': 3,
            'requiredRam': 1024,
            'tempsTotalExecution': 3,
            'tempsRestQuantum': 1,
            'dateDebut': 0,
            'dateFin': 3,
            'usedRam': 1024
        }

    Retour
    ------
    int
        Temps d’attente du processus, calculé comme :
        dateDebut - dateSoumission
    """
    
    return processus["dateDebut"] - processus["dateSoumission"]

def tempsAttenteMoyen(processus : list[dict]):
    """
    Calcule le temps d’attente moyen pour une liste de processus.

    Paramètres
    ----------
    processus : list[dict]
        Exemple :
        [
            {'idProcessus': '2', 'dateSoumission': 0, 'tempsExecution': 3, 'requiredRam': 1024,
             'tempsTotalExecution': 3, 'tempsRestQuantum': 1, 'dateDebut': 0, 'dateFin': 3, 'usedRam': 1024},
            {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6, 'requiredRam': 1024,
             'tempsTotalExecution': 6, 'tempsRestQuantum': 0, 'dateDebut': 0, 'dateFin': 6, 'usedRam': 1024},
            {'idProcessus': '3', 'dateSoumission': 3, 'tempsExecution': 6, 'requiredRam': 512,
             'tempsTotalExecution': 6, 'tempsRestQuantum': 0, 'dateDebut': 3, 'dateFin': 9, 'usedRam': 512}
        ]

    Retour
    ------
    float
        Temps d’attente moyen calculé comme la moyenne des temps d’attente
        individuels (dateDebut - dateSoumission) de tous les processus.
        Retourne 0 si la liste est vide.
    """
    
    if not processus:
        return 0
    
    somme_temps_attente = 0
    for p in processus: 
        somme_temps_attente += tempsAttente(p) #Additionne le tps d'attente de chaque processus
    return somme_temps_attente/len(processus) #Fait la moyenne

def tempsReponseMoyen(processus : list[dict]):
    """
    Calcule le temps de réponse moyen pour une liste de processus.

    Paramètres
    ----------
    processus : list[dict]
        Exemple :
        [
            {'idProcessus': '2', 'dateSoumission': 0, 'tempsExecution': 3, 'requiredRam': 1024,
             'tempsTotalExecution': 3, 'tempsRestQuantum': 1, 'dateDebut': 0, 'dateFin': 3, 'usedRam': 1024},
            {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6, 'requiredRam': 1024,
             'tempsTotalExecution': 6, 'tempsRestQuantum': 0, 'dateDebut': 0, 'dateFin': 6, 'usedRam': 1024},
            {'idProcessus': '3', 'dateSoumission': 3, 'tempsExecution': 6, 'requiredRam': 512,
             'tempsTotalExecution': 6, 'tempsRestQuantum': 0, 'dateDebut': 3, 'dateFin': 9, 'usedRam': 512}
        ]

    Retour
    ------
    float
        Temps de réponse moyen calculé comme la moyenne des temps de réponse
        individuels (dateFin - dateSoumission) de tous les processus.
        Retourne 0 si la liste est vide.
    """
    
    if not processus:
        return 0
    
    somme_temps_reponse = 0
    for p in processus: 
        somme_temps_reponse += tempsReponse(p) #Additionne le tps de réponse de chaque processus
    return somme_temps_reponse/len(processus) #Fait la moyenne




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