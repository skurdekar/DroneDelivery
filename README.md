## Building and Running the application
The application can be downloaded using the following command (sample input file droneOrderInput.txt is provided)

- `git clone https://github.com/skurdekar/DroneDelivery.git`
The application is packaged with the Gradle Build Tool. It can be built using the following command

- `gradle build`
The application can be run as follows (using sample input)

- `gradle run --args=<filepath>`
- `gradle run --args='droneOrderInput.txt'`
Input file lines starting with `#` are ignored.

The build can be cleaned as follows

- `gradle clean`

* If gradle is not installed on your system the above commands can be run using *gradlew* (part of the repo)

The application implements logging using the log4j library. The log configuration is available in log4j.properties file in resources directory.

Alternatively the application can be packaged as a fat jar

- `gradle fatJar`
Once packaged as a fat jar it can be run as a standalone java application

- `java -classpath build/libs/dronedelivery-all-1.0-SNAPSHOT.jar com.dronedelivery.DroneScheduler <inputFilePath>`

## Output
```
    output file path: /Users/skurdekar/droneDeliveryOut/
    location: N11W5 delivery time: 724.98
    OrderId: WM001 Min Delivery Time: 01:00:15
    location: S3E2 delivery time: 216.33
    OrderId: WM002 Min Delivery Time: 00:51:41
    location: N7E50 delivery time: 3029.26
    OrderId: WM003 Min Delivery Time: 01:18:39
    location: N11E5 delivery time: 724.98
    OrderId: WM004 Min Delivery Time: 00:12:05
    OrderId: WM002 NPS: 10.0
    startProcessing: Processed Order: OrderId: WM002 PlaceTime: 05:11:55 MinDeliveryTime: 00:51:41 DispatchTime: 06:00:00 DeliveryTime: 06:03:36 ReturnTime(+1): 06:07:12
    OrderId: WM001 NPS: 8.875555
    startProcessing: Processed Order: OrderId: WM001 PlaceTime: 05:11:50 MinDeliveryTime: 01:00:15 DispatchTime: 06:07:13 DeliveryTime: 06:19:18 ReturnTime(+1): 06:31:23
    OrderId: WM004 NPS: 10.0
    startProcessing: Processed Order: OrderId: WM004 PlaceTime: 06:11:50 MinDeliveryTime: 00:12:05 DispatchTime: 06:31:24 DeliveryTime: 06:43:29 ReturnTime(+1): 06:55:34
    OrderId: WM003 NPS: 7.762778
    startProcessing: Processed Order: OrderId: WM003 PlaceTime: 05:31:50 MinDeliveryTime: 01:18:39 DispatchTime: 06:55:35 DeliveryTime: 07:46:04 ReturnTime(+1): 08:36:33
    calculateNPS: NPS: 75
    DroneDelivery: successfully wrote output to /Users/skurdekar/droneDeliveryOut/droneDeliveryOut.txt
```
#### Output file is written to `userhome/droneDeliveryOut/droneDeliveryOut.txt`

#### Rejects file is written to `userhome/droneDeliveryOut/droneDeliveryRejects.txt`

## Unit Tests

See DroneDeliveryAppTest class for test examples. 
Tests should be run using gradle
gradle test Test reports are generated in build/reports/tests/test/classes directory
All tests are run everytime a gradle build command is run

## Logic & Assumptions

### Logic used to Schedule the Drone and pick the next Order
The orders are scheduled based on least time to delivery from the time of order placement. This includes wait time in case an order is placed before the facility opens. In case an order is placed after the facility opens, but takes lesser time than an order already in queue it will be scheduled ahead of the older order. The time to delivery is the diagnoal distance (hypotenuse) of the triangle with NS and EW co-ordinates. Based on the example provided it was clear that the Drone can travel diagnolly to deliver an order.

### Drone Operating Area
Since the Drone dispatch center is only open for 16 hours every day (from 6 am to 10 pm) and the Drone speed is 1 block (horizontal or vertical) per minute we have to make sure the Drone gets back in 16 hours. The operating distance will be limited to an area that takes less than 8 hours to and fro for delivery. We will limit the circular area to 480 radial blocks (giving us a diagonal to be back in 16 hours for every delivery). Any location that results in a diagonal bigger than 480 blocks will be deemed invalid and the order will not be processed. Options considered for delivery area were Square, Rectangular and Circular. Circular shape gives us the biggest processing area and hence was chosen.

### Invalid parameters in input file
If the file contains bad data the order will be rejected and count against NPS calculation as a detractor with the worst score.

### Error handling
There is minimal error handling implemented in the application which is in no way indicative of how an application should handle edge cases, bad data and erroneous conditions in production. 

### Assumption Handling time at dispatch and delivery
Based on the example provided there is no delay in actually depositing an order at its destination or loading items at the dispatch facility. The only delay in the processing is 1 second delay between returning of the Drone and dispatch. The applications builds on the sample output provided for its calculations and this is not a real world scenario where there will be some delays.

### Assumption: Deliveries that cannot be processed the same day will be rejected
Prior to scheduling delivery the scheduler ensures that the Drone can be back before the operating center closes (10 pm). Any orders that cannot be processed will be rejected and stored in a reject file droneDeliveryRejects.txt (in the same directory as output file). All rejects will be considered incomplete orders and can be set for manual process the next day. The handling of rejects is beyond the scope of this solution.

### Assumption: Solution will not handle rolling over undelivered orders to next day
For NPS calculation rejected orders will be considered to have a 0 NPS Score. Handling of rejected orders is beyond scope of this application. While it is understood that within the NPS calculation window orders can still be delivered next day by 8 am that consideration has not been used.

### Assumption: NPS Calculation
NPS Calculation will be done using the following formula
`Total promoter recommendation/(promoters + detractors) *10 - Total detractor recommendation/(promoters + detractors)*10`
It is not completely clear from the description what the intended logic should be as per the example.

## Next Steps

- The problem poses a significant scheduling challenge at scale. Drones will need to be shared with possible Drone overlapp areas using circular regions. Shared drones will need to work off of concurrent order queues. Calculation and sizing can be based on how much a drone can travel in a particular day.

- Enhanced unit test will need to be added for border and edge case scenarios.

- OrderProcessor can implement a service interface for remote interaction with other Drone Schedulers

- Edge case scenarios and handling bad data needs to be handled in a comprehensive manner.
