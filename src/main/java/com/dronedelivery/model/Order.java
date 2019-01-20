package com.dronedelivery.model;

import com.dronedelivery.config.Config;
import com.dronedelivery.util.DroneDeliveryUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class Order {
    private final static Log logger = LogFactory.getLog(Order.class);

    private static final String ORDER_PATTERN = "WM\\d+";
    private String orderId;
    private Location location;
    private Date orderPlaceTime;
    private String orderStr;
    private Date dispatchTime;
    private Date deliveryTime;
    private Date returnTime;
    private int transportTime;
    private float score;
    private RejectedOrder.RejectReason rejectReason = null;

    /**
     * Constructor for Object class
     * @param orderId order id for the order
     * @param location delivery location
     * @param orderPlaceTime time order was placed
     */
    public Order(String orderId, String location, Date orderPlaceTime, String orderStr){
        boolean isValid = Pattern.matches(ORDER_PATTERN, orderId);
        if(!isValid) {
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
        if(!(order instanceof Order)){
            return false;
        }
        return orderId.equals(((Order)order).orderId);
    }

    @Override
    public int hashCode() {
        return orderId.hashCode();
    }

    public String getOrderStr(){
        return orderStr;
    }

    public String getOrderId(){
        return orderId;
    }

    public Date getOrderPlaceTime() {
        return orderPlaceTime;
    }

    public Date getDispatchTime() {
        return dispatchTime;
    }

    public Date getDroneReturnTime() {
        return returnTime;
    }

    public int getTransportTime(){
        return transportTime;
    }

    public RejectedOrder.RejectReason getRejectReason() { return rejectReason; }

    /**
     * Schedule drone delivery. Sets the dispatch, delivery and return times on the Order
     *
     * @param prevOrder previous order scheduled (null if first order)
     * @return true/false if the delivery was scheduled
     */
    public boolean scheduleDelivery(Order prevOrder) {
        if(orderPlaceTime.compareTo(Config.getFacilityCloseTime()) > 0 ){
            rejectReason = RejectedOrder.RejectReason.FACILITY_CLOSED;
            return false;
        }

        Calendar cal = Calendar.getInstance();
        dispatchTime = Config.getFacilityOpenTime();//if no previous order exists
        if(prevOrder != null) {//add 1 second to previous drone return time
            cal.setTime(prevOrder.getDroneReturnTime());
            cal.add(Calendar.SECOND, 1);
            dispatchTime = cal.getTime();//set dispatch time
        }
        //edge case. If the order has been placed after facility opens reset dispatch time
        if(orderPlaceTime.compareTo(dispatchTime) > 0){
            dispatchTime=orderPlaceTime;
        }

        //set delivery and return times
        cal.setTime(dispatchTime);
        cal.add(Calendar.SECOND, location.getTransportTimeInSeconds());
        deliveryTime = cal.getTime();//set delivery time
        cal.add(Calendar.SECOND, location.getTransportTimeInSeconds());
        returnTime = cal.getTime();//set return time
        //make sure drone can return before drone facility closes. If not reject
        if(returnTime.compareTo(Config.getFacilityCloseTime()) > 0) {
            returnTime=dispatchTime=null;
            rejectReason = RejectedOrder.RejectReason.DESTINATION_TOO_FAR;
            return false;
        }

        //calculate Score
        calculateScore();
        return true;
    }

    public String toString(){
        SimpleDateFormat fmt = Config.TIME_FORMAT;
        return "OrderId: " + orderId + " PlaceTime: " + fmt.format(orderPlaceTime) +
                " TransportTime: " + Time.getTime(transportTime) +
                " DispatchTime: " + fmt.format(dispatchTime)+ " DeliveryTime: " + fmt.format(deliveryTime) +
                " ReturnTime(+1): " + fmt.format(returnTime);
    }

    public String getFileOutput(){
        return orderId + " " + Config.TIME_FORMAT.format(dispatchTime);
    }

    /**
     * Initialize the fastest time it takes to deliver an order
     */
    private void initTransportTime(){
        //int waitTime = DroneDeliveryUtils.getDifferenceInSeconds(Config.getFacilityOpenTime(), orderPlaceTime).getTotalSeconds();
        transportTime = location.getTransportTimeInSeconds();
        //logger.debug("OrderId: " + orderId + " Transport Time: " + Time.getTime(transportTime));
    }

    /**
     * Calculate local score for this order
     */
    private void calculateScore(){
        int deliveryTimeInt = DroneDeliveryUtils.getDifferenceInSeconds(deliveryTime, orderPlaceTime).getTotalSeconds();
        float hours = deliveryTimeInt/3600f;
        score = 10 - hours;
        if (score < 0) {
            score = 0;
        }
        if(hours < 1){
            score = 10;
        }
        //logger.debug("OrderId: " + orderId + " Score: " + score);
    }

    public float getScore(){
        return score;
    }

    public boolean isPromoter(){
        return score > 8;
    }

    public boolean isDetractor(){
        return score < 7;
    }
}
