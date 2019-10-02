#!/bin/sh

mvn clean install
cp ./target/DataAnalyzer-1.0-SNAPSHOT.jar ~/Desktop/test_run/  
cp -r src/main/resources ~/Desktop/test_run/
cp -r ./target/lib ~/Desktop/test_run/resources/
cp -r /c/Users/ryand/Desktop/dbutils/src/main/resources/* ~/Desktop/test_run/resources/

cd ~/Desktop/test_run/
export LOG4J_CONFIGURATION_FILE="C:\Users\ryand\Desktop\test_run\resources\log4j2-da.xml"
java --module-path $PATH_TO_FX --add-modules javafx.controls --add-modules javafx.fxml -jar ./DataAnalyzer-1.0-SNAPSHOT.jar
