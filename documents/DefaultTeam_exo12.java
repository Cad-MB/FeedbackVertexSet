package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DefaultTeam {

  public ArrayList<Point> calculFVS(ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> result = (ArrayList<Point>)points.clone();

    for (int i=0;i<3;i++){
      ArrayList<Point> fvs = localSearch(greedy(points,edgeThreshold),points,edgeThreshold);

      System.out.println("MAIN. Current sol: " + result.size() + ". Found next sol: "+fvs.size());

      if (fvs.size()<result.size()) result = fvs;
    }
    
    return result;
    //return greedy(points,edgeThreshold);
  }

  private ArrayList<Point> greedy(ArrayList<Point> pointsIn, int edgeThreshold) {
    ArrayList<Point> points = (ArrayList<Point>)pointsIn.clone();
    ArrayList<Point> result = (ArrayList<Point>)pointsIn.clone();
    
    
    for (int i=0;i<100;i++) {
      Collections.shuffle(points, new Random(System.nanoTime()));
      ArrayList<Point> rest = removeDuplicates(points);
      ArrayList<Point> fvs = new ArrayList<Point>();

      while (!isSolution(fvs,points,edgeThreshold)) {
        Point choosenOne=rest.get(0);
        for (Point p: rest) if (degree(p,rest,edgeThreshold)>degree(choosenOne,rest,edgeThreshold)) choosenOne=p;
        fvs.add(choosenOne);
        rest.removeAll(fvs);
      }
      
//      System.out.println("GR. Current sol: " + result.size() + ". Found next sol: "+fvs.size());

      if (fvs.size()<result.size()) result = fvs;

    }

    return result;
  }
  private ArrayList<Point> localSearch(ArrayList<Point> firstSolution, ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> current = removeDuplicates(firstSolution);
    ArrayList<Point> next = (ArrayList<Point>)current.clone();

    System.out.println("LS. First sol: " + current.size());

    do {
      current = next;
      next = remove2add1(current, points,edgeThreshold);
      System.out.println("LS. Current sol: " + current.size() + ". Found next sol: "+next.size());
    } while (score(current)>score(next));
    
    System.out.println("LS. Last sol: " + current.size());
    return next;

//  return current;
  }
  private ArrayList<Point> remove2add1(ArrayList<Point> candidate, ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> test = removeDuplicates(candidate);
    long seed = System.nanoTime();
    Collections.shuffle(test, new Random(seed));
    ArrayList<Point> rest = removeDuplicates(points);
    rest.removeAll(test);

    for (int i=0;i<test.size();i++) {
      for (int j=i+1;j<test.size();j++) {
        Point q = test.remove(j);
        Point p = test.remove(i);
        
        for (Point r: rest) {
          test.add(r);
          if (isSolution(test,points,edgeThreshold)) return test;
          test.remove(r);
        }

        test.add(i,p);
        test.add(j,q);
      }
    }

    return candidate;
  }
  private boolean isSolution(ArrayList<Point> candidateIn, ArrayList<Point> pointsIn, int edgeThreshold) {
    ArrayList<Point> candidate = removeDuplicates(candidateIn);
    ArrayList<Point> rest = removeDuplicates(pointsIn);
    rest.removeAll(candidate);
    ArrayList<Point> visited = new ArrayList<Point>();

    while (!rest.isEmpty()) {
      visited.clear();
      visited.add(rest.remove(0));
      for (int i=0;i<visited.size();i++) {
        for (Point p: rest) if (isEdge(visited.get(i),p,edgeThreshold)) {
          for (Point q: visited) if (!q.equals(visited.get(i)) && isEdge(p,q,edgeThreshold)) return false;
          visited.add(p);
        }
        rest.removeAll(visited);
      }
    }

    return true;
  }
  private ArrayList<Point> removeDuplicates(ArrayList<Point> points) {
    ArrayList<Point> result = (ArrayList<Point>)points.clone();
    for (int i=0;i<result.size();i++) {
      for (int j=i+1;j<result.size();j++) if (result.get(i).equals(result.get(j))) {
        result.remove(j);
        j--;
      }
    }
    return result;
  }
  private boolean isEdge(Point p, Point q, int edgeThreshold) {
    return p.distance(q)<edgeThreshold;
  }
  private int degree(Point p, ArrayList<Point> points, int edgeThreshold) {
    int degree=-1;
    for (Point q: points) if (isEdge(p,q,edgeThreshold)) degree++;
    return degree;
  }
  private int score(ArrayList<Point> candidate) {
    return candidate.size();
  }
}
