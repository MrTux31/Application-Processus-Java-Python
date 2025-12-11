

from Metriques import metriques
import ManipulationFichiers.Writing.writing

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

def initialiser_processus(processus: list[dict], quantum: int) -> list[dict]:
    """
    Transforme la liste brute de processus en une liste enrichie pour Round-Robin.

    Chaque processus reçoit des champs supplémentaires pour suivre son exécution :
    - tempsTotalExecution : temps déjà exécuté
    - tempsRestQuantum : quantum restant avant retour en file d'attente
    - dateDebut / dateFin : dates de début et de fin d'exécution
    - usedRam : RAM utilisée par le processus

    Paramètres :
    ----------
    processus : list[dict]
        Liste des processus à initialiser. Chaque élément est un dictionnaire
        Exemple :
        [
            {'idProcessus': '1', 'dateSoumission': '0', 'tempsExecution': '6', 'requiredRam': '1024', 'deadline': '20', 'priority': '1'},
            {'idProcessus': '2', 'dateSoumission': '0', 'tempsExecution': '3', 'requiredRam': '1024', 'deadline': '21', 'priority': '2'},
            {'idProcessus': '3', 'dateSoumission': '0', 'tempsExecution': '5', 'requiredRam': '1024', 'deadline': '22', 'priority': '3'}
        ]

    quantum : int
        Quantum de temps alloué à chaque processus pour l’algorithme Round-Robin.
        Exemple : 2

    Retour :
    -------
    list[dict]
        Liste des processus enrichis et triés par date de soumission, avec les champs supplémentaires pour l'exécution :
        [
            {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6, 'requiredRam': 1024,
             'priority': 1, 'tempsTotalExecution': 0, 'tempsRestQuantum': 2,
             'dateDebut': None, 'dateFin': None, 'usedRam': None},
            {'idProcessus': '2', 'dateSoumission': 0, 'tempsExecution': 3, 'requiredRam': 1024,
             'priority': 2, 'tempsTotalExecution': 0, 'tempsRestQuantum': 2,
             'dateDebut': None, 'dateFin': None, 'usedRam': None}..... etc
        ]
    """
    nouvelle_liste = []
    
    #Ajout des processus dans la liste de processus en attente de soumission
    for p in processus:    
        nouvelle_liste.append({
            "idProcessus": p["idProcessus"],
            "dateSoumission": int(p["dateSoumission"]),
            "tempsExecution": int(p["tempsExecution"]), #Le nombre d'unités de temps pendant lesquelle le processus doit s'executer
            "requiredRam": int(p["requiredRam"]),
            "priority": int(p.get("priority", 0)), #Priorité du processus
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
    Ajoute à la file d'attente tous les processus dont la date de soumission
    est atteinte à la date courante et les trie par priorité décroissante.

    Paramètres :
    ----------
    date : int
        Date actuelle (unité de temps) dans la simulation.
        Exemple : 0

    processus_attente_soumission : list
        Liste des processus en attente de soumission. Chaque processus est un dictionnaire :
        Exemple initial :
        [
            {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6, 'requiredRam': 1024,
             'priority': 1, 'tempsTotalExecution': 0, 'tempsRestQuantum': 2, 'dateDebut': None,
             'dateFin': None, 'usedRam': None},
            {'idProcessus': '2', 'dateSoumission': 0, 'tempsExecution': 3, 'requiredRam': 1024,
             'priority': 2, 'tempsTotalExecution': 0, 'tempsRestQuantum': 2, 'dateDebut': None,
             'dateFin': None, 'usedRam': None},
            {'idProcessus': '3', 'dateSoumission': 0, 'tempsExecution': 5, 'requiredRam': 1024,
             'priority': 3, 'tempsTotalExecution': 0, 'tempsRestQuantum': 2, 'dateDebut': None,
             'dateFin': None, 'usedRam': None}
        ]

    processus_file_attente : list
        Liste des processus en attente d'exécution sur un CPU. Les processus soumis à la date courante y sont ajoutés.
        Exemple après soumission à la date 0 :
        [
            {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6, 'requiredRam': 1024,
             'priority': 1, 'tempsTotalExecution': 0, 'tempsRestQuantum': 2, 'dateDebut': None,
             'dateFin': None, 'usedRam': None},
            {'idProcessus': '3', 'dateSoumission': 0, 'tempsExecution': 5, 'requiredRam': 1024,
             'priority': 3, 'tempsTotalExecution': 2, 'tempsRestQuantum': 2, 'dateDebut': 0,
             'dateFin': None, 'usedRam': 1024}
        ]
    """
    nouveaux_processus = [] #Liste des processus qui vont être soumis et rentrer dans la file d'attente
    #Parcours de tous les processus en attente de soumission
    for ps in processus_attente_soumission: #Création copie liste pour pouvoir parcourir et suppr des elts de manière sécu
        if date == ps["dateSoumission"]: #Si la date de soumission du processus est la meme que la date actuelle
            nouveaux_processus.append(ps) #Ajout de tous les processus ayant cette date de soumissions
    
    #Si différents processus ont la meme date de soumission, alors on les trie par priorité décroissante
    nouveaux_processus.sort(key=lambda p:  -p["priority"])
    for p in nouveaux_processus:
        processus_file_attente.append(p) #Le processus est alors soumis, ajout à la vraie file d'attente, 
        processus_attente_soumission.remove(p)# Suppression du processus de la liste attente soumission

   


def allouer_cpu(processus_file_attente: list, processeurs_dispos: list, processus_elus: list,
                infos_allocations_processeur: list, date: int, ram_dispo : int) -> int:
    """
        Alloue les CPU disponibles aux processus en attente et met à jour les allocations.
        Si un processus n'a pas encore de RAM allouée, celle-ci est réservée.
        Renvoie la quantité de RAM restante après les allocations.

        Paramètres :
        ----------
        processus_file_attente : list
            Liste des processus en attente de CPU. Chaque élément est un dictionnaire de type :
            Exemple :
            [
                {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6,
                    'requiredRam': 1024, 'priority': 1, 'tempsTotalExecution': 0,
                    'tempsRestQuantum': 2, 'dateDebut': None, 'dateFin': None, 'usedRam': None},
                {'idProcessus': '2', 'dateSoumission': 0, 'tempsExecution': 3,
                    'requiredRam': 1024, 'priority': 2, 'tempsTotalExecution': 0,
                    'tempsRestQuantum': 2, 'dateDebut': None, 'dateFin': None, 'usedRam': None}
            ]

        processeurs_dispos : list
            Liste des CPU disponibles pour l'allocation.
            Exemple : ['CPU1', 'CPU2']

        processus_elus : list
            Liste des processus qui ont été élus et sont en cours d'exécution.
            Chaque élément est un dictionnaire de la forme :
            {"processus": {...}, "processeur": "CPUx"}
            Exemple :
            [
                {'processus': {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6,
                                'requiredRam': 1024, 'priority': 1, 'tempsTotalExecution': 0,
                                'tempsRestQuantum': 2, 'dateDebut': None, 'dateFin': None,
                                'usedRam': 1024}, 'processeur': 'CPU1'}
            ]

        infos_allocations_processeur : list
            Historique des allocations CPU pour chaque processus.
            Exemple :
            [
                {'idProcessus': '1', 'dateDebut': 0, 'dateFin': None, 'idProcesseur': 'CPU1'},
                {'idProcessus': '2', 'dateDebut': 0, 'dateFin': None, 'idProcesseur': 'CPU2'}
            ]

        date : int
            Date actuelle (unité de temps) dans la simulation.
            Exemple : 0

        ram_dispo : int
            Quantité de RAM disponible avant l'allocation.
            Exemple : 8192

        Retour :
        -------
        int
            Quantité de RAM restante après l'allocation des CPU et la réservation de la RAM pour les processus élus.
            Exemple : 6144 si deux processus de 1024 ont été alloués.
    """
    ram_restante = ram_dispo #On stocke la qte de ram disponible

    #Parcours de tous les processus en file attente
    for pfa in list(processus_file_attente):

        #Si au moins un processeur est dispo et que la quantité de ram  restante est suffisante
        if (len(processeurs_dispos) > 0 and ram_restante  >= pfa["requiredRam"] and pfa["usedRam"] is None):
            pfa["usedRam"] = pfa["requiredRam"] #On récupère la ram nécessaire au fonctionnement du processus (on l'enregistre pour la rendre plus tard)
            ram_restante = ram_restante - pfa["requiredRam"]
            cpu = processeurs_dispos.pop(0) #On prends un cpu dispo dans la liste (le premier) 
            #Allocation a un CPU
            processus_elus.append({"processus" : pfa, "processeur" : cpu }) #On peut alors élire le processus, sur un cpu 
            processus_file_attente.remove(pfa) #On supprime le processus de la fil d'attente
            #Enregistrement des premières infos sur l'allocation
            infos_allocations_processeur.append({"idProcessus" : pfa["idProcessus"],
            "dateDebut" : date, "dateFin" : None, "idProcesseur": cpu})
        
        #Dans le cas où le processus à déjà de la ram allouée, il reprends son exécution sans reset la mémoire
        elif len(processeurs_dispos) > 0 and pfa["usedRam"] is not None: 
            cpu = processeurs_dispos.pop(0) #On prends un cpu dispo dans la liste (le premier) 
            #Allocation a un CPU
            processus_elus.append({"processus" : pfa, "processeur" : cpu }) #On peut alors élire le processus, sur un cpu 
            processus_file_attente.remove(pfa) #On supprime le processus de la fil d'attente
            #Enregistrement des premières infos sur l'allocation
            infos_allocations_processeur.append({"idProcessus" : pfa["idProcessus"],
            "dateDebut" : date, "dateFin" : None, "idProcesseur": cpu})

    
    return ram_restante

def executer_processus_elus(processus_elus: list, processus_file_attente: list,
                            processus_termines: list, processeurs_dispos: list,
                            infos_allocations_processeur: list, date: int, quantum: int) -> int:
    """
    Met à jour le temps d'exécution des processus élus, gère la fin ou le quantum. Renvoie la qte de ram libérée par les processus finis

    Paramètres :
    ----------
    processus_elus : list
        Liste des dictionnaires représentant les processus actuellement en cours d'exécution. Exemple : 
        [
                {'processus': {'idProcessus': '2', 'dateSoumission': 0, 'tempsExecution': 3,
                            'requiredRam': 1024, 'priority': 2, 'tempsTotalExecution': 0,
                            'tempsRestQuantum': 2, 'dateDebut': None, 'dateFin': None,
                            'usedRam': 1024}, 'processeur': 'CPU1'},
                {'processus': {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6,
                            'requiredRam': 1024, 'priority': 1, 'tempsTotalExecution': 0,
                            'tempsRestQuantum': 2, 'dateDebut': None, 'dateFin': None,
                            'usedRam': 1024}, 'processeur': 'CPU2'}
            ]
    
    processus_file_attente : list
        Liste des processus en attente d'exécution. Les processus qui ont épuisé leur quantum
        mais ne sont pas terminés y sont renvoyés. Exemple : 
        [

        {'idProcessus': '1', 'dateSoumission': 0, 'tempsExecution': 6, 'requiredRam': 1024, 
        'priority': 1, 'tempsTotalExecution': 0, 'tempsRestQuantum': 2, 'dateDebut': None, 'dateFin': None, 'usedRam': None}
        
        ]

    processus_termines : list
        Liste des processus terminés. Les processus finis pendant cet appel y sont ajoutés.
    
    processeurs_dispos : list
        Exemple : ['CPU1', 'CPU2']

    infos_allocations_processeur : list
        Liste contenant l'historique des allocations processeur pour chaque processus.    
        Exemple : 
        [
            {'idProcessus': '2', 'dateDebut': 0, 'dateFin': None, 'idProcesseur': 'CPU1'},
            {'idProcessus': '1', 'dateDebut': 0, 'dateFin': None, 'idProcesseur': 'CPU2'}
        ]
    date : int
        Date actuelle (unité de temps) dans la simulation d'ordonnancement.
        Exemple initial : 0

    quantum : int
        Quantum de temps alloué pour chaque processus élu. Si un processus épuise ce quantum
        sans terminer, il est renvoyé dans la file d'attente avec le quantum réinitialisé.
        Exemple : 2
    
    Retour :
    -------
    int
        Quantité totale de RAM libérée par les processus qui se sont terminés
        pendant cet appel de fonction.
        Exemple : 1024 si un processus terminant libère 1024 de RAM.
    
    """
    

    ram_liberee = 0 #Variable qui stocke la quantitée de ram libérée par les processus terminés

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
        if pe["processus"]["tempsTotalExecution"] >= pe["processus"]["tempsExecution"]:
            pe["processus"]["dateFin"] = date+1 #Enregistrement de la date de fin (+1 sur la date pour avoir la VRAIE date de fin, il se termine une unité de temps après)
            processus_termines.append(pe["processus"]) #Le processus est terminé
            enregistrer_date_fin_alloc(infos_allocations_processeur,pe,date+1) #Enregistrement de la date de fin de l'alloc (+1 sur la date pour avoir la VRAIE date de fin)
            ram_liberee += pe["processus"]["usedRam"] #On récupère la ram rendue par le processus fini
            processeurs_dispos.append(pe["processeur"]) #Le processeur utilisé est à nouveau disponible
            processus_elus.remove(pe) #Suppression des processus élus
        else:
            #Si le processus élu à épuisé le quantum de temps
            if pe["processus"]["tempsRestQuantum"] == 0:
                processus_file_attente.append(pe["processus"]) #On renvoie le processus en file d'attente
                # Réinitialiser le quantum pour le prochain passage
                pe["processus"]["tempsRestQuantum"] = quantum
                enregistrer_date_fin_alloc(infos_allocations_processeur,pe,date+1) #Enregistrement de la date de fin de l'alloc (+1 sur la date pour avoir la VRAIE date de fin)
                processeurs_dispos.append(pe["processeur"]) #Le processeur utilisé est à nouveau disponible
                processus_elus.remove(pe) #Suppression du processus de la liste des élus
        
    return ram_liberee    #On return la qte de ram libérée par les processus terminés


def round_robin(params_algo : dict, processus : list[dict], ressources_dispo : dict):
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

        
    Retour :
    -------
    dict
        Dictionnaire contenant les métriques globales de l'ordonnancement 
        Exemple : {"algo": "ROUND ROBIN", "tempsAttenteMoyen": 2.5, "tempsReponseMoyen": 1.5, "makespan": 10}
    """
    
    
    quantum = params_algo["quantum"]
    processeurs_dispos = list(ressources_dispo["processeurs"]) #Copie de la liste pour ma modifier la vraie
    ram_dispo = ressources_dispo["ram_tot"]
    infos_allocations_processeur = [] #Liste permettant de sauvegarder toutes les allocations qui ont été réalisées

    date = 0 #Variable permettant de sauvegarder la date courante de l'ordonnancement

    #Liste des processus en attente de soumission
    processus_attente_soumission = initialiser_processus(processus,quantum)
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
        ram_dispo = allouer_cpu(processus_file_attente,processeurs_dispos,processus_elus,infos_allocations_processeur,date,ram_dispo) #Allocation du cpu au processus, mise a jour de la qte de ram dispo
        ram_dispo += executer_processus_elus(processus_elus,processus_file_attente,processus_termines,processeurs_dispos,infos_allocations_processeur,date,quantum) #Execution du processus, si il se finit, la ram libérée est rendue

        date += 1 #Incrémentation de la date 
        
    #Enregistrer les résultats de l'ordonnancement dans les deux fichiers de résultats       
    ManipulationFichiers.Writing.writing.enregistrer_resultats("ROUND ROBIN",processus_termines,infos_allocations_processeur, params_algo)
    

    tempsAttenteMoyen = metriques.tempsAttenteMoyen(processus_termines)
    tempsReponseMoyen = metriques.tempsReponseMoyen(processus_termines)
    
    return {"algo": "ROUND ROBIN", "tempsAttenteMoyen": tempsAttenteMoyen, "tempsReponseMoyen":tempsReponseMoyen, "makespan": date} #On retourne les métriques
