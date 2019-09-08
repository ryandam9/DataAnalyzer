package com.analyzer.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class OptionsController implements Initializable {
    @FXML
    private BorderPane borderPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TilePane tilePane;

    @FXML
    private VBox credentialsPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tilePane.setMaxWidth(Region.USE_PREF_SIZE);

        tilePane.getChildren().add(createTile("Oracle", ""));
        tilePane.getChildren().add(createTile("SQL Server", ""));
        tilePane.getChildren().add(createTile("MySQL", ""));
        tilePane.getChildren().add(createTile("Dynamo DB", ""));

        scrollPane.setContent(tilePane);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        borderPane.setCenter(scrollPane);
    }

    private VBox createTile(String name, String imagePath) {
        VBox tile = new VBox();

        Button button = new Button(name);
        button.getStyleClass().add("tile");

        button.setOnAction(event -> {
            try {
                if (!credentialsPane.getChildren().isEmpty()) {
                    credentialsPane.getChildren().remove(0);
                }

                String dbType = button.getText();
                String fxml = "";

                switch (dbType) {
                    case "Oracle":
                        fxml  = "oracle_login.fxml";
                        break;

                    case "SQL Server":
                        fxml = "sql_server_login.fxml";
                        break;

                    case "MySQL":
                        fxml = "mysql_login.fxml";
                        break;

                    case "Dynamo DB":
                        fxml = "dynamo_login.fxml";
                        break;
                }

                URL url = new File("resources/ui/" + fxml).toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent parent = (Parent) loader.load();
                SqlServerLogin sqlServerLogin = loader.getController();

                credentialsPane.getChildren().addAll(parent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        tile.getChildren().addAll(button);
        return tile;
    }
}