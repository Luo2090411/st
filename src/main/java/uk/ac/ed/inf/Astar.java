package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.CentralRegionVertexOrder;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.awt.*;
import java.util.*;



//Astar class provides methods for pathfinding using the A* algorithm.
//It includes a method to find a path between two points, considering obstacles and constraints
//The algorithm utilizes priority queues and heuristics for efficient path exploration.
//The class also includes utility methods for heuristic calculations, checking sets, and validating flight paths.

//In order to perform astar efficiently, points were created. Point is based on lnglat and extends the information possessed by the point, such as direction, angle, cost, and expectation.


public class Astar {

    public static ArrayList<point> findpath(LngLat start, LngLat goal,NamedRegion[] noflyzone,boolean checkregion ) {
        // 16 direction
        double[] DIRS = {0,22.5,45,67.5,90,112.5, 135,157.5, 180,202.5, 225,247.5, 270,292.5, 315,337.5};

        //This line of code creates a priority queue openSet, which is used to store point objects
        //
        PriorityQueue<point> openSet = new PriorityQueue<>((Comparator.comparingDouble(point::getF)));
        ArrayList<LngLat> closedSet = new ArrayList<>();         // visited
        ArrayList<point> path =new ArrayList<point>();;

        LngLatHandler areaCheck = new LngLatHandler();//use LngLatHandler
        point[] pathlist = new point[0];

        point startP = new point(start.lng(),start.lat());
        point goalP = new point(goal.lng(),goal.lat());

        //In algorithm A, the heuristic function is a function used to estimate the cost of the shortest path from the current node to the target node.
        startP.h = heuristic(startP,goalP);

        startP.g = 0;
        startP.f = startP.h+startP.g;


        startP.setParent(null);



        // Add start to the queue first
        openSet.add(startP);

        //check if the goal in noflyzone
        for (int i = 0; i < noflyzone.length; i++) {
            if (areaCheck.isInRegion(goal,noflyzone[i])){
                System.out.println("error , goal in noflyzone");
                return null;
            }
        }

        while (!openSet.isEmpty()) {

            // Get the cell with the smallest cost
            point current = openSet.poll();
//
//



            // Mark the cell to be visited
            closedSet.add(current.getLngLat());

            // Find the goal: early exit
            if (areaCheck.isCloseTo(current.getLngLat(),goal)) {

                // add the hover stage first
                point hover = new point(current.row,current.col);
                hover.parent = hover;
                hover.angle = 999;
                path.add(hover);

                /// Reconstruct the path: trace by find the parent cell
                while (current != null) {
                    path.add(current);
                    current = current.parent;

                }
                //reverse path and Remove starting point
                Collections.reverse(path);
                path.remove(0);

                return  path;


            }

            for (int i = 0; i < 16; i++){

                // Neighbour point location
                double newRow = current.row + 0.00015*Math.cos(Math.toRadians(DIRS[i]));
                double newCol = current.col + 0.00015*Math.sin(Math.toRadians(DIRS[i]));




                // create point neighbour and its lnglat
                LngLat checkposition = new LngLat(newRow,newCol);
                point neighbor = new point(newRow, newCol);



                boolean innoflyzone = false;
                boolean incloseset = false;
                boolean inopenset = false;
                boolean illegalflight = false;




                for (int j = 0; j < noflyzone.length; j++) {
                    //check if next point in noflyareas
                    if (areaCheck.isInRegion(checkposition,noflyzone[j])){
                        innoflyzone = true;

                        break;

                    }
                    // check if the path cross no fly ares
                    if (isSegmentIntersectPolygon(noflyzone[j].vertices(),current.getLngLat(),checkposition)){
                        innoflyzone = true;
                        break;
                    }


                }
                //Checks if a point is in the  closset.
                incloseset = isinclosedset(newRow,newCol,closedSet);

                //Checks if a point is in the open set (frontier).
                inopenset = isinopenset(newRow,newCol,openSet);

                //Checks if a point leaves the Central Area again after its parent has entered it
                if (checkregion){
                    illegalflight = checkpath(current,neighbor);
                }else {
                    illegalflight = false;
                }

                //When a point does not violate any of the above requirements, it will be recorded in the openset.
                if ( !innoflyzone && !incloseset && !inopenset && !illegalflight) {

                        //directly add this cell to the frontier
                    double tentativeG = current.g + 0.00013;

                        neighbor.setParent(current);
                        neighbor.angle = DIRS[i];
                        neighbor.g = tentativeG;
                        neighbor.h = heuristic(neighbor, goalP);
                        neighbor.f = neighbor.g + neighbor.h;

                        openSet.add(neighbor);

                    }
                }
            }


        System.out.println("NULL");
        return null;

    }

    //find  if neighbour point already in closeset
//
    public static boolean isinclosedset(double row, double col, ArrayList<LngLat> A){
        LngLat b = new LngLat(row,col);
        if(A.isEmpty()){
            return false;
        }

        if (A.contains(b)) {
            return true;
        }
        return false;

    }

    public static double heuristic(point a, point b) {
        // A simple heuristic: use distancto()
        LngLatHandler areaCheck = new LngLatHandler();
        return areaCheck.distanceTo(a.getLngLat(),b.getLngLat());
//
    }
//Checks if a point with the specified row and column coordinates is present in the given PriorityQueue
    public static boolean isinopenset(double row, double col,  PriorityQueue<point> A){
        point b = new point(row,col);
        if(A.isEmpty()){
            return false;
        }

        if (A.contains(b)) {
            return true;
        }
        return false;

    }
//Check whether the path between two points involves moving from a central area to a non-central area
    public static boolean checkpath (point parrent,point current){
        // Check if the parent point is in the central area
        if (incentralarea(parrent)){
            // If the current point is not in the central area, return true (path involves leaving central area)
            if (!incentralarea(current)){
                return true;
            }else{
                // If the current point is also in the central area, return false (path does not leave central area)
            return false;

            }
        }else {
            // If the parent point is not in the central area, return false (path does not leave central area)
            return false;
        }
    }
    // Check if the point is within the specified latitude and longitude bounds of the central area
    public static boolean incentralarea(point checkpoint){
        if (checkpoint.row < -3.184319 && checkpoint.row > -3.192473&&checkpoint.col> 55.942617 && checkpoint.col < 55.946233) {
            return true;
        }else {
            return false;
        }
    }
    //Check whether the line segment intersects the no-fly zone, which is the previous ray method
    public static boolean isSegmentIntersectPolygon(LngLat[] polygon, LngLat start, LngLat end) {
        int count = 0;
        int size = polygon.length;
        double distance = 1e-10;

        for (int i = 0; i < size; i++) {
            LngLat p1 = polygon[i];
            LngLat p2 = polygon[(i + 1) % size];

            if (doSegmentsIntersect(start, end, p1, p2, distance)) {
                return true;
            }
        }
        return false;
    }


    public static boolean doSegmentsIntersect(LngLat start1, LngLat end1, LngLat start2, LngLat end2, double epsilon) {
        double x1 = start1.lng(), y1 = start1.lat();
        double x2 = end1.lng(), y2 = end1.lat();
        double x3 = start2.lng(), y3 = start2.lat();
        double x4 = end2.lng(), y4 = end2.lat();

        double den = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);

        if (Math.abs(den) < epsilon) {
            return false; // Parallel or coincident lines
        }

        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / den;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / den;

        if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
            return true; // Intersection
        }

        return false;
    }

}
