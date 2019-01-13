package com.dronedelivery.model;

public class Time {

    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    //Can track 16 hours
    private static final int MAX_DURATION_HOURS = 23; //maximum value for this time object
    private static final int MAX_DURATION_SECS = MAX_DURATION_HOURS*60*60 + 59*60 + 59;
    /**
     * Creates time object from hours minutes and seconds
     * @param hours
     * @param minutes
     * @param seconds
     */
    public Time(int hours, int minutes, int seconds){
        if (hours < 0 || hours > MAX_DURATION_HOURS || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59){
            throw new IllegalArgumentException("Time: Invalid time parameter(s) " + hours + ":" + minutes + ":" + seconds);
        }
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    /**
     * Creates time object from seconds
     * @param seconds
     */
    public Time(int seconds){
        if(seconds > MAX_DURATION_SECS || seconds < 0){
            throw new IllegalArgumentException("Time: Duration seconds invalid value " + seconds);
        }
        hours = (int)seconds/3600;
        minutes = (int)(seconds%3600)/60;
        this.seconds = (int)seconds%60;
    }

    /**
     * Returns time object from seconds
     * @param seconds
     * @return
     */
    public static Time getTime(int seconds){
        return new Time(seconds);
    }

    /**
     * Get the time value in seconds
     * @return
     */
    public int getTotalSeconds(){
        return hours*60*60 + minutes*60 + seconds;
    }

    public String toString(){
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /*public static void main(String argv[]){
        Time t1 = new Time(4, 4, 4);
        System.out.println(t1);

        Time t2 = new Time(3700);
        System.out.println(t2);
    }*/
}
