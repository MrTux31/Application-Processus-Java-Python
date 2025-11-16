from datetime import date
from pathlib import Path
import sys
from Metriques import metriques
import ManipulationFichiers.Writing.writing


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


def initialiser_processus(processus):
    """
    Initialise la liste des processus pour l'algorithme FIFO.
    Convertit les champs en entiers et trie les processus par date de soumission.
    (Ne lève plus d'erreur si requiredRam > ram_dispo)
    """
    nouvelle_liste = []
    for p in processus:
        required_ram = int(p["requiredRam"])

        nouvelle_liste.append({
            "idProcessus": p["idProcessus"],
            "dateSoumission": int(p["dateSoumission"]),
            "tempsExecution": int(p["tempsExecution"]),
            "requiredRam": required_ram,
            "priority": int(p.get("priority", 0)),
            "tempsTotalExecution": 0,
            "dateDebut": None,
            "dateFin": None,
            "usedRam": None
        })

    for i in range(len(nouvelle_liste) - 1):
        for j in range(i + 1, len(nouvelle_liste)):
            if (nouvelle_liste[i]["dateSoumission"] > nouvelle_liste[j]["dateSoumission"]) or (
                nouvelle_liste[i]["dateSoumission"] == nouvelle_liste[j]["dateSoumission"]
            ):
                tmp = nouvelle_liste[i]
                nouvelle_liste[i] = nouvelle_liste[j]
                nouvelle_liste[j] = tmp
    return nouvelle_liste


def soumettre_processus(date: int, processus_attente_soumission: list, processus_file_attente: list):
    """
    Ajoute les processus dont la date de soumission est atteinte à la file d'attente,
    triés par priorité (1 = plus haute) puis idProcessus.
    """
    for ps in list(processus_attente_soumission):
        if date == ps["dateSoumission"]:
            processus_file_attente.append(ps)
            processus_attente_soumission.remove(ps)

    #Tri des processus en attente par leur date de soumission croissante et leur priorité décroissante (plus elle est élevée plus il est prio)
    processus_file_attente.sort(key=lambda p: (p["dateSoumission"], -p["priority"]))



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


def fifo(params_algo: dict, processus: list[dict], ressources_dispo: dict):
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

    processus_attente_soumission = initialiser_processus(processus)
    processus_file_attente = []
    processus_elus = []
    processus_termines = []

    while processus_attente_soumission or processus_file_attente or processus_elus:
        soumettre_processus(date_actuelle, processus_attente_soumission, processus_file_attente)
        allouer_cpu(processus_file_attente, processeurs_dispos, processus_elus,
                    infos_allocations_processeur, date_actuelle, etat_ram)
        executer_processus_elus(processus_elus, processus_file_attente, processus_termines,
                                processeurs_dispos, infos_allocations_processeur, date_actuelle, etat_ram)
        date_actuelle += 1

    if etat_ram["utilisee"] != 0:
        print(f"Avertissement: RAM utilisée non nulle à la fin ({etat_ram['utilisee']}).", file=sys.stderr)

    #Enregistrer les résultats de l'ordonnancement dans les deux fichiers de résultats       
    ManipulationFichiers.Writing.writing.enregistrer_resultats("FIFO",processus_termines,infos_allocations_processeur, params_algo)
    

    # --- Calcul des métriques à la fin ---
    tempsAttenteMoyen = metriques.tempsAttenteMoyen(processus_termines)
    tempsReponseMoyen = metriques.tempsReponseMoyen(processus_termines)



    return {
        "algo": "FIFO",
        "tempsAttenteMoyen": tempsAttenteMoyen,
        "tempsReponseMoyen": tempsReponseMoyen,
        "makespan": date_actuelle
    }
