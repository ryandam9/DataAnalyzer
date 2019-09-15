package com.analyzer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;

public class Utils {
    public static void createState(String fxmlFileName, String cssFileName) {
        try {
            URL url = new File("resources/ui/" + fxmlFileName).toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent parent = (Parent) loader.load();

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Job Monitor");
            stage.setScene(new Scene(parent));

            String cssFile = new File("resources/css/" + cssFileName).toURI().toURL().toString();
            parent.getStylesheets().add(cssFile);
            stage.setResizable(true);
            stage.getIcons().add(new Image(new File("resources/images/oracle.png").toURI().toURL().toString()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}