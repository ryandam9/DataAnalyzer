#!/bin/sh

mvn clean install
cp ./target/DataAnalyzer-1.0-SNAPSHOT.jar ~/Desktop/test_run/  
cp -r src/main/resources ~/Desktop/test_run/
cp -r ./target/lib ~/Desktop/test_run/resources/
cp -r /c/Users/ravis/Desktop/projects/dbutils/src/main/resources/* ~/Desktop/test_run/resources/

cd ~/Desktop/test_run/
export LOG4J_CONFIGURATION_FILE="C:\Users\ravis\Desktop\test_run\resources\log4j2-da.xml"

# Set Paths
export JAVA_HOME="/c/Users/ravis/Desktop/software/jdk-14"
export PATH="/c/Users/ravis/Desktop/software/jdk-14/bin":$PATH
export PATH_TO_FX="/c/Users/ravis/Desktop/software/javafx-sdk-14/lib"

java --module-path $PATH_TO_FX --add-modules javafx.controls --add-modules javafx.fxml -jar ./DataAnalyzer-1.0-SNAPSHOT.jar
