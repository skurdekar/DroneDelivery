package com.dronedelivery.model;

public class RejectedOrder {
    public enum RejectReason {
        DUPICATE_ID, INVALID_PARAMS, INVALID_ID, INVALID_LOCATION,
        DESTINATION_TOO_FAR, FACILITY_CLOSED, UNKNOWN;

        public String toString() {
            return name();
        }

        public static RejectReason fromString(String reasonCode){
            for(RejectReason rr: RejectReason.values()){
                if(rr.toString().equals(reasonCode)){
                    return rr;
                }
            }
            return UNKNOWN;
        }
    }

    private RejectReason rejectReason;
    private String orderStr;

    public RejectedOrder(RejectReason rejectReason, String orderStr) {
        if(rejectReason != null) {
            this.rejectReason = rejectReason;
        }else{
            this.rejectReason = RejectReason.UNKNOWN;
        }
        this.orderStr = orderStr;
    }

    public RejectReason getReason(){
        return rejectReason;
    }

    public String toString(){
        return "Order Rejected: " + rejectReason + " : " + orderStr;
    }
}
