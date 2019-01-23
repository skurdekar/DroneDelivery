package com.dronedelivery.simulator;

import com.dronedelivery.config.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

public class OrderSimulator {
    private final static Log logger = LogFactory.getLog(OrderSimulator.class);
    private final static int BLOCK_BOUNDS = 10;
    public static int NUM_ORDERS = 500;
    private static final Random random = new Random();

    private static ArrayList<String> generateOrderList(){
        ArrayList<String> orderList = new ArrayList<>();
        for(int i=0; i < NUM_ORDERS; i++){
            String orderId = "WM" + String.format("%04d", i+1);
            String ns = random.nextInt(1) + 1 == 1 ? "N" : "S";
            int nsBlock = random.nextInt(BLOCK_BOUNDS);
            int ewBlock = random.nextInt(BLOCK_BOUNDS);
            String ew = random.nextInt(1) + 1 == 1 ? "E" : "W";
            String orderPlaceTime = String.join(":",
                      String.format("%02d", random.nextInt(16) + 5),
                      String.format("%02d", random.nextInt(60)),
                      String.format("%02d", random.nextInt(60)));
            String orderStr = String.join(" ", orderId,
                    ns + nsBlock + ew + ewBlock,
                    orderPlaceTime);
            orderList.add(orderStr);
        }
        return orderList;
    }

    public static final void writeOrdersToFile(){
        ArrayList<String> orderList = generateOrderList();
        String inputFilePath = Config.getInputFilePath();
        File f = new File(inputFilePath);
        if(f.exists()){
            f.delete();
        }
        try(BufferedWriter fw = new BufferedWriter(new FileWriter(inputFilePath))){
                orderList.forEach(orderStr -> {
                    try {
                        fw.write(orderStr);
                        fw.write(System.getProperty("line.separator"));
                    }catch(Exception ex){}
                });
        }catch(Exception ex){
            logger.error("Unable to generate input file ", ex);
        }
    }
}
