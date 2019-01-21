package com.dronedelivery.engine;

import com.dronedelivery.model.Order;

import java.util.Comparator;

public class OrderComparatorDispatchTime implements Comparator<Order> {

    @Override
    public int compare(Order a, Order b) {
        return a.getDispatchTime().compareTo(b.getDispatchTime());
    }
}
