package com.dronedelivery.engine;

import com.dronedelivery.config.Config;
import com.dronedelivery.model.Order;
import com.dronedelivery.model.RejectedOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class OrderProcessor {
    private final static Log logger = LogFactory.getLog(OrderProcessor.class);

    /**
     * List of valid orders sorted by fastest Transport Time
     */
    private ArrayList<Order> sortedOrderList = new ArrayList<>();
    private ArrayList<Order> orderList = new ArrayList<>();

    /**
     * List of scheduled/processed orders
     */
    private ArrayList<Order> processedList = new ArrayList<>();

    /**
     * List of rejected orders
     */
    private ArrayList<RejectedOrder> rejectedList = new ArrayList<>();

    /**
     * Comparator to sort Order list
     */
    private OrderComparatorDlvryDuration comparator = new OrderComparatorDlvryDuration();

    /**
     * NPS Score
     */
    private int NPS;

    public OrderProcessor(){}

    /**
     * Create Order from the order input string contained in file
     *
     * @param orderStr Order input String
     */
    public void createOrder(String orderStr){
        try{
            String[] orderParams = orderStr.split("\\s+");
            if(orderParams.length != 3){
                rejectOrder(RejectedOrder.RejectReason.INVALID_PARAMS, orderStr);
            }else {
                Date orderPlaceTime = Config.TIME_FORMAT.parse(orderParams[2]);
                createOrder(orderParams[0], orderParams[1], orderPlaceTime, orderStr);
            }
        }catch(Exception ex){
            logger.error("createOrder: Exception adding order ", ex);
            rejectOrder(RejectedOrder.RejectReason.fromString(ex.getMessage()), orderStr);
        }
    }

    private void createOrder(String orderId, String location, Date orderPlaceTime, String orderStr){
        Order order = new Order(orderId, location, orderPlaceTime, orderStr);
        if(sortedOrderList.contains(order)){
            throw new IllegalArgumentException(RejectedOrder.RejectReason.DUPICATE_ID.toString());
        }
        sortedOrderList.add(order);
        orderList.add(order);
        sortedOrderList.sort(comparator);//sort by fastest delivery duration
    }

    /**
     * Start order processing. Will Go through order list and try to schedule the order
     */
    public void startProcessing() {
        Date openTime = Config.getFacilityOpenTime();
        Order order = getNextOrder(openTime);//get first order
        Order prevOrder = null;
        while(order != null){
            boolean scheduled = order.scheduleDelivery(prevOrder);
            if(scheduled) {
                logger.info("startProcessing: Scheduled Order: " + order);
                processedList.add(order);
                prevOrder = order;
            }else{
                rejectOrder(order.getRejectReason(), order.getOrderStr());
            }
            if(prevOrder != null) {
                order = getNextOrder(prevOrder.getDroneReturnTime());
            }else{
                order = getNextOrder(openTime);//should never happen
            }
        }
        if(processedList.size() > 0) {
            calculateNPS();
        }
    }

    /**
     * Initializes the OrderProcessor and clears all data
     */
    public void clear(){
        processedList.clear();
        rejectedList.clear();
        sortedOrderList.clear();
        orderList.clear();
        NPS = 0;
    }

    /**
     * Writes Output and Reject files
     */
    public void writeOutput() {
        OrderFileProcessor.getInstance().writeOrderOutput(processedList, NPS);
        OrderFileProcessor.getInstance().writeOrderRejects(rejectedList);
    }

    /**
     * Returns processed orders
     * TODO Clone the list so original list is never returned
     * @return List of processed orders
     */
    public ArrayList<Order> getProcessedOrders(){
        return processedList;
    }

    /**
     * Returns rejected orders
     * TODO Clone the list so original list is never returned
     * @return List of rejected orders
     */
    public ArrayList<RejectedOrder> getRejectedOrders(){
        return rejectedList;
    }

    /**
     * Returns the NPS for the current orders.
     * @return
     */
    public int getNPS(){
        return NPS;
    }

    /**
     * Get next order to process
     * @param currentTime the time
     * @return order to process
     */
    private Order getNextOrder(Date currentTime){
        Iterator<Order> iter = sortedOrderList.iterator();
        Order otp = null;
        while (iter.hasNext()){
            Order currOrder = iter.next();
            //if the current time is greater than order with least transport time, chose the order
            //from sorted list (with least transport time) - first in sorted list
            if(currentTime.compareTo(currOrder.getOrderPlaceTime()) >= 0) {
                otp =  currOrder;
                break;
            }
        }
        //if no orders are found just make the fastest oder current
        /*if(otp == null && !sortedOrderList.isEmpty()) {
            otp = sortedOrderList.get(0);
        }*/
        //get the order with the earliest order place time if no orders were found
        if(otp == null && !orderList.isEmpty()) {
            otp = orderList.get(0);
        }

        //remove the selected order
        if(otp != null){
            sortedOrderList.remove(otp);
            orderList.remove(otp);
        }
        return otp;
    }

    /**
     * Calculate NPS
     */
    private void calculateNPS() {
        int promoterCount = 0, detractorCount = 0;
        float promoterScore = 0, detractorScore = 0;
        for (Order order : processedList) {
            if (order.isPromoter()) {
                promoterCount++;
                promoterScore += order.getScore();
            } else if (order.isDetractor()) {
                detractorCount++;
                detractorScore += order.getScore();
            }
        }

        for (RejectedOrder order : rejectedList) {
            if(order.getReason().equals(RejectedOrder.RejectReason.DESTINATION_TOO_FAR)){
                detractorCount++;
                detractorScore += 0;
            }
        }
        int sampleSize = promoterCount + detractorCount;
        promoterScore = (promoterScore/sampleSize)*10;
        if(detractorCount > 0) {
            detractorScore = ((10 - detractorScore)/sampleSize) * 10;
        }
        NPS = Math.round(promoterScore - detractorScore);

        //int sampleSize = processedList.size() + rejectedList.size();
        //NPS = Math.round((promoterCount * 100f) / sampleSize) - Math.round((detractorCount * 100f) / sampleSize);
        logger.info("calculateNPS: NPS: " + NPS);
    }

    private void rejectOrder(RejectedOrder.RejectReason rr, String orderStr){//order has not been created
        logger.info("rejectOrder: Rejected Order: " + orderStr + " Reason: " + rr.toString());
        RejectedOrder ro = new RejectedOrder(rr, orderStr);
        rejectedList.add(ro);
    }
}
