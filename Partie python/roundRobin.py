def round_robin(params_algo : dict, processus : list[dict], ressources_dispo : dict, fichier_metriques : str):
    quantum = params_algo["quantum"]
    processeurs_dispos = ressources_dispo["processeurs"]
    ram_dispo = ressources_dispo["ram_tot"]



    date = 0 #Variable permettant de sauvegarder la date courante de l'ordonnancement

    #Liste des processus en attente de soumission
    processus_attente_soumission = []
    # File des processus en attente d'executio,
    processus_file_attente = []
    #Liste des processus élus, en cours d'execution sur un cpu
    processus_elus = []
    #Liste des processus terminées
    processus_termines = []
    
    #Ajout des processus dans la liste de processus en attente de soumission
    for p in processus:
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
            "dateFin": None  
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
                pfa["processus"]["tempsRestQuantum"] = quantum #Rénitialisation du quantum restant pour l'élection de ce processus
                processus_elus.append({"processus" : pfa, "processeur" : processeurs_dispos.pop(0)}) #On peut alors élire le processus, sur un cpu (le premier dispo) 
                processus_file_attente.remove(pfa) #On supprime le processus de la fil d'attente

        #Parcours de tous les processus élus
        for pfe in list(processus_elus):
            #Enregistrement de la date de début d'execution du processus    
            if pfe["processus"]["dateDebut"] is None:#Si la date de début n'a pas encore été initialisée
                pfe["processus"]["dateDebut"] = date 
            
            #On décrémente le quantum restant au processus
            pfe["processus"]["tempsRestQuantum"] -= 1   
            #On incrémente le temps pendant lequel le processus s'est exécuté
            pfe["processus"]["tempsTotalExecution"] +=1

            #Si le processus s'est executé pendant le temps qui était prévu, on le met dans la liste des processus terminés
            if pfe["processus"]["tempsTotalExecution"] == pfe["tempsExecution"]:
                pfe["processus"]["dateFin"] = date #Enregistrement de la date de fin
                processus_termines.append(pfe["processus"]) #Le processus est terminé
                processeurs_dispos.append(pfe["processeur"]) #Le processeur utilisé est à nouveau disponible
                processus_elus.remove(pfe) #Supression des processus élus
                
            else:
                
                #Si le processus élu à épuisé le quantum de temps
                if pfe["processus"]["tempsRestQuantum"] == 0:
                    processus_file_attente.append(pfe) #On renvoie le processus en file d'attente
                    processeurs_dispos.append(pfe["processeur"]) #Le processeur utilisé est à nouveau disponible
                    processus_elus.remove(pfe) #Supression du processus de la liste des élus
                
                 
                
        
        
        date += 1 #Incrémentation de la date        

