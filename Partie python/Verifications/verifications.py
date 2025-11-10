
import sys

def verifierRessources(ressources : dict):
    """
    Vérifie la cohérence des ressources pour l’ordonnancement.

    Paramètres
    ----------
    ressources : dict
        Dictionnaire contenant :
        - 'processeurs' : liste des IDs des processeurs,
        - 'nb_processeurs' : nombre total de processeurs,
        - 'ram_tot' : quantité totale de RAM (>0).

    Termine le programme avec une erreur affichée si incohérence détectée.

    Exemple pramètre "ressources" :
    {
        'processeurs': ['CPU1', 'CPU2'],
        'nb_processeurs': 2,
        'ram_tot': 8000
    }
    """
    try:

        liste_processeurs = ressources["processeurs"]
        nb_processeurs = ressources["nb_processeurs"]
        ram_tot = ressources["ram_tot"]
        #Gestion incohérences
        if len(liste_processeurs) != nb_processeurs: #Si le nb de processeurs est incohérent
            print("Erreur dans le fichier des ressources, il y a un nombre total de CPU différent du nombre réel", file=sys.stderr)
            sys.exit(7)
        if len(liste_processeurs) == 0:
            print("Erreur dans le fichier des ressources, il n'y a aucun CPU spécifié", file=sys.stderr)
            sys.exit(8)
        
        #Gestion de l'unicité de chaque id CPU
        id_vus = set()
        for p in liste_processeurs:
            if p not in id_vus:
                id_vus.add(p)
            else:
                print(f"Erreur dans le fichier des ressources : doublon d'idProcesseur détecté ({p})", file=sys.stderr)
                sys.exit(8)
        #Gestion quantité de ram minimale
        if ram_tot<= 0:
            print("Erreur dans le fichier des ressources, la quantité de ram est <=0", file=sys.stderr)
            sys.exit(8)   
    except Exception:
        print("Erreur innatendue dans le fichier des ressources", file=sys.stderr)
        sys.exit(13)
    
    



def verifier_entiers_positifs(p: dict):
    """
    Vérifie que certaines valeurs numériques d’un processus sont des entiers positifs.

    Paramètres
    ----------
    p : dict
        Exemple :
        {
            'idProcessus': '1',
            'dateSoumission': '0',
            'tempsExecution': '6',
            'requiredRam': '1024',
            'deadline': '20',
            'priority': '3'
        }
        Les champs numériques vérifiés sont :
        dateSoumission, tempsExecution, requiredRam, deadline, priority

    Termine le programme avec une erreur si une valeur négative est détectée.
    """
    
    
    champs_numeriques = ["dateSoumission", "tempsExecution", "requiredRam", "deadline", "priority"]
    
    for champ in champs_numeriques:
        valeur = int(p[champ])
        if valeur < 0:
            print(f"Erreur : {champ} du processus {p['idProcessus']} est négatif ({valeur})", file=sys.stderr)
            sys.exit(9)
        

