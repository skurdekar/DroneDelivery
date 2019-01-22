package com.dronedelivery.engine;

import com.dronedelivery.config.Config;
import com.dronedelivery.model.Order;
import com.dronedelivery.model.RejectedOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderProcessor {
    private final static Log logger = LogFactory.getLog(OrderProcessor.class);

    /**
     * List of valid orders sorted by fastest Transport Time
     */
    private ArrayList<Order> sortedOrderList = new ArrayList<>();

    /**
     * List sorted by order place time
     */
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
     * NPS Score
     */
    private int NPS;

    private Object synch = new Object();

    public OrderProcessor() {
    }

    private int[] parseTimeString(String timeString){
        return Arrays.stream(timeString.split(":")).mapToInt(Integer::parseInt).toArray();
    }
    /**
     * Create Order from the order input string contained in file
     *
     * @param orderStr Order input String
     */
    public void createOrder(String orderStr) {
        try {
            String[] orderParams = orderStr.split("\\s+");
            if (orderParams.length != 3) {
                rejectOrder(RejectedOrder.RejectReason.INVALID_PARAMS, orderStr);
            } else {
                int[] timesInt = parseTimeString(orderParams[2]);
                LocalTime orderPlaceTime = LocalTime.of(timesInt[0], timesInt[1], timesInt[2]);
                createOrder(orderParams[0], orderParams[1], orderPlaceTime, orderStr);
            }
        } catch (Exception ex) {
            logger.error("createOrder: Exception adding order ", ex);
            rejectOrder(RejectedOrder.RejectReason.fromString(ex.getMessage()), orderStr);
        }
    }

    private void createOrder(String orderId, String location, LocalTime orderPlaceTime, String orderStr) {
        Order order = new Order(orderId, location, orderPlaceTime, orderStr);
        if (sortedOrderList.contains(order)) {
            throw new IllegalArgumentException(RejectedOrder.RejectReason.DUPICATE_ID.toString());
        }
        sortedOrderList.add(order);
        orderList.add(order);
    }

    public void process() {
        sortedOrderList.sort(Comparator.comparingInt(Order::getTransportTime));//sort by fastest delivery duration
        orderList.sort(Comparator.comparing(Order::getOrderPlaceTime));//sort by earlier place time

        ArrayList<Thread> threadList = new ArrayList<>(Config.getNumDrones());
        for (int i = 0; i < Config.getNumDrones(); i++) {
            threadList.add(new Thread(new OrderRunner(this, i)));
        }
        for (Thread t : threadList) {
            t.start();
        }
        for (Thread t : threadList) {//wait for drones to finish processing
            try {
                t.join();
            } catch (InterruptedException ex) {
                logger.error("Thread interrupted " + t.getName());
            }
        }
        if (processedList.size() > 0) {
            processedList.sort(Comparator.comparing(Order::getDispatchTime));
            Map<String, Long> droneProcessCount = processedList.stream()
                    .map(Order::getDroneId).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            logger.debug("Drone Process Count :" + droneProcessCount);
            calculateNPS();
        }
    }

    /**
     * Start order processing. Will Go through order list and try to schedule the order
     */
    public void pvtStartProcessing() {
        LocalTime openTime = Config.getFacilityOpenTime();

        Order order;
        synchronized (synch) {
            order = getNextOrder(openTime);//get first order
        }
        Order prevOrder = null;
        while (order != null) {
            boolean scheduled = order.scheduleDelivery(prevOrder);
            synchronized (synch) {
                if (scheduled) {
                    logger.info(Thread.currentThread().getName() + " process: Scheduled Order: " + order);
                    processedList.add(order);
                    prevOrder = order;
                } else {
                    rejectOrder(order.getRejectReason(), order.getOrderStr());
                }
                if (prevOrder != null) {
                    order = getNextOrder(prevOrder.getDroneReturnTime());
                } else {
                    order = getNextOrder(openTime);//should never happen
                }
            }
        }
    }

    /**
     * Initializes the OrderProcessor and clears all data
     */
    public void clear() {
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
     *
     * @return List of processed orders
     */
    public ArrayList<Order> getProcessedOrders() {
        return processedList;
    }

    /**
     * Returns rejected orders
     * TODO Clone the list so original list is never returned
     *
     * @return List of rejected orders
     */
    public ArrayList<RejectedOrder> getRejectedOrders() {
        return rejectedList;
    }

    /**
     * Returns the NPS for the current orders.
     *
     * @return
     */
    public int getNPS() {
        return NPS;
    }

    /**
     * Get next order to process. This method is not thread safe and should not be invoked directly from
     * the processor threads
     *
     * @param currentTime the time
     * @return order to process
     */
    private Order getNextOrder(LocalTime currentTime) {
        Order otp = sortedOrderList.stream()
                .filter(o->currentTime.isAfter(o.getOrderPlaceTime()) || currentTime.equals(o.getOrderPlaceTime()))
                .findAny()
                .orElse(null);

        //get the order with the earliest order place time if no orders were found
        if (otp == null && !orderList.isEmpty()) {
            otp = orderList.get(0);
        }

        //remove the selected order
        if (otp != null) {
            sortedOrderList.remove(otp);
            orderList.remove(otp);
        }
        return otp;
    }

    /**
     * Calculate NPS
     */
    private void calculateNPS() {
        float promoterCount = 0, detractorCount = 0, passiveCount = 0;

        for (Order order : processedList) {
            if (order.isPromoter()) {
                promoterCount++;
            } else if (order.isDetractor()) {
                detractorCount++;
            } else {
                passiveCount++;
            }
        }

        for (RejectedOrder order : rejectedList) {
            if (order.getReason().equals(RejectedOrder.RejectReason.DESTINATION_TOO_FAR) ||
                    order.getReason().equals(RejectedOrder.RejectReason.FACILITY_CLOSED)) {
                detractorCount++;
            }
        }

        float sampleSize = promoterCount + detractorCount + passiveCount;
        float promoterScore = (promoterCount / sampleSize) * 100;
        float detractorScore = (detractorCount / sampleSize) * 100;
        NPS = Math.round(promoterScore - detractorScore);

        logger.info("calculateNPS: NPS: " + NPS);
    }

    private void rejectOrder(RejectedOrder.RejectReason rr, String orderStr) {
        logger.info(Thread.currentThread().getName() + " rejectOrder: Rejected Order: " + orderStr +
                " Reason: " + rr.toString());
        RejectedOrder ro = new RejectedOrder(rr, orderStr);
        rejectedList.add(ro);
    }

    class OrderRunner implements Runnable {
        OrderProcessor processor;
        int index;

        OrderRunner(OrderProcessor processor, int index) {
            this.processor = processor;
            this.index = index;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Drone-" + ++index);
            processor.pvtStartProcessing();
        }
    }
}
