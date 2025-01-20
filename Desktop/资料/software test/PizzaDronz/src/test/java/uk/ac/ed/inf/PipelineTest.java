package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PipelineTest extends TestCase {

    private static final String ORDERS_FILE = "C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\smoke_test_data.json";
    private static final String RESTAURANTS_FILE = "C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\smoke_test_restaurant_data.json";
    private static final String NOFLY_ZONES_FILE = "C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\smoke_test_noflyzones_data.json";

    @Override
    protected void setUp() throws Exception {
        System.out.println("Setting up PipelineTest...");
    }

    public void testPipelineProcess() {
        System.out.println("Running testPipelineProcess...");

        try {

            Order[] orders = App.getrestOrders(ORDERS_FILE);
            Restaurant[] restaurants = StressTest.readRestaurantsFromFile(RESTAURANTS_FILE);
            NamedRegion[] noFlyZones = StressTest.getLNOflyZone(NOFLY_ZONES_FILE);

            LocalDate testDate = LocalDate.parse("2023-09-01");


            Order[] validOrders = App.gettrueorder(orders, testDate, restaurants);
            assertNotNull("Valid orders should not be null", validOrders);


//            List<point> flightPath = App.(validOrders, restaurants, noFlyZones);
//            assertNotNull("Flight path should not be null", flightPath);
//            assertFalse("Flight path should not be empty", flightPath.isEmpty());

            // 写入文件进行检查
            new App().writeDeliveries(validOrders, testDate);
//            new App().writePath(flightPath.toArray(new point[0]), testDate);

            File deliveriesFile = new File("./resultfiles/deliveries-" + testDate + ".json");
            File flightPathFile = new File("./resultfiles/flightpath-" + testDate + ".json");

            assertTrue("Deliveries file should exist", deliveriesFile.exists());
            assertTrue("Flight path file should exist", flightPathFile.exists());

            System.out.println("Pipeline test passed.");

        } catch (IOException e) {
            e.printStackTrace();
            fail("Pipeline test failed due to exception: " + e.getMessage());
        }
    }
}

