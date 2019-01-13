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
    final static Log logger = LogFactory.getLog(Order.class);

    private static final String ORDER_PATTERN = "WM\\d+";
    private String orderId;
    private Location location;
    private Date orderPlaceTime;
    private String orderStr;
    private Date dispatchTime;
    private Date deliveryTime;
    private Date returnTime;
    private int fastestDeliveryDuration;
    private float nps;

    /**
     * Constructor for Object class
     * @param orderId order id for the order
     * @param location delivery location
     * @param orderPlaceTime time order was placed
     */
    public Order(String orderId, String location, Date orderPlaceTime, String orderStr){
        boolean isValid = Pattern.matches(ORDER_PATTERN, orderId);
        if(!isValid) {
            throw new IllegalArgumentException("Order: Invalid OrderId. Must match WM####");
        }
        this.location = new Location(location);
        this.orderId = orderId;
        this.orderPlaceTime = orderPlaceTime;
        this.orderStr = orderStr;
        initFastestDeliveryDuration();
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

    public Date getOrderPlaceTime() {
        return orderPlaceTime;
    }

    public Date getDispatchTime() {
        return dispatchTime;
    }

    public Date getDroneReturnTime() {
        return returnTime;
    }

    public int getFastestDeliveryDuration(){
        return fastestDeliveryDuration;
    }

    /**
     *  Schedule drone deliver
     * @param prevOrder previous order scheduled (null if first order)
     * @return true/false if the delivery was scheduled
     */
    public boolean scheduleDelivery(Order prevOrder) {
        Calendar cal = Calendar.getInstance();
        dispatchTime = Config.getFacilityOpenTime();
        if(prevOrder != null) {//add 1 second to previous drone return time
            cal.setTime(prevOrder.getDroneReturnTime());
            cal.add(Calendar.SECOND, 1);
            dispatchTime = cal.getTime();
        }
        //edge case
        if(orderPlaceTime.compareTo(dispatchTime) > 0){
            dispatchTime=orderPlaceTime;
        }

        cal.setTime(dispatchTime);
        cal.add(Calendar.SECOND, location.getDeliveryTimeInSeconds());
        deliveryTime = cal.getTime();
        cal.add(Calendar.SECOND, location.getDeliveryTimeInSeconds());
        returnTime = cal.getTime();
        //make sure drone can return before drone facility closes
        if(returnTime.compareTo(Config.getFacilityCloseTime()) > 0) {
            returnTime=dispatchTime=null;
            return false;
        }
        calculateNPS();
        return true;
    }

    public String toString(){
        SimpleDateFormat fmt = Config.TIME_FORMAT;
        return "OrderId: " + orderId + " PlaceTime: " + fmt.format(orderPlaceTime) +
                " MinDeliveryTime: " + Time.getTime(fastestDeliveryDuration) +
                " DispatchTime: " + fmt.format(dispatchTime)+ " DeliveryTime: " + fmt.format(deliveryTime) +
                " ReturnTime(+1): " + fmt.format(returnTime);
    }

    public String getFileOutput(){
        return orderId + " " + Config.TIME_FORMAT.format(dispatchTime);
    }

    private void initFastestDeliveryDuration(){
        int waitTime = DroneDeliveryUtils.getDifferenceInSeconds(Config.getFacilityOpenTime(), orderPlaceTime).getTotalSeconds();
        fastestDeliveryDuration = location.getDeliveryTimeInSeconds() + waitTime;
        logger.debug("OrderId: " + orderId + " Min Delivery Time: " + Time.getTime(fastestDeliveryDuration));
    }

    /**
     * Calculate local nps for this order
     */
    private void calculateNPS(){
        int deliveryTimeInt = DroneDeliveryUtils.getDifferenceInSeconds(deliveryTime, orderPlaceTime).getTotalSeconds();
        float hours = deliveryTimeInt/3600f;
        nps = 10 - hours;
        if (nps < 0) {
            nps = 0;
        }
        if(hours < 1){
            nps = 10;
        }
        logger.debug("OrderId: " + orderId + " NPS: " + nps);
    }

    public boolean isPromoter(){
        return nps > 8.5;
    }

    public boolean isDetractor(){
        return nps < 6.5;
    }
}
