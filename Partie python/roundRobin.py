def round_robin(params_algo : dict, processus : list[dict], ressources_dispo : dict, fichier_metriques : str):

    liste_processeurs = ressources_dispo["processeurs"]
    ram_dispo = ressources_dispo["ram_tot"]


    date = 0 #Variable permettant de sauvegarder la date courante de l'ordonnancement

    #Liste des processus en attente de soumission
    processus_attente_soumission = []
    # File des processus en attente d'executio,
    processus_file_attente = []
    #Liste des processus élus, en cours d'execution sur un cpu
    processus_elus = []
    
    #Ajout des processus dans la liste de processus en attente de soumission
    for p in processus:
        processus_attente_soumission.append({
            "idProcessus": p["idProcessus"],
            "dateSoumission": int(p["dateSoumission"]),
            "tempsExecution": int(p["tempsExecution"]),
            "requiredRam": int(p["requiredRam"]),
            "deadline": int(p["deadline"]),
            "priority": int(p["priority"]),
            "tempsRestant": int(p["tempsExecution"]),   #Le nombre d'unité de temps pendant lequel le processus s'est executé
            "dateDebut": None,                          # date de premier début
            "dateFin": None  
        })
    
    #Tri des processus par date de soumission croissante
    processus_attente_soumission.sort(key=lambda processus:processus["dateSoumission"]) 

    while len(processus_attente_soumission) != 0:
        #Parcours de tous les processus en attente de soumission
        for ps in list(processus_attente_soumission): #Création copie liste pour pouvoir parcourir et suppr des elts de manière sécu
            if date == ps["dateSoumission"]: #Si la date de soumission du processus est la meme que la date actuelle
                processus_file_attente.append(ps) #Le processus est alors soumis, ajout à la vraie file d'attente, 
                processus_attente_soumission.remove(ps)# Suppression du processus de la liste attente soumission

        #Parcours de tous les processus en file attente
        for pfa in processus_file_attente:
            #Si au moins un processeur est dispo
            if len(liste_processeurs) > 0:
                processus_elus.append({"processus" : pfa, "processeur" : liste_processeurs.pop(0)}) #On peut alors élire le processus, sur un cpu (le premier dispo) 
                processus_file_attente.remove(pfa) #On supprime le processus de la fil d'attente

        #Parcours de tous les processus élus
        for pfe in processus_elus:
            #Enregistrement de la date de début d'execution du processus    
        
        
        
        
        date += 1 #Incrémentation de la date        

