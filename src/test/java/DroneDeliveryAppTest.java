import com.dronedelivery.config.Config;
import com.dronedelivery.engine.OrderFileProcessor;
import com.dronedelivery.engine.OrderProcessor;
import com.dronedelivery.model.RejectedOrder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
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
        assertEquals(op.getNPS(), 0);
    }

    @Test
    public void testTooFar() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N300W500 05:11:50" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 0);
        assertEquals(op.getRejectedOrders().size(), 1);
        assertEquals(op.getRejectedOrders().get(0).getReason(), RejectedOrder.RejectReason.LOCATION_TOO_FAR);
    }

    private static Date getDateFromString(String dateStr) {
        try {
            return Config.TIME_FORMAT.parse(dateStr);
        }catch(Exception ignore){
        }
        return null;
    }
}
