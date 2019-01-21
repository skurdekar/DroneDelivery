package com.dronedelivery.engine;

import com.dronedelivery.config.Config;
import com.dronedelivery.model.Order;
import com.dronedelivery.model.RejectedOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Stream;

/**
 * Handles file related operations
 */
public class OrderFileProcessor {

    private final static Log logger = LogFactory.getLog(OrderProcessor.class);
    private static OrderFileProcessor instance = new OrderFileProcessor();

    public static OrderFileProcessor getInstance() {
        return instance;
    }

    private OrderFileProcessor() {
    }

    /**
     * Read input file
     *
     * @throws Exception if file cannot be read
     */
    public void readOrderInput(OrderProcessor op) throws IOException {
        String inputFilePath = Config.getInputFilePath();
        try (Stream<String> stream = Files.lines(Paths.get(inputFilePath))) {
            for (Object line : stream.toArray()) {
                String lineStr = (String)line;
                if (!lineStr.equals("") && !lineStr.startsWith("#")) {//skip blank or commented lines
                    op.createOrder(lineStr);
                }
            }
        }
    }

    /**
     * Write output file
     *
     * @param processedList ArrayList containing processed orders
     */
    public void writeOrderOutput(ArrayList<Order> processedList, int NPS) {
        String outputFilePath = Config.getOutputFilePath();
        File of = new File(outputFilePath);
        if (!of.exists()) {//make sure directory exists
            of.mkdir();
        }
        try (PrintWriter writer = new PrintWriter(Config.getOutputFile())) {
            for (Order order : processedList) {
                writer.write(order.getFileOutput() + "\n");
            }
            writer.write("NPS " + NPS + "\n");
            logger.info("DroneDelivery: successfully wrote output to " + Config.getOutputFile());
        } catch (Exception ex) {
            logger.error("Could not write to output file: " + Config.getOutputFile(), ex);
        }
    }

    /**
     * Write output file
     *
     * @param rejectedList ArrayList containing processed orders
     */
    public void writeOrderRejects(ArrayList<RejectedOrder> rejectedList) {
        try (PrintWriter writer = new PrintWriter(Config.getRejectFile())) {
            for (RejectedOrder order : rejectedList) {
                writer.write(order + "\n");
            }
            if (rejectedList.size() > 0) {
                logger.info("DroneDelivery: successfully wrote rejects to " + Config.getRejectFile());
            }
        } catch (Exception ex) {
            logger.error("Could not write to rejects file: " + Config.getRejectFile(), ex);
        }
    }

}
