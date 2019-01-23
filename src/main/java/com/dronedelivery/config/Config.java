package com.dronedelivery.config;

import com.dronedelivery.simulator.OrderSimulator;
import org.apache.commons.cli.*;
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

    private static Options generateOptions() {
        final Option verboseOption = Option.builder("g")
                .required(false)
                .hasArg(false)
                .longOpt("genfile")
                .desc("Generate input file.")
                .build();

        final Option fileOption = Option.builder("f")
                .required(true)
                .longOpt("inputfile")
                .hasArg(true)
                .desc("Input file path.")
                .build();

        final Option numdrones = Option.builder("nd")
                .required(false)
                .type(Integer.class)
                .longOpt("numdrones")
                .hasArg(true)
                .desc("Drone Count")
                .build();

        final Option numorders = Option.builder("no")
                .required(false)
                .type(Integer.class)
                .longOpt("numorders")
                .hasArg(true)
                .desc("Order Count")
                .build();

        final Option blockbounds = Option.builder("bb")
                .required(false)
                .type(Integer.class)
                .longOpt("numblocks")
                .hasArg(true)
                .desc("Max blocks")
                .build();

        final Options options = new Options();
        options.addOption(verboseOption);
        options.addOption(fileOption);
        options.addOption(numdrones);
        options.addOption(numorders);
        options.addOption(blockbounds);

        return options;
    }

    public static void parseCommandLine(String args[]) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(generateOptions(), args);
            INPUT_PATH = cmd.getOptionValue('f');
            logger.info("parseCommandLine: input file: " + INPUT_PATH);
            if(cmd.hasOption("nd")){
                try {
                    int numDrones = Integer.valueOf(cmd.getOptionValue("nd"));
                    NUM_DRONES = numDrones < 1 ? 1 : numDrones;
                    NUM_DRONES = numDrones > 10 ? 10 : numDrones;
                    logger.info("num drones: " + NUM_DRONES);
                } catch (Exception ignore) { }
            }
            if(cmd.hasOption("no")){
                try {
                    int numOrders = Integer.valueOf(cmd.getOptionValue("no"));
                    OrderSimulator.NUM_ORDERS = numOrders < 1 ? 1 : numOrders;
                    OrderSimulator.NUM_ORDERS = numOrders > 500 ? 500 : numOrders;
                    logger.info("num simulated orders: " + OrderSimulator.NUM_ORDERS);
                } catch (Exception ignore) { }
            }
            if(cmd.hasOption("bb")){
                try {
                    int blockBounds = Integer.valueOf(cmd.getOptionValue("bb"));
                    OrderSimulator.BLOCK_BOUNDS = blockBounds < 1 ? 1 : blockBounds;
                    OrderSimulator.BLOCK_BOUNDS = blockBounds > 20 ? 20 : blockBounds;
                    logger.info("Block bounds: " + OrderSimulator.BLOCK_BOUNDS);
                } catch (Exception ignore) { }
            }
            if(cmd.hasOption('g')){
                OrderSimulator.writeOrdersToFile();
            }
        }catch(Exception ex){
            logger.error("Error parsing command line ", ex);
            System.exit(1);
        }
        logger.info("output file path: " + OUTPUT_PATH);
    }

    public static LocalTime getFacilityOpenTime() {
        return OPEN_TIME;
    }

    public static LocalTime getFacilityCloseTime() { return CLOSE_TIME; }
}
