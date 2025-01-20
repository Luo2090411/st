package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import junit.framework.Assert;
import junit.framework.TestCase;
import uk.ac.ed.inf.Astar;
import uk.ac.ed.inf.App;
import uk.ac.ed.inf.LngLatHandler;
import uk.ac.ed.inf.LngLatHandler;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.order;
import uk.ac.ed.inf.point;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.constant.OrderStatus;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FunctionalTest extends TestCase {



    public void testSystemLevel() throws IOException {
        String[] testDates = {"2023-11-20", "2023-10-15", "2023-11-25", "2024-01-10", "2024-01-15"};
        String baseUrl = "https://ilp-rest-2024.azurewebsites.net";

        for (String date : testDates) {
            String[] args = {date, baseUrl};
            App.main(args);

            File flightPathFile = new File("./resultfiles/flightpath-" + date + ".json");

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            Assert.assertTrue(flightPathFile.exists());

            point[] flightPaths = objectMapper.readValue(flightPathFile, point[].class);
            Assert.assertNotNull(flightPaths);
            System.out.println("System-level test passed for date: " + date);
        }
    }

    public void testPathFinding() {
        LngLat start = new LngLat(-3.186874, 55.944494);
        LngLat goal = new LngLat(-3.192473, 55.946233);

        // Example no-fly zones (replace with real data as needed)
        NamedRegion[] noFlyZones = {};

        ArrayList<point> path = Astar.findpath(start, goal, noFlyZones, false);
        Assert.assertNotNull(path);
        Assert.assertTrue(path.size() > 0);

        LngLat lastPoint = path.get(path.size() - 1).getLngLat();
        Assert.assertTrue(new LngLatHandler().isCloseTo(lastPoint, goal));

        System.out.println("Pathfinding test passed.");
    }
//

    public void testOutputFiles() {
        try {
            // Get the current date
            LocalDate date = LocalDate.now();

            // Create test path data and set the parent for the point
            point parentPoint = new point(-3.187, 55.945); // Example parent node
            point childPoint = new point(-3.186874, 55.944494); // Example child node
            childPoint.setParent(parentPoint); // Set the parent for the child point

            point[] path = {childPoint}; // Path containing the child point

            // Create an empty order array
            Order[] orders = {};

            // Call methods to generate output files
            new App().writePath(path, date);
            new App().writeDeliveries(orders, date);

            // Define file paths for the generated files
            File pathFile = new File("./resultfiles/flightpath-" + date + ".json");
            File deliveriesFile = new File("./resultfiles/deliveries-" + date + ".json");

            // Verify that the files are generated
            Assert.assertTrue("Path file was not generated!", pathFile.exists());
            Assert.assertTrue("Deliveries file was not generated!", deliveriesFile.exists());

            // Verify that the files are not empty
            Assert.assertTrue("Path file is empty!", pathFile.length() > 0);
            Assert.assertTrue("Deliveries file is empty!", deliveriesFile.length() > 0);

            // Print success message if all assertions pass
            System.out.println("Output file generation test passed.");
        } catch (Exception e) {
            // Catch and print any exception that occurs during the test
            e.printStackTrace();
            Assert.fail("Test failed: " + e.getMessage());
        } finally {
            // Clean up the generated test files (optional)
            File pathFile = new File("./resultfiles/flightpath-" + LocalDate.now() + ".json");
            File deliveriesFile = new File("./resultfiles/deliveries-" + LocalDate.now() + ".json");
            pathFile.delete();
            deliveriesFile.delete();
        }
    }
    public void testLngLatHandler() {
        // Initialize LngLatHandler
        LngLatHandler handler = new LngLatHandler();

        // Define test points
        LngLat point1 = new LngLat(-3.186874, 55.944494);
        LngLat point2 = new LngLat(-3.186874, 55.944400); // Adjusted to be closer

        // Verify isCloseTo logic
        boolean closeToResult = handler.isCloseTo(point1, point2);
        System.out.println("Point1 is close to Point2: " + closeToResult);
        Assert.assertTrue("Point1 and Point2 should be close", closeToResult);

        // Define a test region
        NamedRegion testRegion = new NamedRegion("example", new LngLat[]{
                new LngLat(-3.187000, 55.944300),
                new LngLat(-3.186500, 55.944300),
                new LngLat(-3.186500, 55.944700),
                new LngLat(-3.187000, 55.944700)
        });

        // Verify isInRegion logic
        boolean inRegionResult1 = handler.isInRegion(point1, testRegion);
        System.out.println("Point1 is in Region: " + inRegionResult1);
        Assert.assertTrue("Point1 should be inside the region", inRegionResult1);

        boolean inRegionResult2 = handler.isInRegion(new LngLat(-3.188000, 55.945000), testRegion);
        System.out.println("Point outside the region: " + !inRegionResult2);
        Assert.assertFalse("Point should be outside the region", inRegionResult2);

        // Test nextPosition logic
        LngLat nextPos = handler.nextPosition(point1, 90);
        Assert.assertNotNull("Next position should not be null", nextPos);
        System.out.println("Next position: " + nextPos);

        System.out.println("LngLatHandler test passed.");
    }



}



