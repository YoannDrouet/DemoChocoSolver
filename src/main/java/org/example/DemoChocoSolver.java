package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class DemoChocoSolver {

    public static void main(String[] args) {
        // Créer le modèle
        Model model = new Model("Répartition des développeurs sur les projets");

        // Données
        int nbDevelopers = 4;
        int nbProjects = 5;
        int maxProjectsPerDevelopers = 2;

        // Disponibilités des développeurs (en mois)
        int[] responsibilityDevelopers = {8, 7, 6, 9};
        IntVar[] responsibilityUnderdeveloped = new IntVar[nbDevelopers];
        for (int i = 0; i < nbDevelopers; i++) {
            responsibilityUnderdeveloped[i] = model.intVar("Disponibilite_Dev_" + i, responsibilityDevelopers[i]);
        }

        // Durées de développement des projets (en mois)
        int[] durationProjects = {4, 6, 8, 5, 7};
        IntVar[] durationProjectsVar = new IntVar[nbProjects];
        for (int j = 0; j < nbProjects; j++) {
            durationProjectsVar[j] = model.intVar("Duree_Projet_" + j, durationProjects[j]);
        }

        // Deadlines des projets (en mois)
        int[] deadLinesProjects = {8, 9, 12, 10, 11};
        IntVar[] deadLinesProjectsVar = new IntVar[nbProjects];
        for (int j = 0; j < nbProjects; j++) {
            deadLinesProjectsVar[j] = model.intVar("Deadline_Projet_" + j, deadLinesProjects[j]);
        }

        // Variables
        IntVar[][] assignementDevelopersProjects = model.intVarMatrix("assignementDevelopersProjects", nbDevelopers, nbProjects, 0, 1);

        // Contraintes
        // Chaque développeur est affecté à au plus maxProjectsPerDevelopers projets à la fois
        for (int i = 0; i < nbDevelopers; i++) {
            model.sum(assignementDevelopersProjects[i], "<=", maxProjectsPerDevelopers).post();
        }

        // Chaque projet est affecté à au moins un développeur
        for (int j = 0; j < nbProjects; j++) {
            IntVar[] colonne = new IntVar[nbDevelopers];
            for (int i = 0; i < nbDevelopers; i++) {
                colonne[i] = assignementDevelopersProjects[i][j];
            }
            model.sum(colonne, ">=", 1).post();
        }

        // Contraintes basées sur les disponibilités et deadlines
        for (int i = 0; i < nbDevelopers; i++) {
            for (int j = 0; j < nbProjects; j++) {
                // Si le développeur i est affecté au projet j
                model.ifThen(model.arithm(assignementDevelopersProjects[i][j], "=", 1), model.and(
                        // Le développeur est disponible pendant la durée du projet
                        model.arithm(responsibilityUnderdeveloped[i], ">=", durationProjectsVar[j]),
                        // La deadline du projet est respectée
                        model.arithm(deadLinesProjectsVar[j], "<=", 12)));
            }
        }

        // Résolution
        Solver solver = model.getSolver();
        solver.solve();

        // Afficher les résultats
        System.out.println("Affectation optimale des développeurs aux projets :");
        for (int i = 0; i < nbDevelopers; i++) {
            System.out.print("Développeur " + i + " travaille sur les projets : ");
            for (int j = 0; j < nbProjects; j++) {
                if (assignementDevelopersProjects[i][j].isInstantiatedTo(1)) {
                    System.out.print(j + " ");
                }
            }
            System.out.println();
        }
    }
}
