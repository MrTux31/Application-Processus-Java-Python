
import sys
import csv
from pathlib import Path

def tempsReponse(processus : dict):
    return processus["dateFin"] - processus["dateSoumission"]

def tempsAttente(processus : dict):
    return processus["dateDebut"] - processus["dateSoumission"]

def tempsAttenteMoyen(processus : list[dict]):
    
    if not processus:
        return 0
    
    somme_temps_attente = 0
    for p in processus: 
        somme_temps_attente += tempsAttente(p) #Additionne le tps d'attente de chaque processus
    return somme_temps_attente/len(processus) #Fait la moyenne

def tempsReponseMoyen(processus : list[dict]):
    if not processus:
        return 0
    
    somme_temps_reponse = 0
    for p in processus: 
        somme_temps_reponse += tempsReponse(p) #Additionne le tps de réponse de chaque processus
    return somme_temps_reponse/len(processus) #Fait la moyenne




def enregistrerMetriques(chemin_fichier : Path, metriques : list[dict]):
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