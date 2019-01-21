package com.dronedelivery.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Config {

    private final static Log logger = LogFactory.getLog(Config.class);

    private static int NUM_DRONES = 1;
    //public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String OPEN_TIME_STR = "06:00:00";
    private static final String CLOSE_TIME_STR = "22:00:00";

    private static String INPUT_PATH;
    private static String OUTPUT_PATH = System.getProperty("user.home") + File.separator + "droneDeliveryOut" + File.separator;
    private static final String OUTPUT_FILE = OUTPUT_PATH + "droneDeliveryOut.txt";
    private static final String REJECT_FILE = OUTPUT_PATH + "droneDeliveryRejects.txt";

    private static LocalTime OPEN_TIME;
    private static LocalTime CLOSE_TIME;

    //set facility open and close times
    static {
        try {
            OPEN_TIME = LocalTime.parse(OPEN_TIME_STR);
        } catch (Exception ex) {
        }
        try {
            CLOSE_TIME = LocalTime.parse(CLOSE_TIME_STR);
        } catch (Exception ex) {
        }
    }

    public static String getInputFilePath() {
        return INPUT_PATH;
    }

    public static String getOutputFilePath() {
        return OUTPUT_PATH;
    }

    public static String getRejectFile() {
        return REJECT_FILE;
    }

    public static String getOutputFile() {
        return OUTPUT_FILE;
    }

    public static int getNumDrones() {
        return NUM_DRONES;
    }

    public static void parseCommandLine(String args[]) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please provide input file path");
        }
        INPUT_PATH = args[0];
        if (args.length == 2) {
            try {
                int numDrones = Integer.valueOf(args[1]);
                NUM_DRONES = numDrones < 1 ? 1 : numDrones;
                NUM_DRONES = numDrones > 10 ? 10 : numDrones;
            } catch (Exception ignore) {
            }
        }
        logger.info("parseCommandLine: input file: " + INPUT_PATH);
        logger.info("output file path: " + OUTPUT_PATH);
        logger.info("num drones: " + NUM_DRONES);
    }

    public static LocalTime getFacilityOpenTime() {
        return OPEN_TIME;
    }

    public static LocalTime getFacilityCloseTime() { return CLOSE_TIME; }
}
