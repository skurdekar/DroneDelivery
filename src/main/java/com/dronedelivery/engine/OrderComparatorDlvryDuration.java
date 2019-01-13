package com.dronedelivery.engine;

import com.dronedelivery.model.Order;

import java.util.Comparator;

public class OrderComparatorDlvryDuration implements Comparator<Order> {

    @Override
    public int compare(Order a, Order b) {
        return Integer.compare(a.getFastestDeliveryDuration(), b.getFastestDeliveryDuration());
    }
}
