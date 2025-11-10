from datetime import date
from pathlib import Path
import sys
import csv
from Metriques import metriques


def enregistrer_resultats_fifo(processus, infos_allocations_processeur, params_algos):
    """
    Écrit deux CSV : global (par processus) et détaillé (allocations CPU).
    Crée automatiquement les dossiers parents si besoin.
    (Le total de RAM utilisée n'est plus ajouté en bas du fichier global.)
    """
    # Normalisation des chemins
    fichier_detaille = Path(str(params_algos["fichierResultatsDetailles"]).strip())
    fichier_global = Path(str(params_algos["fichierResultatsGlobaux"]).strip())

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
        print(f"Erreur d'enregistrement des fichiers de résultats pour FIFO, permissions manquantes : {e}", file=sys.stderr)
        sys.exit(11)
    except Exception as e:
        print(f"Chemins de fichiers de résultats incorrects pour FIFO : {e}", file=sys.stderr)
        sys.exit(12)

    # Écriture des fichiers
    try:
        # Fichier global (par processus uniquement)
        with open(fichier_global, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(
                f,
                fieldnames=["idProcessus", "dateSoumission", "dateDebut", "dateFin", "requiredRam", "usedRam"]
            )
            writer.writeheader()

            for p in processus:
                writer.writerow({
                    "idProcessus": p.get("idProcessus"),
                    "dateSoumission": p.get("dateSoumission"),
                    "dateDebut": p.get("dateDebut"),
                    "dateFin": p.get("dateFin"),
                    "requiredRam": p.get("requiredRam"),
                    "usedRam": p.get("usedRam") or 0
                })

        # Fichier détaillé (allocations CPU)
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
        print(f"Erreur d'enregistrement des fichiers de résultats pour FIFO, permissions manquantes : {e}", file=sys.stderr)
        sys.exit(11)


def enregistrer_date_fin_alloc(infos_allocations_processeur, pe, date_fin):
    """
    Met à jour la date de fin de la dernière allocation CPU d'un processus donné (FIFO).
    Contrairement à Round Robin, pas de quantum : mise à jour uniquement à la fin.
    """
    for i in range(len(infos_allocations_processeur) - 1, -1, -1):
        alloc = infos_allocations_processeur[i]
        if alloc["idProcessus"] == pe["processus"]["idProcessus"] and alloc["dateFin"] is None:
            alloc["dateFin"] = date_fin
            break


def initialiser_processus(processus, ram_dispo):
    """
    Initialise la liste des processus pour l'algorithme FIFO.
    Convertit les champs en entiers et trie les processus par date de soumission.
    (Ne lève plus d'erreur si requiredRam > ram_dispo)
    """
    nouvelle_liste = []
    for p in processus:
        required_ram = int(p["requiredRam"])
        raw_deadline = p.get("deadline", "")
        deadline = None
        if raw_deadline not in (None, "", "None"):
            deadline = int(raw_deadline)

        nouvelle_liste.append({
            "idProcessus": p["idProcessus"],
            "dateSoumission": int(p["dateSoumission"]),
            "tempsExecution": int(p["tempsExecution"]),
            "requiredRam": required_ram,
            "deadline": deadline,
            "priority": int(p.get("priority", 0)),
            "tempsTotalExecution": 0,
            "dateDebut": None,
            "dateFin": None,
            "usedRam": None
        })

    # Tri manuel sans lambda
    for i in range(len(nouvelle_liste) - 1):
        for j in range(i + 1, len(nouvelle_liste)):
            if (nouvelle_liste[i]["dateSoumission"] > nouvelle_liste[j]["dateSoumission"]) or (
                nouvelle_liste[i]["dateSoumission"] == nouvelle_liste[j]["dateSoumission"]
                and nouvelle_liste[i]["idProcessus"] > nouvelle_liste[j]["idProcessus"]
            ):
                tmp = nouvelle_liste[i]
                nouvelle_liste[i] = nouvelle_liste[j]
                nouvelle_liste[j] = tmp
    return nouvelle_liste


def soumettre_processus(date_actuelle, processus_attente_soumission, processus_file_attente, etat_ram):
    """
    Ajoute à la file d'attente les processus dont la date de soumission == date_actuelle.
    Les deadlines sont stockées mais ne provoquent plus de rejet.
    Aucune vérification de RAM à ce stade.
    """
    arrivants = []
    i = 0
    while i < len(processus_attente_soumission):
        ps = processus_attente_soumission[i]
        if date_actuelle == ps["dateSoumission"]:
            arrivants.append(ps)
            processus_attente_soumission.pop(i)
            continue
        i += 1

    for ps in arrivants:
        processus_file_attente.append(ps)


def allouer_cpu(processus_file_attente, processeurs_dispos, processus_elus,
                infos_allocations_processeur, date_actuelle, etat_ram):
    """
    Alloue les CPU libres aux processus en attente (FIFO) si la RAM disponible suffit.
    Aucun arrêt du programme n'est déclenché : si la RAM restante est insuffisante,
    le processus reste dans la file d'attente.
    """
    i = 0
    while i < len(processus_file_attente) and len(processeurs_dispos) > 0:
        pfa = processus_file_attente[i]
        ram_restante = etat_ram["totale"] - etat_ram["utilisee"]

        if pfa["requiredRam"] <= ram_restante:
            cpu = processeurs_dispos.pop(0)
            pfa["usedRam"] = int(pfa["requiredRam"])
            etat_ram["utilisee"] += pfa["usedRam"]

            processus_elus.append({"processus": pfa, "processeur": cpu})
            processus_file_attente.pop(i)

            infos_allocations_processeur.append({
                "idProcessus": pfa["idProcessus"],
                "dateDebut": date_actuelle,
                "dateFin": None,
                "idProcesseur": cpu
            })
            continue
        break


def executer_processus_elus(processus_elus, processus_file_attente, processus_termines,
                            processeurs_dispos, infos_allocations_processeur, date_actuelle, etat_ram):
    """
    Exécute d'un tick les processus élus (FIFO) et libère RAM/CPU à la fin.
    """
    i = 0
    while i < len(processus_elus):
        pe = processus_elus[i]
        proc = pe["processus"]

        if proc["dateDebut"] is None:
            proc["dateDebut"] = date_actuelle

        proc["tempsTotalExecution"] += 1

        if proc["tempsTotalExecution"] == proc["tempsExecution"]:
            proc["dateFin"] = date_actuelle + 1
            enregistrer_date_fin_alloc(infos_allocations_processeur, pe, date_actuelle + 1)

            if proc.get("usedRam"):
                etat_ram["utilisee"] -= proc["usedRam"]
                if etat_ram["utilisee"] < 0:
                    etat_ram["utilisee"] = 0
            processeurs_dispos.append(pe["processeur"])
            processus_elus.pop(i)
            processus_termines.append(proc)
            continue
        i += 1


def fifo(params_algo: dict, processus: list[dict], ressources_dispo: dict, fichier_metriques: str):
    """
    Exécute l'algorithme FIFO sur un ensemble de processus.
    - Les deadlines ne provoquent plus de rejet.
    - Si la RAM disponible est insuffisante, le processus attend simplement.
    """
    processeurs_dispos = list(ressources_dispo["processeurs"])
    ram_totale = int(ressources_dispo["ram_tot"])
    etat_ram = {"totale": ram_totale, "utilisee": 0}

    infos_allocations_processeur = []
    date_actuelle = 0

    processus_attente_soumission = initialiser_processus(processus, ram_totale)
    processus_file_attente = []
    processus_elus = []
    processus_termines = []

    while processus_attente_soumission or processus_file_attente or processus_elus:
        soumettre_processus(date_actuelle, processus_attente_soumission, processus_file_attente, etat_ram)
        allouer_cpu(processus_file_attente, processeurs_dispos, processus_elus,
                    infos_allocations_processeur, date_actuelle, etat_ram)
        executer_processus_elus(processus_elus, processus_file_attente, processus_termines,
                                processeurs_dispos, infos_allocations_processeur, date_actuelle, etat_ram)
        date_actuelle += 1

    if etat_ram["utilisee"] != 0:
        print(f"Avertissement: RAM utilisée non nulle à la fin ({etat_ram['utilisee']}).", file=sys.stderr)

    enregistrer_resultats_fifo(processus_termines, infos_allocations_processeur, params_algo)

    # --- Calcul des métriques à la fin ---
    tempsAttenteMoyen = metriques.tempsAttenteMoyen(processus_termines)
    tempsReponseMoyen = metriques.tempsReponseMoyen(processus_termines)

    return {
        "algo": "FIFO",
        "tempsAttenteMoyen": tempsAttenteMoyen,
        "tempsReponseMoyen": tempsReponseMoyen,
        "makespan": date_actuelle
    }
