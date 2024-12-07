package algorithms;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Solves the Feedback Vertex Set (FVS) problem for a geometric graph.
 */
public class DefaultTeam {

  /**
   * Finds the Feedback Vertex Set (FVS) for a given graph.
   *
   * @param points       List of points representing the graph's vertices.
   * @param edgeThreshold Distance threshold for edges between vertices.
   * @return The FVS as a list of points.
   */
  public ArrayList<Point> calculFVS(ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> fvs = greedyFVS(points, edgeThreshold);
    return localOptimization(fvs, points, edgeThreshold, 1000); // Max iterations, can be increased for better results but compiling time will be longer.
  }

  /**
   * Creates an initial FVS using a greedy algorithm.
   *
   * @param points       List of points representing the graph's vertices.
   * @param edgeThreshold Distance threshold for edges between vertices.
   * @return The initial FVS.
   */
  private ArrayList<Point> greedyFVS(ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> fvs = new ArrayList<>();
    ArrayList<Point> remainingPoints = new ArrayList<>(points);

    while (hasCycles(remainingPoints, edgeThreshold)) {
      Point maxDegreePoint = selectHighestDegreeVertex(remainingPoints, edgeThreshold);
      if (maxDegreePoint != null) {
        fvs.add(maxDegreePoint);
        remainingPoints.remove(maxDegreePoint);
      }
    }

    return fvs;
  }

  /**
   * Refines the FVS by attempting to remove and replace vertices.
   *
   * @param fvs           Initial FVS to refine.
   * @param points        List of all points in the graph.
   * @param edgeThreshold Distance threshold for edges between vertices.
   * @param maxIterations Maximum number of iterations to avoid long runtime.
   * @return The optimized FVS.
   */
  private ArrayList<Point> localOptimization(ArrayList<Point> fvs, ArrayList<Point> points, int edgeThreshold, int maxIterations) {
    ArrayList<Point> optimizedFVS = new ArrayList<>(fvs);
    boolean improvement = true;
    int iteration = 0;

    while (improvement && iteration < maxIterations) {
      improvement = false;
      iteration++;

      for (int i = 0; i < optimizedFVS.size(); i++) {
        Point removedPoint = optimizedFVS.remove(i);
        ArrayList<Point> remainingPoints = new ArrayList<>(points);
        remainingPoints.removeAll(optimizedFVS);

        if (!hasCycles(remainingPoints, edgeThreshold)) {
          improvement = true;
        } else {
          ArrayList<Point> replacements = findReplacement(optimizedFVS, points, removedPoint, edgeThreshold);
          if (replacements.isEmpty()) {
            optimizedFVS.add(i, removedPoint);
          } else {
            optimizedFVS.addAll(replacements);
            improvement = true;
          }
        }

        if (improvement) break;
      }
    }

    return optimizedFVS;
  }

  /**
   * Finds replacement vertices for a removed vertex.
   *
   * @param currentFVS    Current FVS.
   * @param points        List of all points in the graph.
   * @param removedPoint  The vertex removed from the FVS.
   * @param edgeThreshold Distance threshold for edges between vertices.
   * @return A list of replacement vertices.
   */
  private ArrayList<Point> findReplacement(ArrayList<Point> currentFVS, ArrayList<Point> points, Point removedPoint, int edgeThreshold) {
    ArrayList<Point> replacements = new ArrayList<>();
    ArrayList<Point> remainingPoints = new ArrayList<>(points);
    remainingPoints.removeAll(currentFVS);

    for (Point candidate : remainingPoints) {
      ArrayList<Point> testFVS = new ArrayList<>(currentFVS);
      testFVS.add(candidate);

      ArrayList<Point> testRemaining = new ArrayList<>(points);
      testRemaining.removeAll(testFVS);

      if (!hasCycles(testRemaining, edgeThreshold)) {
        replacements.add(candidate);
        break;
      }
    }

    return replacements;
  }

  /**
   * Selects the vertex with the highest degree.
   *
   * @param points       List of all points in the graph.
   * @param edgeThreshold Distance threshold for edges between vertices.
   * @return The vertex with the highest degree.
   */
  private Point selectHighestDegreeVertex(ArrayList<Point> points, int edgeThreshold) {
    Point bestPoint = null;
    int maxDegree = -1;

    for (Point p : points) {
      int degree = calculateDegree(p, points, edgeThreshold);
      if (degree > maxDegree) {
        maxDegree = degree;
        bestPoint = p;
      }
    }

    return bestPoint;
  }

  /**
   * Calculates the degree of a vertex.
   *
   * @param p            The vertex to calculate the degree for.
   * @param points       List of all points in the graph.
   * @param edgeThreshold Distance threshold for edges between vertices.
   * @return The degree of the vertex.
   */
  private int calculateDegree(Point p, ArrayList<Point> points, int edgeThreshold) {
    int degree = 0;
    for (Point other : points) {
      if (!p.equals(other) && p.distance(other) < edgeThreshold) {
        degree++;
      }
    }
    return degree;
  }

  /**
   * Checks if the graph contains cycles.
   *
   * @param points       List of all points in the graph.
   * @param edgeThreshold Distance threshold for edges between vertices.
   * @return True if the graph has cycles, false otherwise.
   */
  private boolean hasCycles(ArrayList<Point> points, int edgeThreshold) {
    UnionFind uf = new UnionFind(points.size());
    for (int i = 0; i < points.size(); i++) {
      for (int j = i + 1; j < points.size(); j++) {
        if (points.get(i).distance(points.get(j)) < edgeThreshold) {
          if (!uf.union(i, j)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Union-Find data structure for detecting cycles.
   */
  private static class UnionFind {
    int[] parent;
    int[] rank;

    UnionFind(int size) {
      parent = new int[size];
      rank = new int[size];
      for (int i = 0; i < size; i++) {
        parent[i] = i;
        rank[i] = 0;
      }
    }

    int find(int x) {
      if (parent[x] != x) {
        parent[x] = find(parent[x]);
      }
      return parent[x];
    }

    boolean union(int x, int y) {
      int rootX = find(x);
      int rootY = find(y);
      if (rootX == rootY) {
        return false;
      }
      if (rank[rootX] > rank[rootY]) {
        parent[rootY] = rootX;
      } else if (rank[rootX] < rank[rootY]) {
        parent[rootX] = rootY;
      } else {
        parent[rootY] = rootX;
        rank[rootX]++;
      }
      return true;
    }
  }
}