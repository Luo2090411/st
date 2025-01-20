package uk.ac.ed.inf;

import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ed.inf.*;
import uk.ac.ed.inf.ilp.data.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ed.inf.*;

import java.util.ArrayList;

public class AstarStructuralTest {

    /**
     * Test case 1: Start point equals goal point.
     * Purpose: Verify that the algorithm immediately returns the start point as the path.
     */
    @Test
    public void testStartEqualsGoal() {
        LngLat start = new LngLat(-3.186874, 55.944494);
        LngLat goal = new LngLat(-3.186874, 55.944494); // Same as start
        NamedRegion[] noFlyZones = {}; // No obstacles

        ArrayList<point> path = Astar.findpath(start, goal, noFlyZones, false);

        // Validate the path contains only the start/goal point
        assertNotNull("Path should not be null", path);
        assertEquals("Path should contain exactly one point", 1, path.size());
        assertEquals("Start point should equal goal point", start, path.get(0).getLngLat());
    }

    /**
     * Test case 2: No available path due to complete blockage by no-fly zones.
     * Purpose: Verify that the algorithm returns null or an empty path when no path is possible.
     */
    @Test
    public void testNoPathAvailable() {
        LngLat start = new LngLat(-3.190000, 55.944000);
        LngLat goal = new LngLat(-3.185000, 55.945000);

        // Define a no-fly zone that completely blocks the path
        NamedRegion[] noFlyZones = {
            new NamedRegion("Blocker", new LngLat[]{
                new LngLat(-3.191000, 55.943000),
                new LngLat(-3.191000, 55.946000),
                new LngLat(-3.184000, 55.946000),
                new LngLat(-3.184000, 55.943000),
                new LngLat(-3.191000, 55.943000)
            })
        };

        ArrayList<point> path = Astar.findpath(start, goal, noFlyZones, false);

        // Validate the path is null or empty
        assertTrue("Path should be null or empty when no path is available", path == null || path.isEmpty());
    }

    /**
     * Test case 3: Valid path avoiding no-fly zones.
     * Purpose: Verify the algorithm successfully finds a valid path around obstacles.
     */
    @Test
    public void testValidPathWithObstacles() {
        LngLat start = new LngLat(-3.190000, 55.944000);
        LngLat goal = new LngLat(-3.185000, 55.945000);

        // Define no-fly zones that create a simple obstacle
        NamedRegion[] noFlyZones = {
            new NamedRegion("Obstacle", new LngLat[]{
                new LngLat(-3.188000, 55.944500),
                new LngLat(-3.187000, 55.944500),
                new LngLat(-3.187000, 55.945500),
                new LngLat(-3.188000, 55.945500),
                new LngLat(-3.188000, 55.944500)
            })
        };

        ArrayList<point> path = Astar.findpath(start, goal, noFlyZones, false);

        // Validate the path is not null or empty
        assertNotNull("Path should not be null", path);
        assertFalse("Path should not be empty", path.isEmpty());

        // Validate the path avoids the no-fly zone
        for (point p : path) {
            assertFalse("Path point should not be inside the no-fly zone",
                isPointInPolygon(p.getLngLat(), noFlyZones[0].getVertices()));
        }
    }

    /**
     * Helper function to check if a point is inside a polygon using ray-casting.
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
     * Helper function to check if a line segment intersects a ray from the point.
     */
    private boolean isIntersecting(LngLat point, LngLat vertex1, LngLat vertex2) {
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




