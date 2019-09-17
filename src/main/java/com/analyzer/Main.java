package com.analyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

import static com.analyzer.AppLogger.logger;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL url = new File("resources/ui/options.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);

        Scene scene = new Scene(root, 1320, 1000);
        String cssFile = new File("resources/css/theme-1.css").toURI().toURL().toString();
        scene.getStylesheets().add(cssFile);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Data Analyzer");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}