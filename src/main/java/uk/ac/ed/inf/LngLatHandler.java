package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

public class LngLatHandler implements LngLatHandling {

    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        double distance;
        distance = ((startPosition.lng() - endPosition.lng()) * (startPosition.lng() - endPosition.lng())) + ((startPosition.lat() - endPosition.lat()) * (startPosition.lat() - endPosition.lat()));
        distance = Math.sqrt(distance);

        return distance;
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        double distance1 = distanceTo(startPosition, otherPosition);
        if (distance1 >= 0.00015) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {


//

    return pointInPolygon(region.vertices(), position);

    }

    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {
        double nowlng = startPosition.lng();
        double nowlat = startPosition.lat();

        nowlng = nowlng + 0.00015 * Math.cos(angle);
        nowlat = nowlat + 0.00015 * Math.sin(angle);

        LngLat nextposition;

        nextposition = new LngLat(nowlng, nowlat);


        return nextposition;
    }


    static final double eps = 1e-20;

    static int dcmp(double a, double b) {
        if (Math.abs(a - b) < eps) {
            return 0;
        }
        return a - b > 0 ? 1 : -1;
    }

//Checks if a point is inside a polygon using the Ray-Casting algorithm.
    //The method iterates through each edge of the polygon and checks whether the ray extending from the point p intersects with that edge.
    //The flag variable keeps track of the number of intersections, and if it's an odd number, the point is inside the polygon.
static boolean pointInPolygon(LngLat[] noflyarea, LngLat position) {
    int count = 0;
    int size = noflyarea.length;
    double distance = 1e-10;

    for (int i = 0; i < size; i++) {
        LngLat p1 = noflyarea[i];
        LngLat p2 = noflyarea[(i + 1) % size];
        if (Double.compare(p1.lat(), p2.lat()) == 0) {
            continue;
        }
        if (position.lat() < Math.min(p1.lat(), p2.lat()) - distance || position.lat() >= Math.max(p1.lat(), p2.lat()) + distance) {
            continue;
        }
        double x = (position.lat() - p1.lat()) * (p2.lng() - p1.lng()) / (p2.lat() - p1.lat()) + p1.lng();
        if (Double.compare(Math.abs(x - position.lng()), distance) < 0 &&
                Double.compare(position.lng(), Math.min(p1.lng(), p2.lng()) - distance) >= 0 &&
                Double.compare(position.lng(), Math.max(p1.lng(), p2.lng()) + distance) < 0) {
            return true;
        }
        if (Double.compare(x, position.lng() + distance) > 0) {
            count++;
        }
    }
    return count % 2 == 1;
}


}