from pathlib import Path
import sys
import csv

def enregistrer_resultats(processus, infos_allocations_processeur,params_algos):
    fichier_detaille = Path(params_algos["fichierResultatsDetailles"].strip())
    fichier_global = Path(params_algos["fichierResultatsGlobaux"].strip())
    
    # Vérifie que le fichier existe
    if not fichier_detaille.exists() or not fichier_global.exists():
        #Mettre le message d'erreur dans la sortie d'erreur, pour récup les erreurs avec Java
        print("Chemins de fichiers de résultats incorrects pour le Round Robin",file=sys.stderr)
        sys.exit(12)
    
    try :
        #Enregistrer le fichier d'informations globales des processus---------------------
        with open(fichier_detaille, "w", newline="", encoding="utf-8") as f:
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
        with open(fichier_global, "w", newline="", encoding="utf-8") as f:
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
    # Trouver la dernière allocation correspondant à ce processus sur ce CPU
    for alloc in reversed(infos_allocations_processeur): #Inverser la liste pour retrouver l'allocation plus rapidement
        if alloc["idProcessus"] == pe["processus"]["idProcessus"] and alloc["dateFin"] is None: #on cherche la dernière allocation non terminée du processus
            alloc["dateFin"] = date  # Le processus quitte le CPU à ce moment précis
            break

def round_robin(params_algo : dict, processus : list[dict], ressources_dispo : dict, fichier_metriques : str):
    quantum = params_algo["quantum"]
    processeurs_dispos = ressources_dispo["processeurs"]
    ram_dispo = ressources_dispo["ram_tot"]
    infos_allocations_processeur = [] #Liste permettant de sauvegarder toutes les allocations qui ont été réalisées



    date = 0 #Variable permettant de sauvegarder la date courante de l'ordonnancement

    #Liste des processus en attente de soumission
    processus_attente_soumission = []
    # File des processus en attente d'execution
    processus_file_attente = []
    #Liste des processus élus, en cours d'execution sur un cpu
    processus_elus = []
    #Liste des processus terminées
    processus_termines = []
    
    #Ajout des processus dans la liste de processus en attente de soumission
    for p in processus:
        if int(p["requiredRam"]) > ram_dispo:
            print(f"Impossible d'exécuter le processus {p["idProcessus"]} car il demande trop de ram : {p["requiredRam"]} contre un total de {ram_dispo} disponible", file=sys.stderr)
            sys.exit(9)

        processus_attente_soumission.append({
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
    processus_attente_soumission.sort(key=lambda processus:processus["dateSoumission"]) 

    #Tant qu'il reste des processus à traiter
    while processus_attente_soumission or processus_file_attente or processus_elus:

        #Parcours de tous les processus en attente de soumission
        for ps in list(processus_attente_soumission): #Création copie liste pour pouvoir parcourir et suppr des elts de manière sécu
            if date == ps["dateSoumission"]: #Si la date de soumission du processus est la meme que la date actuelle
                processus_file_attente.append(ps) #Le processus est alors soumis, ajout à la vraie file d'attente, 
                processus_attente_soumission.remove(ps)# Suppression du processus de la liste attente soumission

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
                
                 
                
        
        
        date += 1 #Incrémentation de la date 
    #Enregistrer les résultats de l'ordonnancement dans les deux fichiers de résultats       
    enregistrer_resultats(processus_termines,infos_allocations_processeur, params_algo)


