package com.dronedelivery.model;

public class RejectedOrder {
    public enum RejectReason {
        INVALID_PARAMS, LOCATION_TOO_FAR;

        public String toString() {
            return name();
        }
    }

    private RejectReason rejectReason;
    private String orderStr;

    public RejectedOrder(RejectReason rejectReason, String orderStr) {
        this.rejectReason = rejectReason;
        this.orderStr = orderStr;
    }

    public RejectReason getReason(){
        return rejectReason;
    }

    public String toString(){
        return "Order Rejected: " + rejectReason + " : " + orderStr;
    }
}
