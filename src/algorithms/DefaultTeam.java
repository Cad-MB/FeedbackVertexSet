package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class DefaultTeam {

  public Evaluation eval = new Evaluation();

  public ArrayList<Point> calculFVS(ArrayList<Point> points, int edgeThreshold) {
    // étape 1 : approche gloutonne améliorée pour une solution initiale
    ArrayList<Point> fvs = greedyApproximation(points, edgeThreshold);

    // étape 2 : optimisation locale pour raffiner la solution
    fvs = localOptimization(points, fvs, edgeThreshold);

    return fvs;
  }

  // méthode gloutonne pour une solution initiale
  private ArrayList<Point> greedyApproximation(ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> fvs = new ArrayList<>();
    ArrayList<Point> remainingPoints = new ArrayList<>(points);

    // suppression des sommets à faible degré
    remainingPoints.removeIf(p -> eval.neighbor(p, points, edgeThreshold).size() < 2);

    // ajout des sommets les plus connectés pour casser les cycles
    while (!eval.isValid(points, fvs, edgeThreshold)) {
      Point bestChoice = selectBestVertex(remainingPoints, fvs, edgeThreshold);
      if (bestChoice != null) {
        fvs.add(bestChoice);
        remainingPoints.remove(bestChoice);
      }
    }
    return fvs;
  }

  // sélection du sommet avec le plus grand impact sur les cycles
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

  // impact calculé comme le nombre de voisins directs et indirects
  private double calculateImpact(Point p, ArrayList<Point> neighbors, int edgeThreshold) {
    double impact = neighbors.size();
    for (Point neighbor : neighbors) {
      impact += eval.neighbor(neighbor, neighbors, edgeThreshold).size();
    }
    return impact;
  }

  // optimisation locale en affinant la solution initiale
  private ArrayList<Point> localOptimization(ArrayList<Point> points, ArrayList<Point> fvs, int edgeThreshold) {
    ArrayList<Point> currentFVS = new ArrayList<>(fvs);
    boolean improvement = true;

    while (improvement) {
      improvement = false;

      // étape 1 : remplacement de deux sommets par un
      ArrayList<Point> refined = replaceTwoWithOne(points, currentFVS, edgeThreshold);
      if (refined.size() < currentFVS.size()) {
        currentFVS = refined;
        improvement = true;
      }

      // étape 2 : amélioration par raffinement glouton
      ArrayList<Point> greedyRefined = refineByGreedy(points, currentFVS, edgeThreshold);
      if (greedyRefined.size() < currentFVS.size()) {
        currentFVS = greedyRefined;
        improvement = true;
      }

      // étape 3 : recuit simulé pour explorer davantage de solutions
      ArrayList<Point> annealed = simulatedAnnealing(points, currentFVS, edgeThreshold);
      if (annealed.size() < currentFVS.size()) {
        currentFVS = annealed;
        improvement = true;
      }
    }

    return currentFVS;
  }

  // remplacement de deux sommets par un autre
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

  // raffinement glouton pour tester la validité après suppression
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

  // recuit simulé pour explorer des solutions alternatives
  private ArrayList<Point> simulatedAnnealing(ArrayList<Point> points, ArrayList<Point> fvs, int edgeThreshold) {
    ArrayList<Point> bestFVS = new ArrayList<>(fvs);
    ArrayList<Point> currentFVS = new ArrayList<>(fvs);

    double temperature = 200.0; // initiale
    double coolingRate = 0.99;  // taux
    int maxIterations = 300;   // limite d'itérations

    Random random = new Random();

    while (temperature > 1 && maxIterations > 0) {
      ArrayList<Point> neighborFVS = new ArrayList<>(currentFVS);

      // modification aléatoire (ajout ou suppression)
      if (random.nextBoolean() && !neighborFVS.isEmpty()) {
        neighborFVS.remove(random.nextInt(neighborFVS.size()));
      } else {
        ArrayList<Point> remaining = new ArrayList<>(points);
        remaining.removeAll(neighborFVS);
        if (!remaining.isEmpty()) {
          neighborFVS.add(remaining.get(random.nextInt(remaining.size())));
        }
      }

      // mise à jour si la solution est valide et potentiellement meilleure
      if (eval.isValid(points, neighborFVS, edgeThreshold)) {
        if (neighborFVS.size() < currentFVS.size() ||
                Math.exp((currentFVS.size() - neighborFVS.size()) / temperature) > random.nextDouble()) {
          currentFVS = neighborFVS;
        }

        if (currentFVS.size() < bestFVS.size()) {
          bestFVS = new ArrayList<>(currentFVS);
        }
      }

      temperature *= coolingRate;
      maxIterations--;
    }

    return bestFVS;
  }
}