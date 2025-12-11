# Application-Processus-Java-Python

### S3.01 Conception et Développement d’application

Projet de visualisation et de comparaison d’algorithmes d’ordonnancement de processus.  
L’application est composée de deux parties complémentaires :

- **Partie Python** : implémente les algorithmes d’ordonnancement et génère les résultats.  
- **Partie Java** : permet le paramétrage, le lancement du script Python et la visualisation des résultats.

**Equipe**
- [Quentin ROMA](https://github.com/MrTux31)
- [Axel Allemane](https://github.com/Allema31)
- [Nino Ribeiro-Vaur](https://github.com/niriva)
- [Antonin Le Donné](https://github.com/Antonin-Le-Donne)
- [Eliot Olivencia](https://github.com/JLozer-m)


**Enseignants référents :**
Patricia Stolf (Python) et André Péninou (Java)

---

## Objectif

L’objectif du projet est de simuler et comparer plusieurs politiques d’ordonnancement sur un même ensemble de processus, puis de visualiser les résultats sous forme de listes, métriques et diagrammes de Gantt.

---

## Fonctionnalités principales

### Partie Python
- Lecture du fichier de configuration généré par la partie Java.  
- Exécution d’un ou plusieurs algorithmes d’ordonnancement (FIFO, Round Robin, Priorité).  
- Écriture des résultats et des métriques dans des fichiers au format JSON ou CSV.

### Partie Java
- Interface graphique permettant de :
  - Choisir les fichiers d’entrée et de sortie (processus, ressources, résultats, métriques).  
  - Sélectionner les algorithmes à exécuter et leurs paramètres (par exemple le quantum).  
  - Lancer le programme Python.  
  - Afficher les résultats :
    - Liste des processus et de leurs caractéristiques.
    - Dates de soumission, de début et de fin d’exécution.
    - Diagrammes de Gantt (par processus ou par processeur).
    - Graphiques comparatifs des métriques (temps d’attente moyen, temps de réponse moyen, makespan, etc.).

---

## Organisation du travail

Le projet est structuré autour de plusieurs sous-parties :

**Côté Python :**
- Lecture et écriture de fichiers.
- Implémentation des différentes politiques d’ordonnancement.

**Côté Java :**
- Paramétrage et gestion de la configuration.
- Affichages généraux des résultats.
- Génération des diagrammes de Gantt (par processus et par processeur).

---

## Formats de données

Les fichiers utilisés par l’application contiennent :

- **Ressources disponibles** : processeurs, RAM totale.  
- **Processus à exécuter** : date de soumission, durée, RAM nécessaire, priorité, deadline.  
- **Résultats d’ordonnancement** : identifiant du processus, processeur utilisé, dates de début et de fin.  
- **Métriques globales** : temps de réponse moyen, temps d’attente moyen, makespan, etc.

---

## Technologies utilisées

- **Langages** : Java, Python  
- **Formats de données** : JSON, CSV  
- **Outils** : Git, GitHub, IDE (VSCode)

---

## Auteurs

Projet réalisé dans le cadre du module **S3.01 - Conception et Développement d’application**  
par les étudiants du groupe 1B-2 
