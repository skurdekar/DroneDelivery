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
    final static Log logger = LogFactory.getLog(OrderProcessor.class);

    private ArrayList<Order> orderList = new ArrayList<>();
    private ArrayList<Order> processedList = new ArrayList<>();
    private ArrayList<RejectedOrder> rejectedList = new ArrayList<>();
    private OrderComparatorDlvryDuration comparator = new OrderComparatorDlvryDuration();
    private int NPS;

    public OrderProcessor(){}

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
            if(ex instanceof IllegalArgumentException &&
                    RejectedOrder.RejectReason.LOCATION_TOO_FAR.toString().equals(ex.getMessage())){
                rejectOrder(RejectedOrder.RejectReason.LOCATION_TOO_FAR, orderStr);
            }else{
                rejectOrder(RejectedOrder.RejectReason.INVALID_PARAMS, orderStr);
            }
        }
    }

    private void createOrder(String orderId, String location, Date orderPlaceTime, String orderStr){
        Order order = new Order(orderId, location, orderPlaceTime, orderStr);
        orderList.add(order);
        orderList.sort(comparator);
    }

    /**
     * Start order processing
     */
    public void startProcessing() {
        Date openTime = Config.getFacilityOpenTime();
        Order order = getNextOrder(openTime);//get first order
        Order prevOrder = null;
        while(order != null){
            boolean delivered = order.scheduleDelivery(prevOrder);
            if(delivered) {
                logger.info("startProcessing: Processed Order: " + order);
                processedList.add(order);
                prevOrder = order;
            }else{
                rejectOrder(RejectedOrder.RejectReason.LOCATION_TOO_FAR, order.getOrderStr());
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
        orderList.clear();
        NPS = 0;
    }

    public void writeOutput() {
        OrderFileProcessor.getInstance().writeOrderOutput(processedList, NPS);
        OrderFileProcessor.getInstance().writeOrderRejects(rejectedList);
    }

    public ArrayList<Order> getProcessedOrders(){
        return processedList;
    }

    public ArrayList<RejectedOrder> getRejectedOrders(){
        return rejectedList;
    }

    public int getNPS(){
        return NPS;
    }

    /**
     * Get next order to process
     * @param currentTime the time
     * @return order to process
     */
    private Order getNextOrder(Date currentTime){
        Iterator<Order> iter = orderList.iterator();
        Order otp = null;
        while (iter.hasNext()){
            Order currOrder = iter.next();
            if(currentTime.compareTo(currOrder.getOrderPlaceTime()) >= 0){
                orderList.remove(currOrder);
                otp =  currOrder;
                break;
            }
        }

        //if all orders come in after open time just start processing
        if(otp == null && !orderList.isEmpty()) {
            otp = orderList.get(0);
            orderList.remove(0);
        }
        return otp;
    }

    /**
     * Calculate NPS
     */
    private void calculateNPS() {
        int promoterCount = 0;
        int detractorCount = 0;
        float promoterScore = 0;
        float detractorScore = 0;
        for (Order order : processedList) {
            if (order.isPromoter()) {
                promoterCount++;
                promoterScore += order.getNPS();
            } else if (order.isDetractor()) {
                detractorCount++;
                detractorScore += order.getNPS();
            }
        }

        for (RejectedOrder order : rejectedList) {
            if(order.getReason().equals(RejectedOrder.RejectReason.LOCATION_TOO_FAR)){
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
        logger.info("rejectOrder: Rejected Order: " + orderStr);
        RejectedOrder ro = new RejectedOrder(rr, orderStr);
        rejectedList.add(ro);
    }
}
