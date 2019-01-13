package com.dronedelivery;

import com.dronedelivery.config.Config;
import com.dronedelivery.engine.OrderFileProcessor;
import com.dronedelivery.engine.OrderProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DroneScheduler {
    final static Log logger = LogFactory.getLog(DroneScheduler.class);
    private static DroneScheduler instance = new DroneScheduler();
    private DroneScheduler(){}

    public static DroneScheduler getInstance(){
        return instance;
    }

    public void startScheduler() throws Exception{
        OrderProcessor op = new OrderProcessor();
        OrderFileProcessor.getInstance().readOrderInput(op);
        op.startProcessing();
        op.writeOutput();
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
