package com.analyzer;

import com.analyzer.controllers.DataBrowser;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;

import static com.analyzer.AppLogger.logger;

public class Utils {
    public static void createStage(String fxmlFileName, String cssFileName, String host, Connection connection, String dbType) {
        try {
            URL url = new File("resources/ui/" + fxmlFileName).toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent parent = (Parent) loader.load();
            String cssFile = new File("resources/css/" + cssFileName).toURI().toURL().toString();
            parent.getStylesheets().add(cssFile);

            DataBrowser dataBrowserController = loader.getController();
            dataBrowserController.setupParameters(host, connection, dbType);

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle(dbType + " Data Browser");
            stage.setScene(new Scene(parent));
            stage.setResizable(true);
            stage.getIcons().add(new Image(new File("resources/images/oracle.png").toURI().toURL().toString()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createStage(String fxmlFileName, String cssFileName, String title) {
        try {
            URL url = new File("resources/ui/" + fxmlFileName).toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent parent = (Parent) loader.load();
            String cssFile = new File("resources/css/" + cssFileName).toURI().toURL().toString();
            parent.getStylesheets().add(cssFile);

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle(title);
            stage.setScene(new Scene(parent));
            stage.setResizable(true);
            stage.getIcons().add(new Image(new File("resources/images/oracle.png").toURI().toURL().toString()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Log an Exception
     *
     * @param e Exception Object
     */
    public static void logStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        logger.debug(sStackTrace);
    }
}