package com.dronedelivery.model;

import com.dronedelivery.config.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Order {
    private final static Log logger = LogFactory.getLog(Order.class);

    public static final String ORDER_PATTERN = "WM(\\d+)";
    public static final String TIME_PATTERN = "(\\d+):(\\d+):(\\d+)";

    private String orderId;
    private String orderStr;
    private Location location;
    private LocalTime orderPlaceTime;
    private LocalTime dispatchTime;
    private LocalTime deliveryTime;
    private LocalTime returnTime;
    private int transportTime;
    private float score;
    private String droneId;
    private RejectedOrder.RejectReason rejectReason = null;

    /**
     * Constructor for Object class
     *
     * @param orderId        order id for the order
     * @param location       delivery location
     * @param orderPlaceTime time order was placed
     */
    public Order(String orderId, String location, LocalTime orderPlaceTime, String orderStr) {
        boolean isValid = Pattern.matches(ORDER_PATTERN, orderId);
        if (!isValid) {
            throw new IllegalArgumentException(RejectedOrder.RejectReason.INVALID_ID.toString());
        }
        this.location = new Location(location);
        this.orderId = orderId;
        this.orderPlaceTime = orderPlaceTime;
        this.orderStr = orderStr;
        initTransportTime();
    }

    @Override
    public boolean equals(Object order) {
        if (!(order instanceof Order)) {
            return false;
        }
        return orderId.equals(((Order) order).orderId);
    }

    @Override
    public int hashCode() {
        return orderId.hashCode();
    }

    public String getOrderStr() {
        return orderStr;
    }

    public String getOrderId() {
        return orderId;
    }

    public LocalTime getOrderPlaceTime() {
        return orderPlaceTime;
    }

    public LocalTime getDispatchTime() {
        return dispatchTime;
    }

    public LocalTime getDroneReturnTime() {
        return returnTime;
    }

    public int getTransportTime() {
        return transportTime;
    }

    public RejectedOrder.RejectReason getRejectReason() {
        return rejectReason;
    }

    /**
     * Schedule drone delivery. Sets the dispatch, delivery and return times on the Order
     *
     * @param prevOrder previous order scheduled (null if first order)
     * @return true/false if the delivery was scheduled
     */
    public boolean scheduleDelivery(Order prevOrder) {
        if (orderPlaceTime.isAfter(Config.getFacilityCloseTime())) {
            rejectReason = RejectedOrder.RejectReason.FACILITY_CLOSED;
            return false;
        }

        dispatchTime = Config.getFacilityOpenTime();//if no previous order exists
        if (prevOrder != null) {//add 1 second to previous drone return time
            dispatchTime = prevOrder.getDroneReturnTime().plusSeconds(1);
        }
        //edge case. If the order has been placed after facility opens reset dispatch time
        if (orderPlaceTime.isAfter(dispatchTime)) {
            dispatchTime = orderPlaceTime;
        }

        //set delivery and return times
        deliveryTime = dispatchTime.plusSeconds(location.getTransportTimeInSeconds());
        returnTime = deliveryTime.plusSeconds(location.getTransportTimeInSeconds());//set return time
        //make sure drone can return before drone facility closes. If not reject
        if (returnTime.isAfter(Config.getFacilityCloseTime())) {
            returnTime = dispatchTime = null;
            rejectReason = RejectedOrder.RejectReason.DESTINATION_TOO_FAR;
            return false;
        }
        droneId = Thread.currentThread().getName();

        //calculate Score
        calculateScore();
        return true;
    }

    public String toString() {
        DateTimeFormatter fmt = Config.TIME_FORMAT;
        return "OrderId: " + orderId + " PlaceTime: " + fmt.format(orderPlaceTime) +
                " TransportTime: " + Time.getTime(transportTime) +
                " DispatchTime: " + fmt.format(dispatchTime) + " DeliveryTime: " + fmt.format(deliveryTime) +
                " ReturnTime(+1): " + fmt.format(returnTime);
    }

    public String getFileOutput() {
        return String.format("%s %s %s", droneId, orderId, Config.TIME_FORMAT.format(dispatchTime));
    }

    /**
     * Initialize the fastest time it takes to deliver an order
     */
    private void initTransportTime() {
        transportTime = location.getTransportTimeInSeconds();
        //logger.debug("OrderId: " + orderId + " Transport Time: " + Time.getTime(transportTime));
    }

    /**
     * Calculate local score for this order
     */
    private void calculateScore() {
        long deliveryTimeLong = (int)orderPlaceTime.until(deliveryTime, SECONDS);
        float hours = deliveryTimeLong / 3600f;
        score = 10 - hours;
        if (score < 0) {
            score = 0;
        }
        if (hours < 1) {
            score = 10;
        }
        //logger.debug("OrderId: " + orderId + " Score: " + score);
    }

    public boolean isPromoter() {
        return score > 8;
    }

    public boolean isDetractor() {
        return score < 7;
    }
}
