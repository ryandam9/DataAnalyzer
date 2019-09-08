#!/bin/sh

mvn clean install
cp ./target/DataAnalyzer-1.0-SNAPSHOT.jar ~/Desktop/test_run/  
cp -r src/main/resources ~/Desktop/test_run/

cd ~/Desktop/test_run/
java --module-path $PATH_TO_FX --add-modules javafx.controls --add-modules javafx.fxml -jar ./DataAnalyzer-1.0-SNAPSHOT.jar
