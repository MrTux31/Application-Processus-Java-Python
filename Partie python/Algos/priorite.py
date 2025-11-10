import sys, csv, json
from pathlib import Path
from Metriques import metriques
import ManipulationFichiers.Writing.writing

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
            
            
def priorite(params_algo: dict, processus: list[dict], ressources_dispo: dict, fichier_metriques: str):
    """
    Exécute l'algorithme de planification par priorité (priorité la plus élevée d'abord).
    - Pas de quantum.
    - La RAM n'est pas gérée ici : un processus prêt s'exécute dès qu'un CPU est libre.
    """
    # Copie de la liste des CPU disponibles (mutable pendant la simulation)
    processeurs_dispos = list(ressources_dispo["processeurs"])
    # La RAM n'est pas utilisée dans la logique priorite, mais on la conserve pour homogénéité d'interface
    ram_totale = int(ressources_dispo.get("ram_tot", 0))
    # Historique des allocations CPU (pour l'export détaillé)
    infos_allocations_processeur = []
    # Horloge discrète de la simulation (ticks)
    date = 0

    # Préparation des structures: tri initial par date de soumission
    processus_attente_soumission = initialiser_processus(processus, ram_totale)
    processus_file_attente = []
    processus_elus = []
    processus_termines = []

    # Boucle principale: soumettre -> allouer -> exécuter -> avancer l'horloge
    while processus_attente_soumission or processus_file_attente or processus_elus:
        # Ajoute à la file d'attente les processus dont la date de soumission == date
        soumettre_processus_priorite(date, processus_attente_soumission, processus_file_attente)
        # Attribue les CPU libres aux processus les plus prioritaires en attente
        allouer_cpu(processus_file_attente, processeurs_dispos, processus_elus, infos_allocations_processeur, date)
        # Fait progresser d'un tick les processus en cours; termine et libère CPU si fini
        executer_processus_elus(processus_elus, processus_termines, processeurs_dispos, infos_allocations_processeur, date)
        # Avance le temps d'une unité
        date += 1

    #Enregistrer les résultats de l'ordonnancement dans les deux fichiers de résultats       
    ManipulationFichiers.Writing.writing.enregistrer_resultats("PRIORITE",processus_termines,infos_allocations_processeur, params_algo)
    

    # Calcul des métriques finales sur les processus terminés
    tempsAttenteMoyen = metriques.tempsAttenteMoyen(processus_termines)
    tempsReponseMoyen = metriques.tempsReponseMoyen(processus_termines)

    return {
        "algo": "PRIORITE",
        "tempsAttenteMoyen": tempsAttenteMoyen,
        "tempsReponseMoyen": tempsReponseMoyen,
        "makespan": date
    }