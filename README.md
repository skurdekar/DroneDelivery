##Building and Running the application
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

The application implements logging using the log4j library. The log configuration is available in log4j.properties file in resources directory.

Alternatively the application can be packaged as a fat jar

gradle fatJar
Once packaged as a fat jar it can be run as a standalone java application

java -classpath build/libs/dronedelivery-all-1.0.jar com.dronedelivery.DroneScheduler <inputFilePath>

**Tests**

See ReservationAppTest class for test examples Tests should be run using gradle
gradle test Test reports are generated in build/reports/tests/test/classes directory
All tests are run everytime a gradle build command is run
