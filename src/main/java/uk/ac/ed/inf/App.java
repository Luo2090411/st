package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.*;
import uk.ac.ed.inf.record.deliver;
import uk.ac.ed.inf.record.graph1;
import uk.ac.ed.inf.record.graph2;
import uk.ac.ed.inf.record.outputpath;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class App {

    //Get resturants and no-fly zones through the reat website
    //This code contains two methods, which are mainly used to obtain data from the specified URL and map the data to the corresponding Java object array.
    public static Restaurant[] getrestResturants(String a) throws IOException {
        // Create an ObjectMapper to handle JSON data and register modules for additional functionalities.
        URL resturantUrl = new URL(a);

        //Reads and parses JSON data from the specified URL into an array of Restaurant objects
        //using an ObjectMapper, providing a convenient method to retrieve restaurant information
        return new ObjectMapper().readValue(resturantUrl, Restaurant[].class);
    }

    public static NamedRegion[] getNOflyZone(String a) throws IOException{
        // similar to getrestResturants()
        URL nOflyZoneUrl = new URL(a);
        return new ObjectMapper().readValue(nOflyZoneUrl, NamedRegion[].class);
    }


    //Get orders from reat website
    //Since there is credibility card information in the order, including the time format, additional steps are required.
//    public static Order[] getrestOrders(String a) throws IOException {
//
//        URL orderUrl = new URL(a);
//
//        // Create an ObjectMapper to handle JSON data and register modules for additional functionalities.
//        // Register the JavaTimeModule to support Java 8 Date/Time classes.
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//
//        // Create a SimpleModule to customize deserialization of specific types
//        SimpleModule module = new SimpleModule();
//
//        // Register a custom deserializer (CardDeserializer) for the CreditCardInformation class.
//        module.addDeserializer(CreditCardInformation.class, new CardDeserializer());
//
//        // Register the custom module with the ObjectMapper.
//        objectMapper.registerModule(module);
//
//        // Read and parse JSON data from the specified URL into an array of Order objects.
//        return objectMapper.readValue(orderUrl, Order[].class);
//    }





    public static Order[] getrestOrders(String filePath) throws IOException {
        // 创建 ObjectMapper 并注册模块
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 创建 SimpleModule 并注册自定义反序列化器
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreditCardInformation.class, new CardDeserializer());
        objectMapper.registerModule(module);

        // 从本地文件读取 JSON 数据并解析为 Order 数组
        File orderFile = new File(filePath);
        return objectMapper.readValue(orderFile, Order[].class);
    }













    //The next three functions generate and write three GeoJSON representations  based on the supplied date (localdate and point[] arrays)
    //The institutions and methods they use are similar

    //In order to standardize the format of the output file, 4 records are created in the record folder
    //record is a newly introduced data class type that simplifies the process of creating immutable classes to represent dataIts immutable nature is very suitable for our file format requirements.


    //Writes delivery information for validated orders to a JSON file .
    public void writeDeliveries(Order[] validatedOrder, LocalDate date) {
        // Map validated orders to  objects containing selected order details.
        // delivery information includes order details such as order number, status, validation code,and total price in pence
        List<deliver> deliverlist = Arrays.stream(validatedOrder).toList().stream().map(
                order -> new deliver(
                        order.getOrderNo(),
                        order.getOrderStatus(),
                        order.getOrderValidationCode(),
                        order.getPriceTotalInPence()
                )).collect(Collectors.toList());

        // Set up ObjectMapper with JavaTimeModule for date serialization.

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            //Writes the provided date to the filename
            String Formate = date.toString();
            String NAME = "deliveries-"+Formate + ".json";

            //Write the delivery list to a JSON file in the "resultfiles" directory.
            //The "resultfiles" directory was created in advance under the root directory
            objectMapper.writeValue(new File("resultfiles/" + NAME), deliverlist);

            //Throw a runtime exception if an I/O error occurs during the file writing process.
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writePath(point[] path,LocalDate date){
        //The flight path information includes details such as parent coordinates, angle and current coordinates for each point in the path.
        //In order to avoid duplication of points, the starting point is removed when generating the point array of the path.

        List<outputpath> pathlist = Arrays.stream(path).toList().stream().map(
             point -> new outputpath(
                     point.parent.getRow(),
                     point.parent.getCol(),
                     point.getAngle(),
                     point.getRow(),
                     point.getCol()

             )).collect(Collectors.toList());


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            String Formate = date.toString();
            String NAME = "flightpath-"+Formate + ".json";
            objectMapper.writeValue(new File("resultfiles/" + NAME), pathlist);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void pathGraph(point[] path, LocalDate date){

        // List to store coordinates of points forming the LineString geometry.
        // List<List<Double>> will show like  this [[1234.1234],[321,321]....]
        List<List<Double>> coordinate = new ArrayList<>();

        for (int i = 0; i < path.length; i++) {
            // Exclude points with a special angle value (hover position).
                if (path[i].angle != 999) {

                    coordinate.add(Arrays.asList(path[i].parent.row, path[i].parent.col));

                    // If it's the last point in the path, add its coordinates.

                    if (i == path.length - 1) {
                        coordinate.add(Arrays.asList(path[i].row, path[i].col));
                    }
                }else {
                    continue;
                }
        }

        // write the linestring form
        graph1 pathGraph = new graph1("LineString", coordinate);
        // finish geojson from
        graph2 pathGraphgeojson = new graph2("Feature","   ",pathGraph);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            String Formate = date.toString();
            String NAME = "drone-"+Formate + ".geojson";
            objectMapper.writeValue(new File("resultfiles/" + NAME), pathGraphgeojson);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    //Filters and validates orders based on the provided date and restaurants.
    //
    public static Order[] gettrueorder(Order[] orders, LocalDate DATE,Restaurant[] restaurants){
        // create localorder[] to store Orders with updated status
        // create selectedOrder to store the Qualifying orders

        Order localorder[] = new Order[orders.length];
        ArrayList<Order> selectedOrder = new ArrayList<>();

        //Traverse and check the entire array and select orders that meet the requirements. The validateOrder method completed by cw1 is used here to check the orders.
        for (int i = 0; i < orders.length; i++) {
            localorder[i] = new order().validateOrder(orders[i], restaurants);

            if (localorder[i].getOrderDate().equals(DATE) && localorder[i].getOrderValidationCode() == OrderValidationCode.NO_ERROR){
                selectedOrder.add(localorder[i]);
            }

        }
        // Convert the ArrayList to an array of valid Order objects.
        return selectedOrder.toArray(new Order[0]);

    }

    // Find the restaurant location for one order.
    // Since the pizza order will come from a restaurant, select the first pizza of the order and traverse the menus of all restaurants until the source is found, output the address of the restaurant
    public static LngLat findlocation(Order order, Restaurant[] restaurants){
        for (int i = 0; i < restaurants.length; i++) {
            for (int j = 0; j < restaurants[i].menu().length; j++) {
                if (order.getPizzasInOrder()[0].name().equals(restaurants[i].menu()[j].name()) ){

                    return restaurants[i].location();
                }


            }



        }
        return null;
    }


    public static void main(String[] args) throws IOException {
        //Divide the entered command into two parts,
        // extract the date string and change the format to localdate,
        // and provide the web page string to rest-related functions
        String dateArgument = args[0];
        String network = args[1];
        LocalDate choosedate = LocalDate.parse(dateArgument);


//      Record important location appleton

        LngLat Appleton = new LngLat(-3.186874, 55.944494);
//
//        LngLat Startposition = Appleton The purpose of startposition is to connect each path end to end.
//        If the drone cannot move by itself, the starting point of each path will be the last considered end point.
//    ;

// read information from rest service
        Restaurant[] restRestaurants = getrestResturants(network+"/restaurants");
//        Order[] restorder = getrestOrders(network+"/orders");

        Order[] restorder = getrestOrders("C:\\Users\\18168\\Desktop\\资料\\software test\\PizzaDronz\\src\\main\\java\\uk\\ac\\ed\\inf\\test_data.json");

        NamedRegion[] noflyareas = getNOflyZone(network+"/noFlyZones");


// get valiedOrder and output first file
        Order[] valiedOrder = gettrueorder(restorder,choosedate,restRestaurants);
        new App().writeDeliveries(valiedOrder, choosedate);
//Since the previous method can only generate a single flight record, a "pathlist" is created to record all flight records.

        List<point> pathlist = new ArrayList<>();
        // for loop generates path for each order
        for (int i = 0; i < valiedOrder.length ; i++) {
            //Call findpath method
            //Calculate the path to go

            if (findlocation(valiedOrder[i],restRestaurants) != null) {
                ArrayList<point> toPath = Astar.findpath(Appleton, findlocation(valiedOrder[i], restRestaurants), noflyareas, false);

                //If necessary, replace the actual coordinates of the search path with this flat, and update it after completing a path.
                //Startposition = toPath.get(toPath.size()-1).getLngLat();

                //Calculate the path to back. When calculating the return journey, we will encounter the requirement of being unable to leave the center point. Enter true to enable detection.
                ArrayList<point> backPath = Astar.findpath(findlocation(valiedOrder[i], restRestaurants), Appleton, noflyareas, true);

                //Startposition = backPath.get(backPath.size()-1).getLngLat();


                //Add the round-trip paths to the total list in sequence
                pathlist.addAll(toPath);
                pathlist.addAll(backPath);
            }
        }
        //Convert the path into a point array to complete the output
        point[] mergedArray = pathlist.toArray(new point[0]);

        new App().writePath(mergedArray,choosedate);
        new App().pathGraph(mergedArray,choosedate);


    }

}



