from datetime import date
from pathlib import Path
import sys
import csv

def enregistrer_resultats_fifo(processus, infos_allocations_processeur, params_algos):
    """
    Ecrit deux CSV : global (par processus) et détaillé (allocations CPU).
    Crée automatiquement les dossiers parents si besoin.
    """
    # Normalisation des chemins
    fichier_detaille = Path(str(params_algos["fichierResultatsDetailles"]).strip())
    fichier_global   = Path(str(params_algos["fichierResultatsGlobaux"]).strip())

    # Avertir sur 'quantum' uniquement s'il a une valeur non nulle
    if params_algos.get("quantum") not in (None, "", 0):
        print("Avertissement: 'quantum' fourni mais ignoré pour l'algorithme FIFO.", file=sys.stderr)

    # Créer les dossiers parents si nécessaire
    try:
        if str(fichier_detaille.parent) not in (".", ""):
            fichier_detaille.parent.mkdir(parents=True, exist_ok=True)
        if str(fichier_global.parent) not in (".", ""):
            fichier_global.parent.mkdir(parents=True, exist_ok=True)
    except PermissionError as e:
        print(f"Erreur d'enregistrement des fichiers de résultats pour FIFO, des permissions sont manquantes : {e}", file=sys.stderr)
        sys.exit(11)
    except Exception as e:
        # Si on n'arrive même pas à préparer les dossiers, sortir avec 12 (cohérent avec ton code)
        print(f"Chemins de fichiers de résultats incorrects pour FIFO : {e}", file=sys.stderr)
        sys.exit(12)

    # Écriture des fichiers
    try:
        # Global par processus
        with open(fichier_global, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(
                f,
                fieldnames=["idProcessus", "dateSoumission", "dateDebut", "dateFin", "requiredRam", "usedRam"]
            )
            writer.writeheader()
            for p in processus:
                writer.writerow({
                    "idProcessus":   p.get("idProcessus"),
                    "dateSoumission":p.get("dateSoumission"),
                    "dateDebut":     p.get("dateDebut"),
                    "dateFin":       p.get("dateFin"),
                    "requiredRam":   p.get("requiredRam"),
                    "usedRam":       p.get("usedRam")
                })

        # Détail allocations CPU
        with open(fichier_detaille, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(
                f,
                fieldnames=["idProcessus", "dateDebut", "dateFin", "idProcesseur"]
            )
            writer.writeheader()
            writer.writerows(infos_allocations_processeur)

    except FileNotFoundError as e:
        print(f"Erreur d'enregistrement des fichiers de résultats pour FIFO : {e}", file=sys.stderr)
        sys.exit(10)
    except PermissionError as e:
        print(f"Erreur d'enregistrement des fichiers de résultats pour FIFO, des permissions sont manquantes : {e}", file=sys.stderr)
        sys.exit(11)


    
    
def enregistrer_date_fin_alloc(infos_allocations_processeur, pe, date):
    """
    Met à jour la date de fin de la dernière allocation CPU d'un processus donné (FIFO).

    Contrairement à Round Robin, il n'y a pas de quantum à gérer : la mise à jour se fait
    uniquement quand le processus termine son exécution.

    Paramètres
    ----------
    infos_allocations_processeur : list[dict]
        Liste des allocations CPU. Exemple :
        [
            {"idProcessus": "1", "dateDebut": 0, "dateFin": None, "idProcesseur": "CPU1"},
            {"idProcessus": "2", "dateDebut": 2, "dateFin": None, "idProcesseur": "CPU2"}
        ]

    pe : dict
        Processus en cours avec le CPU utilisé. Exemple :
        {
            "processus": {
                "idProcessus": "1",
                "dateSoumission": 0,
                "tempsExecution": 6,
                "requiredRam": 1024,
                "deadline": 20,
                "priority": 3,
                "tempsTotalExecution": 6,
                "dateDebut": 0,
                "dateFin": None,
                "usedRam": None
            },
            "processeur": "CPU1"
        }

    date : int
        La date (unité de temps) à enregistrer comme date de fin pour l'allocation.
    """
    # Recherche de la dernière allocation en cours du processus sur ce processeur
    for alloc in reversed(infos_allocations_processeur):  # Inverser la liste pour retrouver plus vite
        if alloc["idProcessus"] == pe["processus"]["idProcessus"] and alloc["dateFin"] is None:
            alloc["dateFin"] = date  # Le processus quitte le CPU à ce moment précis
            break


def initialiser_processus(processus, ram_dispo):
    """
    Initialise la liste des processus pour l'algorithme FIFO.
    Vérifie la RAM disponible et trie les processus par date de soumission.
    """

    nouvelle_liste = []

    # Parcours de tous les processus fournis
    for p in processus:
        # Vérifie si le processus peut être exécuté avec la RAM disponible
        if int(p["requiredRam"]) > ram_dispo:
            print(
                f"Impossible d'exécuter le processus {p['idProcessus']} : "
                f"{p['requiredRam']} > {ram_dispo}",
                file=sys.stderr
            )
            sys.exit(9)  # Quitte le programme avec un code d’erreur spécifique

        # Création d’un dictionnaire enrichi pour le processus
        nouvelle_liste.append({
            "idProcessus": p["idProcessus"],                # Identifiant du processus
            "dateSoumission": int(p["dateSoumission"]),     # Moment où il arrive dans la file
            "tempsExecution": int(p["tempsExecution"]),     # Durée totale d'exécution
            "requiredRam": int(p["requiredRam"]),           # Mémoire requise
            "deadline": int(p["deadline"]),                 # Date limite (si utilisée)
            "priority": int(p["priority"]),                 # Priorité (non utilisée en FIFO)
            "tempsTotalExecution": 0,                       # Temps déjà exécuté
            "dateDebut": None,                              # Sera défini au premier passage CPU
            "dateFin": None,                                # Sera défini à la fin de l’exécution
            "usedRam": None                                 # RAM réellement utilisée (optionnelle)
        })

    # Trie les processus par ordre d'arrivée (FIFO)
    nouvelle_liste.sort(key=lambda x: x["dateSoumission"])

    # Retourne la liste des processus prêts à être exécutés
    return nouvelle_liste

def soumettre_processus(date: int, processus_attente_soumission: list, processus_file_attente: list):
    """
    Ajoute à la file d'attente les processus dont la date de soumission correspond à la date actuelle (FIFO).
    """
    # Parcours de tous les processus en attente de soumission
    for ps in list(processus_attente_soumission):  # Copie de la liste pour itérer en toute sécurité
        if date == ps["dateSoumission"]:  # Si le processus doit être soumis à cette date
            processus_file_attente.append(ps)       # Ajout à la file d'attente principale
            processus_attente_soumission.remove(ps) # Retrait de la liste d'attente

def allouer_cpu(processus_file_attente: list, processeurs_dispos: list, processus_elus: list,
                infos_allocations_processeur: list, date: int):
    """
    Alloue les CPU libres aux processus en attente (FIFO).
    Un processus prend le premier processeur disponible et y reste jusqu’à sa fin.
    """
    # Parcours des processus en file d’attente
    for pfa in list(processus_file_attente):
        # Si au moins un processeur est disponible
        if len(processeurs_dispos) > 0:
            cpu = processeurs_dispos.pop(0)  # On prend le premier CPU libre
            
            # Le processus est élu et associé à ce CPU
            processus_elus.append({"processus": pfa, "processeur": cpu})
            processus_file_attente.remove(pfa)  # On le retire de la file d’attente
            
            # Enregistrement de l’allocation CPU (début)
            infos_allocations_processeur.append({
                "idProcessus": pfa["idProcessus"],
                "dateDebut": date,
                "dateFin": None,
                "idProcesseur": cpu
            })

def executer_processus_elus(processus_elus: list, processus_file_attente: list,
                            processus_termines: list, processeurs_dispos: list,
                            infos_allocations_processeur: list, date: int):
    """
    Exécute d'un tick les processus élus (FIFO).
    - Pas de quantum : un processus reste sur le CPU jusqu'à sa fin.
    - Quand un processus termine, on libère le CPU et on clôt l'allocation.
    """
    for pe in list(processus_elus):
        # Initialisation de la date de premier début si nécessaire
        if pe["processus"]["dateDebut"] is None:
            pe["processus"]["dateDebut"] = date

        # Avancement d'unité de temps
        pe["processus"]["tempsTotalExecution"] += 1

        # Si le processus a terminé toute son exécution
        if pe["processus"]["tempsTotalExecution"] == pe["processus"]["tempsExecution"]:
            pe["processus"]["dateFin"] = date + 1  # +1 pour la vraie date de fin (inclusif)
            processus_termines.append(pe["processus"])  # Archiver comme terminé

            # Clôturer l'allocation CPU en cours
            enregistrer_date_fin_alloc(infos_allocations_processeur, pe, date + 1)

            # Libérer le processeur et retirer des élus
            processeurs_dispos.append(pe["processeur"])
            processus_elus.remove(pe)
        # Sinon (pas terminé), on ne fait rien :
        # le processus continue sur le même CPU au tick suivant.

def fifo(params_algo: dict, processus: list[dict], ressources_dispo: dict, fichier_metriques: str):
    """
    Exécute l'algorithme FIFO sur un ensemble de processus et enregistre les résultats.

    Paramètres
    ----------
    params_algo : dict
        Ex. {
            "fichierResultatsDetailles": "rDetailedFIFO.csv",
            "fichierResultatsGlobaux": "rGlobauxFIFO.csv"
        }
        (Aucun 'quantum' en FIFO)

    processus : list[dict]
        Ex. [
            {"idProcessus": "1", "dateSoumission": "0", "tempsExecution": "6", "requiredRam": "1024",
             "deadline": "20", "priority": "3"},
            {"idProcessus": "2", "dateSoumission": "0", "tempsExecution": "3", "requiredRam": "512",
             "deadline": "15", "priority": "1"},
            {"idProcessus": "3", "dateSoumission": "3", "tempsExecution": "6", "requiredRam": "512",
             "deadline": "15", "priority": "1"}
        ]

    ressources_dispo : dict
        Ex. {
            "processeurs": ["CPU1", "CPU2"],
            "nb_processeurs": 2,
            "ram_tot": 8192
        }

    fichier_metriques : str
        Chemin vers le CSV de métriques globales (si tu en génères).
    """
    # Ressources
    processeurs_dispos = list(ressources_dispo["processeurs"])  # copie pour éviter de modifier l'entrée
    ram_dispo = ressources_dispo["ram_tot"]

    # Journal des allocations CPU (début/fin)
    infos_allocations_processeur = []

    # Horloge d'ordonnancement
    date = 0

    # Files et états
    processus_attente_soumission = initialiser_processus(processus, ram_dispo)  # (FIFO: pas de quantum)
    processus_file_attente = []
    processus_elus = []
    processus_termines = []

    # Boucle principale: tant qu'il reste quelque chose à traiter
    while processus_attente_soumission or processus_file_attente or processus_elus:
        # 1) Soumettre les processus dont la dateSoumission == date
        soumettre_processus(date, processus_attente_soumission, processus_file_attente)
        # 2) Allouer les CPUs libres aux processus en file d'attente
        allouer_cpu(processus_file_attente, processeurs_dispos, processus_elus,
                    infos_allocations_processeur, date)
        # 3) Exécuter un tick des processus élus (FIFO: pas de quantum)
        executer_processus_elus(processus_elus, processus_file_attente, processus_termines,
                                processeurs_dispos, infos_allocations_processeur, date)
        # 4) Avancer le temps
        date += 1

    # Enregistrer les résultats de l'ordonnancement (fichiers détaillé & global)
    enregistrer_resultats_fifo(processus_termines, infos_allocations_processeur, params_algo)

