package uk.ac.ed.inf;

import org.testng.annotations.Test;
import static org.junit.Assert.*;
import uk.ac.ed.inf.*;
import uk.ac.ed.inf.ilp.data.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PressureTest {

    /**
     * Test: Pathfinding with start point inside a no-fly zone.
     * Purpose: Ensure the system correctly handles scenarios where the start point is invalid due to no-fly zone constraints.
     */
    @Test
    public void testStartInNoFlyZone() {
        // Define a start point inside a no-fly zone
        LngLat start = new LngLat(-3.187692, 55.945206); // Inside Bayes Central Area
        LngLat goal = new LngLat(-3.185692, 55.944406); // Valid goal outside no-fly zones

        // Load no-fly zones from the JSON file
        NamedRegion[] noFlyZones = App.loadNoFlyZones("noflyzones.json");

        // Attempt to find a path
        ArrayList<point> path = Astar.findpath(start, goal, noFlyZones, false);

        // Validate that the system correctly handles this case
        assertNull("Pathfinding should fail when the start point is inside a no-fly zone", path);
    }

    @Test
    public void testPathWithMultipleNoFlyZones() throws IOException {
    // Define start and goal points
        LngLat start = new LngLat(-3.190000, 55.944000); // A point outside no-fly zones
        LngLat goal = new LngLat(-3.184000, 55.945500); // A point outside no-fly zones but requires navigating through multiple zones

    // Load no-fly zones from the JSON file
        NamedRegion[] noFlyZones = App.loadNoFlyZones("noflyzones.json");

    // Attempt to find a path that navigates through multiple no-fly zones
        ArrayList<point> path = Astar.findpath(start, goal, noFlyZones, false);

    // Validate the generated path
        assertNotNull("Path should not be null for a valid start and goal outside no-fly zones", path);
        assertTrue("Path should contain multiple points", path.size() > 1);

    // Validate that the path avoids all no-fly zones
        for (NamedRegion noFlyZone : noFlyZones) {
            for (point p : path) {
                assertFalse("Path point should not be inside any no-fly zone",
                    isPointInPolygon(p.getLngLat(), noFlyZone.getVertices()));
            }
        }
    }

/**
 * Helper function to determine if a point is inside a polygon
 */
    private boolean isPointInPolygon(LngLat point, LngLat[] polygon) {
        int intersectCount = 0;
        for (int i = 0; i < polygon.length - 1; i++) {
            if (isIntersecting(point, polygon[i], polygon[i + 1])) {
                intersectCount++;
         }
        }
        return (intersectCount % 2 == 1); // Odd number of intersections means inside
    }

/**
 * Helper function to check if a line segment intersects a ray from the point
 */
    private boolean isIntersecting(LngLat point, LngLat vertex1, LngLat vertex2) {
        // Ray-casting algorithm to detect intersection
        if (vertex1.getLat() > vertex2.getLat()) {
            LngLat temp = vertex1;
            vertex1 = vertex2;
            vertex2 = temp;
        }
        if (point.getLat() == vertex1.getLat() || point.getLat() == vertex2.getLat()) {
            point = new LngLat(point.getLng(), point.getLat() + 0.00000001);
        }
        if (point.getLat() > vertex2.getLat() || point.getLat() < vertex1.getLat() || point.getLng() > Math.max(vertex1.getLng(), vertex2.getLng())) {
            return false;
        }
        if (point.getLng() < Math.min(vertex1.getLng(), vertex2.getLng())) {
            return true;
        }
        double slope = (vertex2.getLng() - vertex1.getLng()) / (vertex2.getLat() - vertex1.getLat());
        double intersectLng = vertex1.getLng() + (point.getLat() - vertex1.getLat()) * slope;
        return point.getLng() < intersectLng;
    }

}



