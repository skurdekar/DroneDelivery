package com.dronedelivery.util;


import com.dronedelivery.config.Config;
import com.dronedelivery.model.Time;

import java.util.Date;

public class DroneDeliveryUtils {

    /**
     * Calculates the difference in time (in minutes) between d1 and d2
     *
     * @param d1 date1
     * @param d2 date2
     * @return difference in minutes
     */
    public static Time getDifferenceInSeconds(Date d1, Date d2) {
        int diff = (int) (d1.getTime() - d2.getTime());
        if (diff > 0) {
            return new Time(diff / 1000);
        } else {
            diff = 0;
        }
        return new Time(diff);
    }

    /*public static void main(String argv[]){
        try {
            Date orderTime = Config.TIME_FORMAT.parse("05:11:50");
            System.out.println(getDifferenceInSeconds(Config.getFacilityOpenTime(), orderTime));
        }catch(Exception ignored){}
    }*/
}
