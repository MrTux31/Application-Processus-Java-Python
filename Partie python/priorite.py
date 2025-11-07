import sys, csv, json
from pathlib import Path

def enregistrer_resultats_priorite(processus, infos_allocations_processeur, params_algos):
    """
    Ecrit deux CSV : global (par processus) et détaillé (allocations CPU).
    Crée automatiquement les dossiers parents si besoin.
    """
    # Normalisation des chemins
    fichier_detaille = Path(str(params_algos["fichierResultatsDetailles"]).strip())
    fichier_global   = Path(str(params_algos["fichierResultatsGlobaux"]).strip())

    # Créer les dossiers parents si nécessaire
    try:
        if str(fichier_detaille.parent) not in (".", ""):
            fichier_detaille.parent.mkdir(parents=True, exist_ok=True)
        if str(fichier_global.parent) not in (".", ""):
            fichier_global.parent.mkdir(parents=True, exist_ok=True)
    except PermissionError as e:
        print(f"Erreur d'enregistrement des fichiers de résultats pour priorite, des permissions sont manquantes : {e}", file=sys.stderr)
        sys.exit(11)
    except Exception as e:
        # Si on n'arrive même pas à préparer les dossiers, sortir avec 12 (cohérent avec ton code)
        print(f"Chemins de fichiers de résultats incorrects pour priorite : {e}", file=sys.stderr)
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
        print(f"Erreur d'enregistrement des fichiers de résultats pour priorite : {e}", file=sys.stderr)
        sys.exit(10)
    except PermissionError as e:
        print(f"Erreur d'enregistrement des fichiers de résultats pour priorite, des permissions sont manquantes : {e}", file=sys.stderr)
        sys.exit(11)

def enregistrer_date_fin_alloc(infos_allocations_processeur, pe, date):
    """
    Met à jour la date de fin de la dernière allocation CPU d'un processus donné (priorite).

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
    Initialise la liste des processus pour l'algorithme priorite.
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
            "deadline": int(p["deadline"]),                 # Date limite d'exécution
            "priority": int(p["priority"]),                 # Priorité du processus
            "tempsTotalExecution": 0,                       # Temps déjà exécuté (0 au départ)
            "dateDebut": None,                              # Défini au premier passage CPU
            "dateFin": None,                                # Défini à la fin de l’exécution
            "usedRam": None                                 # RAM réellement utilisée (optionnelle)
        })

    # Trie les processus par ordre d'arrivée (priorite)
    nouvelle_liste.sort(key=lambda x: x["dateSoumission"])

    # Retourne la liste des processus prêts à être exécutés
    return nouvelle_liste

def soumettre_processus_priorite(date: int, processus_attente_soumission: list, processus_file_attente: list):
    """
    Retourne la liste des processus soumis à la date donnée pour l'algorithme priorite.
    """

    # Parcours de tous les processus en attente de soumission
    for ps in list(processus_attente_soumission):  # Copie de la liste pour itérer en toute sécurité
        if date == ps["dateSoumission"]:  # Si le processus doit être soumis à cette date
            processus_file_attente.append(ps)       # Ajout à la file d'attente principale
            processus_attente_soumission.remove(ps) # Retrait de la liste d'attente
    

    processus_file_attente.sort(key=lambda p: p["priority"], reverse=True)  # Tri par priorité décroissante


def allouer_cpu(processus_file_attente: list, processeurs_dispos: list, processus_elus: list,
                infos_allocations_processeur: list, date: int):
    """
    Alloue les CPU libres aux processus en attente.
    """
    #Parcours de tous les processus en file attente
    for pfa in list(processus_file_attente):
        #Si au moins un processeur est dispo
        if len(processeurs_dispos) > 0:
            cpu = processeurs_dispos.pop(0) #On prends un cpu dispo dans la liste (le premier) 
            #Allocation a un CPU
            processus_elus.append({"processus" : pfa, "processeur" : cpu }) #On peut alors élire le processus, sur un cpu 
            processus_file_attente.remove(pfa) #On supprime le processus de la fil d'attente
            #Enregistrement des premières infos sur l'allocation
            infos_allocations_processeur.append({"idProcessus" : pfa["idProcessus"],
            "dateDebut" : date, "dateFin" : None, "idProcesseur": cpu})


def executer_processus_elus(processus_elus: list, processus_termines: list, 
                            processeurs_dispos: list, infos_allocations_processeur: list, date: int,):
    """
    Met à jour le temps d'exécution des processus élus, gère la fin ou le quantum.
    """
    #Parcours de tous les processus élus
    for pe in list(processus_elus):

        #Enregistrement de la date de début d'execution du processus si ce n'est pas déjà fait
        if pe["processus"]["dateDebut"] is None: 
            pe["processus"]["dateDebut"] = date 
        
        #On incrémente le temps pendant lequel le processus s'est exécuté
        pe["processus"]["tempsTotalExecution"] +=1

        #Si le processus s'est executé pendant le temps qui était prévu, on le met dans la liste des processus terminés
        if pe["processus"]["tempsTotalExecution"] == pe["processus"]["tempsExecution"]:
            pe["processus"]["dateFin"] = date+1 #Enregistrement de la date de fin (+1 sur la date pour qu'elle soit exacte)
            processus_termines.append(pe["processus"]) #Le processus est terminé
            enregistrer_date_fin_alloc(infos_allocations_processeur,pe,date+1) #Enregistrement de la date de fin de l'alloc (+1 pour qu'elle soit exacte)
            processeurs_dispos.append(pe["processeur"]) #Le processeur utilisé est à nouveau disponible
            processus_elus.remove(pe) #Supression des processus élus

# code non-finalisé
         