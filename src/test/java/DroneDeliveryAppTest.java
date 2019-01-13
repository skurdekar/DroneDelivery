import com.dronedelivery.config.Config;
import com.dronedelivery.engine.OrderFileProcessor;
import com.dronedelivery.engine.OrderProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DroneDeliveryAppTest {

    @Test
    public void test1() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 N11W5 05:11:50" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 1);
        assertEquals(op.getProcessedOrders().get(0).getDispatchTime(), getDateFromString("06:00:00"));
        assertEquals(op.getNPS(), 100);
    }

    @Test
    public void test2() {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("WM001 05:11:50" );
        op.startProcessing();
        assertEquals(op.getProcessedOrders().size(), 0);
        assertEquals(op.getRejectedOrders().size(), 1);
        assertEquals(op.getNPS(), 0);
    }

    private static Date getDateFromString(String dateStr) {
        try {
            return Config.TIME_FORMAT.parse(dateStr);
        }catch(Exception ignore){
        }
        return null;
    }
}
