package com.dronedelivery.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Location {
    final static Log logger = LogFactory.getLog(Location.class);

    private static DecimalFormat DEC_FORMAT = new DecimalFormat(".##");
    private static final String LOCATION_PATTERN = "[NS]\\d+[EW]\\d+";

    private static final int MAX_RADIUS = 480; //max travel time in north-south is 5:10 hours
    private static final float SPEED = 1.0f;

    private final String location;
    private int transportTimeInSeconds;

    /**
     * Constructor
     * @param location location string
     */
    public Location(String location){
        this.location = location;
        transportTimeInSeconds = _getTransportTime();
    }

    /**
     * Time it takes for the drone to make a delivery to this location(seconds)
     * @return drone delivery time
     */
    public int getTransportTimeInSeconds(){
        return transportTimeInSeconds;
    }

    /**
     * Returns the time (in seconds) it takes for the drone to deliver to a particular location
     * based on location passed in
     *
     * @return time rounded to 2 decimals
     */
    private int _getTransportTime(){
        boolean isValid = Pattern.matches(LOCATION_PATTERN, location);
        if(!isValid) {
            throw new IllegalArgumentException(RejectedOrder.RejectReason.INVALID_LOCATION.toString());
        }

        long squareMinutes = 0;
        float retVal;

        final Pattern integerPattern = Pattern.compile("(\\-?\\d+)");
        final Matcher matched = integerPattern.matcher(location);
        while (matched.find()) {
            int blocks = Integer.valueOf(matched.group());
            if(blocks < 0){
                throw new IllegalArgumentException(RejectedOrder.RejectReason.INVALID_LOCATION.toString());
            }
            float minutes = blocks*SPEED;
            squareMinutes += Math.pow(minutes, 2);
        }

        if(squareMinutes < Math.pow(MAX_RADIUS, 2)) {
            long squareSecs = squareMinutes*3600; //convert to sec sq
            retVal = Float.valueOf(DEC_FORMAT.format(Math.sqrt(squareSecs)));
            logger.debug("Location: " + location + " transport time(secs): " + retVal);
        } else {
            throw new IllegalArgumentException(RejectedOrder.RejectReason.DESTINATION_TOO_FAR.toString());
        }
        return Math.round(retVal); //round to nearest integer
    }

    /*public static void main(String argv[]) {
        Location loc = new Location("NN10E5");
    }*/
}
