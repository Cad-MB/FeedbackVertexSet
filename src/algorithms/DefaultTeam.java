package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class DefaultTeam {

  public Evaluation eval = new Evaluation();

  public ArrayList<Point> calculFVS(ArrayList<Point> points, int edgeThreshold) {
    // Étape 1 : Approche gloutonne améliorée
    ArrayList<Point> fvs = greedyApproximation(points, edgeThreshold);

    // Étape 2 : Optimisation locale
    fvs = localOptimization(points, fvs, edgeThreshold);

    return fvs;
  }

  private ArrayList<Point> greedyApproximation(ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> fvs = new ArrayList<>();
    ArrayList<Point> remainingPoints = new ArrayList<>(points);

    // Suppression initiale des sommets à faible degré
    remainingPoints.removeIf(p -> eval.neighbor(p, points, edgeThreshold).size() < 2);

    // Sélection des sommets à fort impact pour casser les cycles
    while (!eval.isValid(points, fvs, edgeThreshold)) {
      Point bestChoice = selectBestVertex(remainingPoints, fvs, edgeThreshold);
      if (bestChoice != null) {
        fvs.add(bestChoice);
        remainingPoints.remove(bestChoice);
      }
    }
    return fvs;
  }

  private Point selectBestVertex(ArrayList<Point> remainingPoints, ArrayList<Point> fvs, int edgeThreshold) {
    Point bestVertex = null;
    double maxImpact = -1;

    for (Point p : remainingPoints) {
      ArrayList<Point> neighbors = eval.neighbor(p, remainingPoints, edgeThreshold);
      double impact = calculateImpact(p, neighbors, edgeThreshold);

      if (impact > maxImpact) {
        maxImpact = impact;
        bestVertex = p;
      }
    }

    return bestVertex;
  }

  private double calculateImpact(Point p, ArrayList<Point> neighbors, int edgeThreshold) {
    // Impact basé sur le nombre de voisins et leurs degrés
    double impact = neighbors.size();
    for (Point neighbor : neighbors) {
      impact += eval.neighbor(neighbor, neighbors, edgeThreshold).size();
    }
    return impact;
  }

  private ArrayList<Point> localOptimization(ArrayList<Point> points, ArrayList<Point> fvs, int edgeThreshold) {
    ArrayList<Point> currentFVS = new ArrayList<>(fvs);
    boolean improvement = true;

    while (improvement) {
      improvement = false;

      // Étape 1 : Remplacement "deux-pour-un"
      ArrayList<Point> refined = replaceTwoWithOne(points, currentFVS, edgeThreshold);
      if (refined.size() < currentFVS.size()) {
        currentFVS = refined;
        improvement = true;
      }

      // Étape 2 : Raffinement glouton
      ArrayList<Point> greedyRefined = refineByGreedy(points, currentFVS, edgeThreshold);
      if (greedyRefined.size() < currentFVS.size()) {
        currentFVS = greedyRefined;
        improvement = true;
      }

      // Étape 3 : Simulated Annealing
      ArrayList<Point> annealed = simulatedAnnealing(points, currentFVS, edgeThreshold);
      if (annealed.size() < currentFVS.size()) {
        currentFVS = annealed;
        improvement = true;
      }
    }

    return currentFVS;
  }

  private ArrayList<Point> replaceTwoWithOne(ArrayList<Point> points, ArrayList<Point> fvs, int edgeThreshold) {
    ArrayList<Point> bestFVS = new ArrayList<>(fvs);
    ArrayList<Point> remaining = new ArrayList<>(points);
    remaining.removeAll(fvs);

    for (int i = 0; i < fvs.size(); i++) {
      for (int j = i + 1; j < fvs.size(); j++) {
        Point p1 = fvs.get(i);
        Point p2 = fvs.get(j);

        ArrayList<Point> testFVS = new ArrayList<>(fvs);
        testFVS.remove(p1);
        testFVS.remove(p2);

        for (Point candidate : remaining) {
          testFVS.add(candidate);
          if (eval.isValid(points, testFVS, edgeThreshold) && testFVS.size() < bestFVS.size()) {
            bestFVS = new ArrayList<>(testFVS);
          }
          testFVS.remove(candidate);
        }
      }
    }
    return bestFVS;
  }

  private ArrayList<Point> refineByGreedy(ArrayList<Point> points, ArrayList<Point> fvs, int edgeThreshold) {
    ArrayList<Point> refinedFVS = new ArrayList<>(fvs);
    for (int i = 0; i < refinedFVS.size(); i++) {
      Point removed = refinedFVS.remove(i);
      if (!eval.isValid(points, refinedFVS, edgeThreshold)) {
        refinedFVS.add(i, removed);
      }
    }
    return refinedFVS;
  }

  private ArrayList<Point> simulatedAnnealing(ArrayList<Point> points, ArrayList<Point> fvs, int edgeThreshold) {
    ArrayList<Point> bestFVS = new ArrayList<>(fvs);
    ArrayList<Point> currentFVS = new ArrayList<>(fvs);

    double temperature = 200.0;  // Température initiale réduite
    double coolingRate = 0.99;  // Accélération du refroidissement
    int maxIterations = 300;    // Limitation stricte des itérations

    Random random = new Random();

    while (temperature > 1 && maxIterations > 0) {
      ArrayList<Point> neighborFVS = new ArrayList<>(currentFVS);

      // Modification aléatoire contrôlée
      if (random.nextBoolean() && !neighborFVS.isEmpty()) {
        neighborFVS.remove(random.nextInt(neighborFVS.size())); // Retirer un sommet
      } else {
        ArrayList<Point> remaining = new ArrayList<>(points);
        remaining.removeAll(neighborFVS);
        if (!remaining.isEmpty()) {
          neighborFVS.add(remaining.get(random.nextInt(remaining.size()))); // Ajouter un sommet
        }
      }

      // Vérifier la validité de la solution voisine
      if (eval.isValid(points, neighborFVS, edgeThreshold)) {
        // Accepter la solution voisine si elle est meilleure ou par probabilité
        if (neighborFVS.size() < currentFVS.size() ||
                Math.exp((currentFVS.size() - neighborFVS.size()) / temperature) > random.nextDouble()) {
          currentFVS = neighborFVS;
        }

        // Mettre à jour la meilleure solution
        if (currentFVS.size() < bestFVS.size()) {
          bestFVS = new ArrayList<>(currentFVS);
        }
      }

      // Réduire la température
      temperature *= coolingRate;
      maxIterations--;
    }

    return bestFVS;
  }
}
