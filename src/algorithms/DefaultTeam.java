package algorithms;

import java.awt.Point;
import java.util.ArrayList;

public class DefaultTeam {

  public Evaluation eval = new Evaluation();

  public ArrayList<Point> calculFVS(ArrayList<Point> points, int edgeThreshold) {
    // Step 1: Improved greedy approximation
    ArrayList<Point> fvs = greedyApproximation(points, edgeThreshold);

    // Step 2: Aggressive local optimization
    fvs = localOptimization(points, fvs, edgeThreshold);

    return fvs;
  }

  private ArrayList<Point> greedyApproximation(ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> fvs = new ArrayList<>();
    ArrayList<Point> remainingPoints = new ArrayList<>(points);

    // Initial removal of low-degree vertices
    remainingPoints.removeIf(p -> eval.neighbor(p, points, edgeThreshold).size() < 2);

    // Iteratively select high-impact vertices to break cycles
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
    // Impact is based on the number of neighbors and their degrees
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

      // Try removing each vertex and check if the graph remains valid
      for (int i = 0; i < currentFVS.size(); i++) {
        Point removed = currentFVS.remove(i);

        if (eval.isValid(points, currentFVS, edgeThreshold)) {
          improvement = true;
          break; // Found an improvement
        } else {
          currentFVS.add(i, removed); // Restore the vertex if not valid
        }
      }

      // Attempt two-for-one replacements for further refinement
      ArrayList<Point> refined = replaceTwoWithOne(points, currentFVS, edgeThreshold);
      if (refined.size() < currentFVS.size()) {
        currentFVS = refined;
        improvement = true;
      }
    }

    return currentFVS;
  }

  private ArrayList<Point> replaceTwoWithOne(ArrayList<Point> points, ArrayList<Point> fvs, int edgeThreshold) {
    ArrayList<Point> refinedFVS = new ArrayList<>(fvs);

    for (int i = 0; i < fvs.size(); i++) {
      for (int j = i + 1; j < fvs.size(); j++) {
        Point p1 = fvs.get(i);
        Point p2 = fvs.get(j);

        refinedFVS.remove(p1);
        refinedFVS.remove(p2);

        for (Point candidate : points) {
          if (!fvs.contains(candidate)) {
            refinedFVS.add(candidate);
            if (eval.isValid(points, refinedFVS, edgeThreshold)) {
              return refinedFVS;
            }
            refinedFVS.remove(candidate);
          }
        }

        // Restore removed points if no valid replacement found
        refinedFVS.add(p1);
        refinedFVS.add(p2);
      }
    }
    return refinedFVS;
  }
}
