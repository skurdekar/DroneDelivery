package com.dronedelivery;

import com.dronedelivery.config.Config;
import com.dronedelivery.engine.OrderFileProcessor;
import com.dronedelivery.engine.OrderProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class DroneScheduler {
    private final static Log logger = LogFactory.getLog(DroneScheduler.class);
    private static DroneScheduler instance = new DroneScheduler();
    private DroneScheduler(){}

    public static DroneScheduler getInstance(){
        return instance;
    }

    public void startScheduler() {
        OrderProcessor op = new OrderProcessor();
        try {
            OrderFileProcessor.getInstance().readOrderInput(op);
            op.startProcessing();
            op.writeOutput();
        }catch(IOException ex){
            logger.error("Could not read input file", ex);
        }
    }

    public static void main(String argv[]) {

        try {
            Config.parseCommandLine(argv);
            DroneScheduler.getInstance().startScheduler();
        }catch(Exception ex){
            logger.error("Could not start Drone Scheduler", ex);
            System.exit(1);
        }
    }
}
