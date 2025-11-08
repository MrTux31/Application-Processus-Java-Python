from pathlib import Path
import sys
import csv

def enregistrer_resultats(processus, infos_allocations_processeur,params_algos):
    """
    Enregistre les résultats de l'ordonnancement dans deux fichiers CSV : global et détaillé.

    Paramètres :
    ----------
    processus : list[dict]
        Liste des processus terminés. Exemple :
        [
            {"idProcessus": "1", "dateSoumission": 0, "dateDebut": 0, "dateFin": 6,
             "requiredRam": 1024, "usedRam": None, "tempsExecution": 6,
             "deadline": 20, "priority": 3, "tempsTotalExecution": 6, "tempsRestQuantum": 0},
            {"idProcessus": "2", "dateSoumission": 0, "dateDebut": 0, "dateFin": 3,
             "requiredRam": 512, "usedRam": None, "tempsExecution": 3,
             "deadline": 15, "priority": 1, "tempsTotalExecution": 3, "tempsRestQuantum": 1}
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
            "fichierResultatsDetailles": "rDetailedROUNDROBIN.csv",
            "fichierResultatsGlobaux": "rGlobauxROUNDROBIN.csv",
            "quantum": 2
        }
    """
    
    fichier_detaille = Path(params_algos["fichierResultatsDetailles"].strip())
    fichier_global = Path(params_algos["fichierResultatsGlobaux"].strip())
    
    # Vérifie que le dossier parent existe bien
    if not fichier_detaille.parent.exists() or not fichier_global.parent.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print("Chemins de fichiers de résultats incorrects pour le Round Robin",file=sys.stderr)
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
        print(f"Erreur d'enregistrement des fichiers de résultats pour Round Robin : {e}", file=sys.stderr)
        sys.exit(10)
    #Si il n'y a pas de permissions d'écritures dans la destination
    except PermissionError as e:
        print(f"Erreur d'enregistrement des fichiers de résultats pour Round Robin, des permissions sont manquantes : {e}", file=sys.stderr)
        sys.exit(11)
    


def enregistrer_date_fin_alloc(infos_allocations_processeur,pe, date):
    """
    Met à jour la date de fin de la dernière allocation CPU d'un processus donné.

    Paramètres :
    ----------
    infos_allocations_processeur : list[dict]
        Liste des allocations CPU. Exemple :
        [
            {"idProcessus": "1", "dateDebut": 0, "dateFin": None, "idProcesseur": "CPU1"},
            {"idProcessus": "2", "dateDebut": 0, "dateFin": None, "idProcesseur": "CPU2"}
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
                "tempsTotalExecution": 2,
                "tempsRestQuantum": 2,
                "dateDebut": 0,
                "dateFin": None,
                "usedRam": None
            },
            "processeur": "CPU1"
        }

    date : int
        La date (unité de temps) à enregistrer comme date de fin pour l'allocation.
    """
    # Trouver la dernière allocation correspondant à ce processus sur ce CPU
    for alloc in reversed(infos_allocations_processeur): #Inverser la liste pour retrouver l'allocation plus rapidement
        if alloc["idProcessus"] == pe["processus"]["idProcessus"] and alloc["dateFin"] is None: #on cherche la dernière allocation non terminée du processus
            alloc["dateFin"] = date  # Le processus quitte le CPU à ce moment précis
            break

def initialiser_processus(processus: list[dict], ram_dispo: int, quantum: int) -> list[dict]:
    """
    Transforme la liste brute de processus en une liste enrichie pour Round-Robin.
    """
    nouvelle_liste = []
    
    #Ajout des processus dans la liste de processus en attente de soumission
    for p in processus:    
        nouvelle_liste.append({
            "idProcessus": p["idProcessus"],
            "dateSoumission": int(p["dateSoumission"]),
            "tempsExecution": int(p["tempsExecution"]), #Le nombre d'unités de temps pendant lesquelle le processus doit s'executer
            "requiredRam": int(p["requiredRam"]),
            "deadline": int(p["deadline"]),
            "priority": int(p["priority"]),
            "tempsTotalExecution": 0,   #Le nombre d'unité de temps pendant lequel le processus s'est executé
            "tempsRestQuantum" : quantum,
            "dateDebut": None,# date de premier début
            "dateFin": None,
            "usedRam" : None
        })

    #Tri des processus par date de soumission croissante
    nouvelle_liste.sort(key=lambda processus:processus["dateSoumission"]) 
    return nouvelle_liste

def soumettre_processus(date: int, processus_attente_soumission: list, processus_file_attente: list):
    """
    Ajoute les processus dont la date de soumission est atteinte à la file d'attente.
    """
    #Parcours de tous les processus en attente de soumission
    for ps in list(processus_attente_soumission): #Création copie liste pour pouvoir parcourir et suppr des elts de manière sécu
        if date == ps["dateSoumission"]: #Si la date de soumission du processus est la meme que la date actuelle
            processus_file_attente.append(ps) #Le processus est alors soumis, ajout à la vraie file d'attente, 
            processus_attente_soumission.remove(ps)# Suppression du processus de la liste attente soumission

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

