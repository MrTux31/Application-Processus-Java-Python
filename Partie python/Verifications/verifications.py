
import sys

def verifierRessources(ressources : dict):
    """
    "ressources": {
        "processeurs": liste des IDs,
        "nb_processeurs": int,
        "ram_tot": int
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
    except Exception as e:
        print(f"Erreur innatendue dans le fichier des ressources : {e}", file=sys.stderr)
        sys.exit(13)
    
    



def verifier_entiers_positifs(p: dict):
    champs_numeriques = ["dateSoumission", "tempsExecution", "requiredRam", "deadline", "priority"]
    
    for champ in champs_numeriques:
        valeur = int(p[champ])
        if valeur < 0:
            print(f"Erreur : {champ} du processus {p['idProcessus']} est négatif ({valeur})", file=sys.stderr)
            sys.exit(9)
        

def verifierProcessus(processus: list[dict], ram_dispo: int):
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
                
                if int(p["deadline"]) < int(p["dateSoumission"]):
                    print(f"Deadline avant la date de soumission pour le processus {p['idProcessus']}", file=sys.stderr)
                    sys.exit(9)

    #Si des clés manquent dans ce dico, alors le fichier des processus était mal formé
    except KeyError as e:
        print(f"Le fichier des processus initiaux est mal formé (clé manquante : {e}).", file=sys.stderr)
        sys.exit(13)

    except Exception as e:
        print(f"Erreur innatendue dans le fichier des processus : {e}", file=sys.stderr)
        sys.exit(13)


def verifierAlgos(algos : dict):
    for a in algos:
        try:
            if a == "ROUND ROBIN" and algos[a]["quantum"] is None:
                print("Erreur dans le fichier de configuration, quantum manquant pour le Round Robin", file=sys.stderr)
                sys.exit(13)

            if a == "ROUND ROBIN" and algos[a]["quantum"] <=0:
                print("Erreur dans le fichier de configuration, quantum <=0 pour le Round Robin", file=sys.stderr)
                sys.exit(13)
        except Exception as e:
            print(f"Erreur innatendue dans le fichier de config pour l'algo à exécuter {a} : {e}", file=sys.stderr)
            sys.exit(13)