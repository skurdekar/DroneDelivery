package com.dronedelivery.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Config {

    final static Log logger = LogFactory.getLog(Config.class);

    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final String OPEN_TIME_STR = "06:00:00";
    private static final String CLOSE_TIME_STR = "22:00:00";

    private static String INPUT_PATH;
    private static String OUTPUT_PATH = System.getProperty("user.home") + File.separator + "droneDeliveryOut" + File.separator;
    private static final String OUTPUT_FILE = OUTPUT_PATH + "droneDeliveryOut.txt";
    private static Date OPEN_TIME;
    private static Date CLOSE_TIME;

    static{
        try{
            OPEN_TIME = TIME_FORMAT.parse(OPEN_TIME_STR);
        }catch(Exception ex){
        }
        try{
            CLOSE_TIME = TIME_FORMAT.parse(CLOSE_TIME_STR);
        }catch(Exception ex){
        }
    }

    public static String getInputFilePath() {
        return INPUT_PATH;
    }

    public static String getOutputFilePath() {
        return OUTPUT_PATH;
    }

    public static String getOutputFile() {
        return OUTPUT_FILE;
    }

    public static void parseCommandLine(String argv[]){
        if(argv.length < 1){
            throw new IllegalArgumentException("Please provide input file path");
        }
        INPUT_PATH = argv[0];
        //OUTPUT_PATH = argv[1];
        logger.info("parseCommandLine: input file: " + INPUT_PATH);
        logger.info("output file path: " + OUTPUT_PATH);
    }

    public static Date getFacilityOpenTime(){
        return (Date)OPEN_TIME.clone();
    }

    public static Date getFacilityCloseTime(){
        return (Date)CLOSE_TIME.clone();
    }
}
