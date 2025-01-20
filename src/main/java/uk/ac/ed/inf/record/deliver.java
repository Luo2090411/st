package uk.ac.ed.inf.record;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;

public record deliver(String orderNo, OrderStatus orderStatus, OrderValidationCode orderValidationCode, int costInPence){
}
