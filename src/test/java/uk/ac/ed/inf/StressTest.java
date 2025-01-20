package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import junit.framework.TestCase;

import uk.ac.ed.inf.ilp.data.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.ed.inf.App.*;

public class StressTest extends TestCase {


    public void testSystemStress() throws IOException {
        // Define stress test levels for order quantities
        int[] stressLevels = {10, 100, 1000, 5000, 10000};
        double[] results = new double[stressLevels.length];

        for (int i = 0; i < stressLevels.length; i++) {
            long startTime = System.currentTimeMillis();

            // Generate test order JSON data for the current stress level
            generateTestOrders("./stress_test_orders.json", stressLevels[i]);

            // Load test data from JSON files
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());




            SimpleModule module = new SimpleModule();
            module.addDeserializer(CreditCardInformation.class, new CardDeserializer());
            objectMapper.registerModule(module);




//            Order[] ordersData = objectMapper.readValue(new File("C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\stress_test_orders.json"), Order[].class);
//            Restaurant[] restaurantsData = objectMapper.readValue(new File("C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\restaurant_data.json"), Restaurant[].class);
//            NamedRegion[] noFlyZones = objectMapper.readValue(new File("C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\noflyzones.json"), NamedRegion[].class);
            // 使用相对路径来提高跨平台兼容性


            // 读取订单数据
            Order[] ordersData = getrestOrders("C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\stress_test_orders.json");

                // 读取餐厅数据
            Restaurant[] restaurantsData = readRestaurantsFromFile("C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\restaurant_data.json");

            // 读取禁飞区数据
            NamedRegion[] noFlyZones = getLNOflyZone("C:\\\\Users\\\\18168\\\\Desktop\\\\资料\\\\software test\\\\PizzaDronz\\\\noflyzones.json");


            // Process orders using the App class
            LocalDate testDate = LocalDate.parse("2023-09-01");
            Order[] validOrders = App.gettrueorder(ordersData, testDate, restaurantsData);
            new App().writeDeliveries(validOrders, testDate);

            // Generate the flight paths for all valid orders


            LngLat Appleton = new LngLat(-3.186874, 55.944494);

            List<point> pathList = new ArrayList<>();
            for (Order order : validOrders) {
                if (App.findlocation(order, restaurantsData) != null) {
                    List<point> toPath = Astar.findpath(Appleton, App.findlocation(order, restaurantsData), noFlyZones, false);
                    List<point> backPath = Astar.findpath(App.findlocation(order, restaurantsData), Appleton, noFlyZones, true);
                    pathList.addAll(toPath);
                    pathList.addAll(backPath);
                }
            }

            point[] mergedPathArray = pathList.toArray(new point[0]);
            new App().writePath(mergedPathArray, testDate);
            new App().pathGraph(mergedPathArray, testDate);

            long endTime = System.currentTimeMillis();
            results[i] = (endTime - startTime) / 1000.0; // Convert to seconds

            System.out.println("Stress Level: " + stressLevels[i] + ", Time Elapsed (s): " + results[i]);
        }

        // Cleanup the generated test files
        new File("./stress_test_orders.json").delete();
    }

    // Helper method to generate mock JSON order data
    private void generateTestOrders(String filePath, int count) throws IOException {
        String jsonTemplate = "{\n" +
                "    \"orderNo\": \"TEST1234\",\n" +
                "    \"orderDate\": \"2023-09-01\",\n" +
                "    \"priceTotalInPence\": 2500,\n" +
                "    \"pizzasInOrder\": [\n" +
                "        {\"name\": \"Margherita\", \"priceInPence\": 800},\n" +
                "        {\"name\": \"Pepperoni\", \"priceInPence\": 1200}\n" +
                "    ],\n" +
                "    \"creditCardInformation\": {\n" +
                "        \"creditCardNumber\": \"1234567812345678\",\n" +
                "        \"creditCardExpiry\": \"12/25\",\n" +
                "        \"cvv\": \"123\"\n" +
                "    }\n" +
                "}";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("[");
            for (int i = 0; i < count; i++) {
                writer.write(jsonTemplate);
                if (i != count - 1) writer.write(",");
                writer.newLine();
            }
            writer.write("]");
        }
    }

    static Restaurant[] readRestaurantsFromFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 检查文件是否存在
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Restaurant data file not found: " + file.getAbsolutePath());
        }

        return objectMapper.readValue(file, Restaurant[].class);


    }

    public static NamedRegion[] getLNOflyZone(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 检查文件是否存在
        File noFlyZoneFile = new File(filePath);
        if (!noFlyZoneFile.exists()) {
            throw new IOException("No-fly zones data file not found: " + noFlyZoneFile.getAbsolutePath());
        }

        return objectMapper.readValue(noFlyZoneFile, NamedRegion[].class);
    }
}


