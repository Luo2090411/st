package uk.ac.ed.inf;



import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.interfaces.OrderValidation;
import java.time.LocalDate;
import uk.ac.ed.inf.ilp.data.Pizza;

import java.util.ArrayList;
import java.util.Arrays;


public class order implements OrderValidation {
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
//                UNDEFINED,
//                NO_ERROR,
//                CARD_NUMBER_INVALID,
//                EXPIRY_DATE_INVALID,
//                CVV_INVALID,
//                TOTAL_INCORRECT,
//                PIZZA_NOT_DEFINED,
//                MAX_PIZZA_COUNT_EXCEEDED,
//                PIZZA_FROM_MULTIPLE_RESTAURANTS,
//                RESTAURANT_CLOSED;

        int totalprice = 0;
        int pizzanumber = orderToValidate.getPizzasInOrder().length;

        for (int i = 0; i < pizzanumber; i++) {

            totalprice += orderToValidate.getPizzasInOrder()[i].priceInPence();

        }
        totalprice += 100;

        ArrayList<Pizza> allpizza= new ArrayList<>();
        for (int i = 0; i < definedRestaurants.length; i++) {
            for (int j = 0; j < definedRestaurants[i].menu().length; j++) {
                allpizza.add(definedRestaurants[i].menu()[j]);
            }
        }

        int checkpizza = 0;
        boolean nodefinepizza = false;
        for (int i = 0; i < orderToValidate.getPizzasInOrder().length; i++) {
            if (allpizza.contains(orderToValidate.getPizzasInOrder()[i])){
                checkpizza+=1;
            }
        }

        if (checkpizza != orderToValidate.getPizzasInOrder().length){
            nodefinepizza = true;
        }


//                UNDEFINED,
        if (orderToValidate.getOrderNo() == null ){

            orderToValidate.setOrderStatus(OrderStatus.UNDEFINED);
            orderToValidate.setOrderValidationCode(OrderValidationCode.UNDEFINED);
            return orderToValidate;
        }
        //                CARD_NUMBER_INVALID,
        if (orderToValidate.getCreditCardInformation().getCreditCardNumber().length() != 16 ){

            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.CARD_NUMBER_INVALID);
            return orderToValidate;

            //                CARD_NUMBER_INVALID,
        }
        if(orderToValidate.getCreditCardInformation().getCreditCardExpiry().length() != 5){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
            return orderToValidate;

            //                EXPIRY_DATE_INVALID,
        }
        LocalDate today = orderToValidate.getOrderDate();
        String expmonth = orderToValidate.getCreditCardInformation().getCreditCardExpiry().substring(0,2);
        String expyear = orderToValidate.getCreditCardInformation().getCreditCardExpiry().substring(3);

        if (Integer.parseInt(expmonth) < 1 || Integer.parseInt(expmonth) >12 ){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
            return orderToValidate;
        }
        LocalDate expdate = LocalDate.parse("20"+expyear+"-"+expmonth+"-"+"28");

        if (expdate.isBefore(orderToValidate.getOrderDate())){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.EXPIRY_DATE_INVALID);
            return orderToValidate;
        }





//                CVV_INVALID,
        if (orderToValidate.getCreditCardInformation().getCvv().length() != 3 ){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.CVV_INVALID);
            return orderToValidate;

            //                TOTAL_INCORRECT,
        }if (orderToValidate.getPriceTotalInPence() != totalprice){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.TOTAL_INCORRECT);
            return orderToValidate;
//                PIZZA_NOT_DEFINED,

        }if (pizzanumber == 0 || nodefinepizza){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_NOT_DEFINED);
            return orderToValidate;

//                MAX_PIZZA_COUNT_EXCEEDED,
        }if (pizzanumber > 4){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
            return orderToValidate;

        }

        Restaurant restaurantOfOrder = null;
        for (Pizza pizza: orderToValidate.getPizzasInOrder()) {
            for (Restaurant restaurant: definedRestaurants) {
                if(Arrays.asList(restaurant.menu()).contains(pizza)) {
                    if(restaurantOfOrder == null) {
                        restaurantOfOrder = restaurant;
                    }
                    else {
                        if(restaurant != restaurantOfOrder){
                            orderToValidate.setOrderStatus(OrderStatus.INVALID);
                            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
                            return orderToValidate;
                        }
                    }
                }
            }
        }
        if (restaurantOfOrder == null){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.PIZZA_NOT_DEFINED);
            return orderToValidate;
        }
        if(!Arrays.asList(restaurantOfOrder.openingDays())
                .contains(orderToValidate.getOrderDate().getDayOfWeek())){
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
            orderToValidate.setOrderValidationCode(OrderValidationCode.RESTAURANT_CLOSED);
            return orderToValidate;
        }

        orderToValidate.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);

        orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
        return orderToValidate;




    }
}
