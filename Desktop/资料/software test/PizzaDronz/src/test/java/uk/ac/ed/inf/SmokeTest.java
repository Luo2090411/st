package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import junit.framework.Assert;
import junit.framework.TestCase;
import uk.ac.ed.inf.App;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.order;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static uk.ac.ed.inf.App.getrestOrders;
import static uk.ac.ed.inf.StressTest.getLNOflyZone;
import static uk.ac.ed.inf.StressTest.readRestaurantsFromFile;

public class SmokeTest extends TestCase {

    private static final String TEST_ORDERS_FILE = "C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\smoke_test_data.json";
    private static final String TEST_RESTAURANTS_FILE = "C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\smoke_test_restaurant_data.json";
    private static final String TEST_NOFLYZONES_FILE = "C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\smoke_test_noflyzones_data.json";

    @Override
    protected void setUp() throws Exception {
        System.out.println("Setting up SmokeTest...");
    }

    // 测试订单验证
    public void testFindValidateOrders() {
        System.out.println("Running testFindValidateOrders...");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {

            Order[] ordersData = getrestOrders(TEST_ORDERS_FILE);
            Restaurant[] restaurantsData = readRestaurantsFromFile(TEST_RESTAURANTS_FILE);

            OrderValidationCode[] expectedValidationCodes = {
                    OrderValidationCode.CARD_NUMBER_INVALID,
                    OrderValidationCode.EXPIRY_DATE_INVALID,
                    OrderValidationCode.CVV_INVALID,
                    OrderValidationCode.TOTAL_INCORRECT,
                    OrderValidationCode.PIZZA_NOT_DEFINED,
                    OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED,
                    OrderValidationCode.NO_ERROR
            };

            order orderValidator = new order();
            for (int i = 0; i < 7; i++) {
                Order validatedOrder = orderValidator.validateOrder(ordersData[i], restaurantsData);
                Assert.assertEquals("Validation failed for order: " + ordersData[i].getOrderNo(),
                        expectedValidationCodes[i], validatedOrder.getOrderValidationCode());
            }
            System.out.println("testFindValidateOrders passed.");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Failed to load test order or restaurant data.");
        }
    }

    // 测试路径规划
    public void testPathFindingAlgo() {
        System.out.println("Running testPathFindingAlgo...");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
//            Restaurant[] restaurantsData = objectMapper.readValue(new File(TEST_RESTAURANTS_FILE), Restaurant[].class);
            NamedRegion[] noFlyZones = getLNOflyZone(TEST_NOFLYZONES_FILE);

            LngLat startPoint = new LngLat(-3.2025, 55.9433);
            LngLat endPoint = new LngLat(-3.1869, 55.9445);

            var path = Astar.findpath(startPoint, endPoint, noFlyZones, false);
            Assert.assertNotNull("Path should not be null", path);
            Assert.assertFalse("Path should not be empty", path.isEmpty());

            LngLat lastPoint = path.get(path.size() - 1).getLngLat();
            if (endPoint.lng() - lastPoint.lng() >0.01) {
                System.out.println("Longitude should match");
            }

            if (endPoint.lat() - lastPoint.lat() >0.01){
                System.out.println("Latitude should match");
            }


            System.out.println("testPathFindingAlgo passed.");

        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Failed to load test restaurant or no-fly zone data.");
        }
    }
}


