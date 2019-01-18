import com.dronedelivery.config.Config;
import com.dronedelivery.engine.OrderProcessor;
import com.dronedelivery.model.RejectedOrder;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DroneDeliveryAppTest {

    @Test
    public void testDispatchTime() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N11W5 05:11:50" );
        op.createOrder("WM002 S3E2 05:11:55" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 2);
        assertEquals(op.getProcessedOrders().get(0).getDispatchTime(), getDateFromString("06:00:00"));
        assertEquals(op.getProcessedOrders().get(1).getDispatchTime(), getDateFromString("06:07:13"));
        assertEquals(op.getNPS(), 94);
    }

    @Test
    public void testInvalidOrderParam() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 05:11:50" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 0);
        assertEquals(op.getRejectedOrders().size(), 1);
        assertEquals(op.getRejectedOrders().get(0).getReason(), RejectedOrder.RejectReason.INVALID_PARAMS);
        assertEquals(op.getNPS(), 0);
    }

    @Test
    public void testDestinationTooFar() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N300W500 05:11:50" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 0);
        assertEquals(op.getRejectedOrders().size(), 1);
        assertEquals(op.getRejectedOrders().get(0).getReason(), RejectedOrder.RejectReason.DESTINATION_TOO_FAR);
    }

    @Test
    public void facilityClosed() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N30W50 22:11:50" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 0);
        assertEquals(op.getRejectedOrders().size(), 1);
        assertEquals(op.getRejectedOrders().get(0).getReason(), RejectedOrder.RejectReason.FACILITY_CLOSED);
    }

    @Test
    public void preemptFasterDelivery() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N11W5 05:11:50" );
        op.createOrder("WM002 S3E2 05:25:55" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 2);
        assertEquals(op.getProcessedOrders().get(0).getOrderId(), "WM002");
        assertEquals(op.getProcessedOrders().get(1).getOrderId(), "WM001");
        assertEquals(op.getNPS(), 94);
    }

    @Test
    public void retainInPlaceOrder() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N11W5 05:11:50" );
        op.createOrder("WM002 S3E2 06:12:55" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 2);
        assertEquals(op.getProcessedOrders().get(0).getOrderId(), "WM001");
        assertEquals(op.getProcessedOrders().get(1).getOrderId(), "WM002");
        //assertEquals(op.getNPS(), 100);
    }

    @Test
    public void retainInPlaceOrder2() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N11W5 05:11:50" );
        op.createOrder("WM004 S1E1 06:02:55" );
        op.createOrder("WM002 S3E3 06:04:55" );
        op.createOrder("WM003 S3E2 06:12:55" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 4);
        assertEquals(op.getProcessedOrders().get(0).getOrderId(), "WM001");
        assertEquals(op.getProcessedOrders().get(1).getOrderId(), "WM004");
        assertEquals(op.getProcessedOrders().get(2).getOrderId(), "WM003");
    }

    @Test
    public void testInvalidLocation() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 A11W5 06:11:50" );
        op.startProcessing();
        assertEquals(op.getRejectedOrders().get(0).getReason(), RejectedOrder.RejectReason.INVALID_LOCATION);
    }

    @Test
    public void testDuplicateId() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N11W5 06:11:50" );
        op.createOrder("WM001 N11W5 06:11:50" );
        op.startProcessing();
        assertEquals(op.getRejectedOrders().get(0).getReason(), RejectedOrder.RejectReason.DUPICATE_ID);
        assertEquals(op.getProcessedOrders().size(), 1);
    }

    private static Date getDateFromString(String dateStr) {
        try {
            return Config.TIME_FORMAT.parse(dateStr);
        }catch(Exception ignore){
        }
        return null;
    }
}