def executer_processus_elus(processus_elus: list, processus_file_attente: list,
                            processus_termines: list, processeurs_dispos: list,
                            infos_allocations_processeur: list, date: int, quantum: int):
    """
    Met à jour le temps d'exécution des processus élus, gère la fin ou le quantum.
    """
    #Parcours de tous les processus élus
    for pe in list(processus_elus):
        #Enregistrement de la date de début d'execution du processus    
        if pe["processus"]["dateDebut"] is None:#Si la date de début n'a pas encore été initialisée
            pe["processus"]["dateDebut"] = date 
        
        #On décrémente le quantum restant au processus
        pe["processus"]["tempsRestQuantum"] -= 1   
        #On incrémente le temps pendant lequel le processus s'est exécuté
        pe["processus"]["tempsTotalExecution"] +=1

        #Si le processus s'est executé pendant le temps qui était prévu, on le met dans la liste des processus terminés
        if pe["processus"]["tempsTotalExecution"] == pe["processus"]["tempsExecution"]:
            pe["processus"]["dateFin"] = date+1 #Enregistrement de la date de fin (+1 sur la date pour avoir la VRAIE date de fin)
            processus_termines.append(pe["processus"]) #Le processus est terminé
            enregistrer_date_fin_alloc(infos_allocations_processeur,pe,date+1) #Enregistrement de la date de fin de l'alloc (+1 sur la date pour avoir la VRAIE date de fin)
            processeurs_dispos.append(pe["processeur"]) #Le processeur utilisé est à nouveau disponible
            processus_elus.remove(pe) #Supression des processus élus
            
        else:
            
            #Si le processus élu à épuisé le quantum de temps
            if pe["processus"]["tempsRestQuantum"] == 0:
                processus_file_attente.append(pe["processus"]) #On renvoie le processus en file d'attente
                # Réinitialiser le quantum pour le prochain passage
                pe["processus"]["tempsRestQuantum"] = quantum
                enregistrer_date_fin_alloc(infos_allocations_processeur,pe,date+1) #Enregistrement de la date de fin de l'alloc (+1 sur la date pour avoir la VRAIE date de fin)
                processeurs_dispos.append(pe["processeur"]) #Le processeur utilisé est à nouveau disponible
                processus_elus.remove(pe) #Supression du processus de la liste des élus
            


def round_robin(params_algo : dict, processus : list[dict], ressources_dispo : dict, fichier_metriques : str):
    """
    Exécute l'algorithme Round-Robin sur un ensemble de processus et enregistre les résultats.

    Paramètres :
    ----------
    params_algo : dict
        Dictionnaire contenant les paramètres de l'algorithme. Exemple :
        {
            "fichierResultatsDetailles": "rDetailedROUNDROBIN.csv",
            "fichierResultatsGlobaux": "rGlobauxROUNDROBIN.csv",
            "quantum": 2
        }

    processus : list[dict]
        Liste des processus à exécuter. Chaque processus est un dictionnaire avec au moins :
        [
            {"idProcessus": "1", "dateSoumission": "0", "tempsExecution": "6", "requiredRam": "1024",
             "deadline": "20", "priority": "3"},
            {"idProcessus": "2", "dateSoumission": "0", "tempsExecution": "3", "requiredRam": "512",
             "deadline": "15", "priority": "1"},
            {"idProcessus": "3", "dateSoumission": "3", "tempsExecution": "6", "requiredRam": "512",
             "deadline": "15", "priority": "1"}
        ]

    ressources_dispo : dict
        Dictionnaire contenant les ressources disponibles. Exemple :
        {
            "processeurs": ["CPU1", "CPU2"],
            "nb_processeurs": 2,
            "ram_tot": 8192
        }

    fichier_metriques : str
        Chemin du fichier CSV pour stocker les métriques globales. Exemple :
        "fichierMetriquesGlobales.csv"
    """
    
    
    quantum = params_algo["quantum"]
    processeurs_dispos = ressources_dispo["processeurs"]
    ram_dispo = ressources_dispo["ram_tot"]
    infos_allocations_processeur = [] #Liste permettant de sauvegarder toutes les allocations qui ont été réalisées



    date = 0 #Variable permettant de sauvegarder la date courante de l'ordonnancement

    #Liste des processus en attente de soumission
    processus_attente_soumission = initialiser_processus(processus,ram_dispo,quantum)
    # File des processus en attente d'execution
    processus_file_attente = []
    #Liste des processus élus, en cours d'execution sur un cpu
    processus_elus = []
    #Liste des processus terminées
    processus_termines = []
    
   

    #Tant qu'il reste des processus à traiter
    while processus_attente_soumission or processus_file_attente or processus_elus:

        #Effectuer les 4 actions
        soumettre_processus(date,processus_attente_soumission,processus_file_attente)
        allouer_cpu(processus_file_attente,processeurs_dispos,processus_elus,infos_allocations_processeur,date)
        executer_processus_elus(processus_elus,processus_file_attente,processus_termines,processeurs_dispos,infos_allocations_processeur,date,quantum)
        date += 1 #Incrémentation de la date 

    #Enregistrer les résultats de l'ordonnancement dans les deux fichiers de résultats       
    enregistrer_resultats(processus_termines,infos_allocations_processeur, params_algo)


