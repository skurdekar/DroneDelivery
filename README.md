## Building and Running the application
The application can be downloaded using the following command

- `git clone https://github.com/skurdekar/DroneDelivery.git`
The application is packaged with the Gradle Build Tool. It can be built using the following command

- `gradle build`
The application can be run as follows

- `gradle run --args=<filepath>`
- `gradle run --args='droneOrderInput.txt'`

The build can be cleaned as follows

- `gradle clean`
*If gradle is not installed on your system the above commands can be run using gradlew (part of the repo)

The application implements logging using the log4j library. The log configuration is available in log4j.properties file in resources directory.

Alternatively the application can be packaged as a fat jar

- `gradle fatJar`
Once packaged as a fat jar it can be run as a standalone java application

- `java -classpath build/libs/dronedelivery-all-1.0.jar com.dronedelivery.DroneScheduler <inputFilePath>`

## Output
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

## Tests

See DroneDeliveryAppTest class for test examples. 
Tests should be run using gradle
gradle test Test reports are generated in build/reports/tests/test/classes directory
All tests are run everytime a gradle build command is run
