package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class DefaultTeam {

  public ArrayList<Point> calculFVS(ArrayList<Point> points, int edgeThreshold) {
    // Étape 1 : Identifier les cycles dans le graphe et initialiser les poids
    ArrayList<Point> fvs = approximateFVS(points, edgeThreshold);

    // Étape 2 : Optimisation locale pour affiner la solution
    fvs = refineFVS(points, fvs, edgeThreshold);

    return fvs;
  }

  private ArrayList<Point> approximateFVS(ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> fvs = new ArrayList<>();
    HashSet<Point> remainingPoints = new HashSet<>(points);

    while (hasCycles(new ArrayList<>(remainingPoints), edgeThreshold)) {
      Point toRemove = selectWeightedVertex(remainingPoints, edgeThreshold);
      if (toRemove != null) {
        fvs.add(toRemove);
        remainingPoints.remove(toRemove);
      }
    }
    return fvs;
  }

  private Point selectWeightedVertex(HashSet<Point> points, int edgeThreshold) {
    PriorityQueue<Point> queue = new PriorityQueue<>(
            (p1, p2) -> Double.compare(calculateWeight(p2, points, edgeThreshold),
                    calculateWeight(p1, points, edgeThreshold))
    );
    queue.addAll(points);
    return queue.isEmpty() ? null : queue.poll();
  }

  private double calculateWeight(Point p, HashSet<Point> points, int edgeThreshold) {
    int degree = calculateDegree(p, points, edgeThreshold);
    return degree > 1 ? (double) degree / (degree - 1) : Double.MAX_VALUE;
  }

  private ArrayList<Point> refineFVS(ArrayList<Point> points, ArrayList<Point> fvs, int edgeThreshold) {
    ArrayList<Point> refinedFVS = new ArrayList<>(fvs);
    boolean improvement = true;

    while (improvement) {
      improvement = false;

      for (int i = 0; i < refinedFVS.size(); i++) {
        Point removedPoint = refinedFVS.remove(i);
        ArrayList<Point> remainingPoints = new ArrayList<>(points);
        remainingPoints.removeAll(refinedFVS);

        if (!hasCycles(remainingPoints, edgeThreshold)) {
          improvement = true;
          break;
        }

        refinedFVS.add(i, removedPoint); // Restore point if it doesn't improve the solution
      }
    }

    return refinedFVS;
  }

  private int calculateDegree(Point p, HashSet<Point> points, int edgeThreshold) {
    int degree = 0;
    for (Point other : points) {
      if (!p.equals(other) && p.distance(other) < edgeThreshold) {
        degree++;
      }
    }
    return degree;
  }

  private boolean hasCycles(ArrayList<Point> points, int edgeThreshold) {
    UnionFind uf = new UnionFind(points.size());
    for (int i = 0; i < points.size(); i++) {
      for (int j = i + 1; j < points.size(); j++) {
        if (points.get(i).distance(points.get(j)) < edgeThreshold) {
          if (!uf.union(i, j)) {
            return true; // Cycle détecté
          }
        }
      }
    }
    return false;
  }

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
