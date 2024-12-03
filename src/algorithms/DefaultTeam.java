package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DefaultTeam {

  public ArrayList<Point> calculFVS(ArrayList<Point> points, int edgeThreshold) {
    ArrayList<Point> fvs = new ArrayList<Point>();

    //REMOVE >>>>>
    for(int i=0;i<5*points.size()/6;i++){
      fvs.add(points.get(i));
    }
    //<<<<< REMOVE

    return fvs;
  }
}