def verifierProcessus(processus: list[dict], ram_dispo: int):
    """
    Vérifie la cohérence des processus avant l’ordonnancement.

    Paramètres
    ----------
    processus : list[dict]
        Exemple :
        [
            {'idProcessus': '1', 'dateSoumission': '0', 'tempsExecution': '6', 'requiredRam': '1024', 'deadline': '20', 'priority': '3'},
            {'idProcessus': '2', 'dateSoumission': '0', 'tempsExecution': '3', 'requiredRam': '1024', 'deadline': '15', 'priority': '1'},
            {'idProcessus': '3', 'dateSoumission': '3', 'tempsExecution': '6', 'requiredRam': '512', 'deadline': '15', 'priority': '1'}
        ]
    ram_dispo : int
        Quantité totale de RAM disponible pour l’ordonnancement (exemple : 8000).

    Vérifie pour chaque processus :
    - unicité de l’ID,
    - valeurs numériques positives (via `verifier_entiers_positifs`),
    - RAM demandée <= RAM disponible et > 0,
    - deadline >= date de soumission.

    Termine avec une erreur si une incohérence est détectée
    """
    
    try:
        ids_vus = set() #Création d'un ensemble pour stocker les id (on vérif si ils sont uniques)
        for p in processus:
            if p["idProcessus"] in ids_vus: #Test de l'unicité de l'id du processus
                print(f"Erreur : doublon d'idProcessus détecté ({p['idProcessus']})", file=sys.stderr)
                sys.exit(9)
            else:   
                ids_vus.add(p["idProcessus"])
                verifier_entiers_positifs(p)
                
                if int(p["requiredRam"]) > ram_dispo: #Conversions en int car le données récupérées dans les CSV sont str
                    print(f"Impossible d'exécuter le processus {p['idProcessus']} : RAM demandée {p['requiredRam']} > RAM disponible {ram_dispo}", file=sys.stderr)
                    sys.exit(9)
                
                if int(p["requiredRam"]) == 0: 
                    print(f"Impossible d'exécuter le processus {p['idProcessus']} : RAM demandée ({p['requiredRam']}) insuffisante", file=sys.stderr)
                    sys.exit(9)
                
                if int(p["deadline"]) < int(p["dateSoumission"]):
                    print(f"Deadline avant la date de soumission pour le processus {p['idProcessus']}", file=sys.stderr)
                    sys.exit(9)
                
                if int(p["tempsExecution"]) == 0:
                    print(f"Temps d'execution de 0 pour le processus {p['idProcessus']}", file=sys.stderr)
                    sys.exit(9)

    #Si des clés manquent dans ce dico, alors le fichier des processus était mal formé
    except KeyError as e:
        print(f"Le fichier des processus initiaux est mal formé (clé manquante : {e}).", file=sys.stderr)
        sys.exit(13)

    except Exception :
        print("Erreur innatendue dans le fichier des processus", file=sys.stderr)
        sys.exit(13)


def verifierAlgos(algos : dict):
    """
    Vérifie la cohérence des algorithmes à exécuter pour l’ordonnancement.

    Paramètres
    ----------
    algos : dict
        Exemple :
        {
            'ROUND ROBIN': {
                'fichierResultatsDetailles': Path('Resultats/RoundRobin/rDetailedROUNDROBIN.csv'),
                'fichierResultatsGlobaux': Path('Resultats/RoundRobin/rGlobauxROUNDROBIN.csv'),
                'quantum': 2
            },
            'FIFO': {
                'fichierResultatsDetailles': Path('Resultats/Fifo/rDetailedFIFO.csv'),
                'fichierResultatsGlobaux': Path('Resultats/Fifo/rGlobauxFIFO.csv'),
                'quantum': None
            },
            'PRIORITE': {
                'fichierResultatsDetailles': Path('Resultats/Priorite/rDetailedPriority.csv'),
                'fichierResultatsGlobaux': Path('Resultats/Priorite/rGlobauxPriority.csv'),
                'quantum': None
            }
        }

    Vérifie :
    - Que la liste des algorithmes n’est pas vide.
    - Pour ROUND ROBIN, que le quantum est défini et > 0.

    Termine le programme avec une erreur en cas d'incohérence 
    """
    if len(algos) == 0:
        print("Erreur dans le fichier de configuration, liste des algorithmes à exécuter manquante", file=sys.stderr)
        sys.exit(13)

    for a in algos:
        try:
            if a == "ROUND ROBIN" and algos[a]["quantum"] is None:
                print("Erreur dans le fichier de configuration, quantum manquant pour le Round Robin", file=sys.stderr)
                sys.exit(13)

            if a == "ROUND ROBIN" and algos[a]["quantum"] <=0:
                print("Erreur dans le fichier de configuration, quantum <=0 pour le Round Robin", file=sys.stderr)
                sys.exit(13)
        except Exception:
            print(f"Erreur innatendue dans le fichier de config pour l'algo à exécuter {a}", file=sys.stderr)
            sys.exit(13)