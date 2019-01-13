#Building and Running the application
The application can be downloaded using the following command

git clone https://github.com/skurdekar/dronedelivery.git
The application is packaged with the Gradle Build Tool. It can be built using the following command

gradle build
The application can be run as follows

gradle run --args=<filepath>
gradle run --args='/Users/skurdekar/devSandBox/dronedelivery/droneOrderInput.txt'

The build can be cleaned as follows

gradle clean
*If gradle is not installed on your system the above commands can be run using gradlew (part of the repo)

Once run, the application will prompt you with an input prompt for entering commands to process the reservation

2018-01-19 00:58:16 INFO TicketServiceImpl: - Created TicketService numSeats: 100, rows: 10, hold timeout(s): 30 Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):

Valid commands are **hold, reserve, available, print and end with their respective options as specified above

The application implements logging using the log4j library. The log configuration is available in log4j.properties file in resources directory.

The application implements loading configurations using typesafe-config library. The configurations are stored in application.conf file in resources directory

Alternatively the application can be packaged as a fat jar

gradle fatJar
Once packaged as a fat jar it can be run as a standalone java application

java -classpath build/libs/dronedelivery-all-1.0.jar com.dronedelivery.DroneScheduler <inputFilePath>

Tests
See ReservationAppTest class for test examples Tests should be run using gradle
gradle test Test reports are generated in build/reports/tests/test/classes directory
All tests are run everytime a gradle build command is run
