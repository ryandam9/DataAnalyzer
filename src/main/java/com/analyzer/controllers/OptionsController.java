package com.analyzer.controllers;

import com.analyzer.classes.AppData;
import com.analyzer.ui.TileButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        tilePane.getChildren().add(createTile("Oracle", "oracle"));
        tilePane.getChildren().add(createTile("SQL Server", "sqlserver"));
        tilePane.getChildren().add(createTile("MySQL", "mysql"));
        tilePane.getChildren().add(createTile("Dynamo DB", "dynamoDB"));
        tilePane.getChildren().add(createTile("DB2", "db2"));

        scrollPane.setContent(tilePane);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        borderPane.setCenter(scrollPane);
        tilePane.setMaxWidth(Region.USE_PREF_SIZE);
        credentialsPane.prefHeightProperty().bind(tilePane.prefHeightProperty());
        scrollPane.setFitToWidth(true);
//        scrollPane.setFitToHeight(true);

        borderPane.getStyleClass().add("root");
    }

    private VBox createTile(String name, String imageName) {
        VBox tile = new VBox();

        Button button = new TileButton(name);
        button.getStyleClass().add("tile");

        String imagePath = "resources/images/" + imageName + ".png";
        Image image = new Image(new File(imagePath).toURI().toString(), 150, 150, false, false);
        button.setGraphic(new ImageView(image));

        button.setOnAction(event -> {
            try {

                if (!credentialsPane.getChildren().isEmpty()) {
                    credentialsPane.getChildren().remove(0);
                }

                String dbType = ((TileButton) button).getDbType();
                String fxml = "";

                AppData.dbSelection = dbType;       // Store user selection @ Application level

                switch (dbType) {
                    case AppData.ORACLE:
                        fxml = "oracle_login.fxml";
                        break;

                    case AppData.SQL_SERVER:
                        fxml = "sql_server_login.fxml";
                        break;

                    case AppData.MySQL:
                        fxml = "mysql_login.fxml";
                        break;

                    case AppData.DYNAMO_DB:
                        fxml = "dynamo_login.fxml";
                        break;

                    case AppData.DB2:
                        fxml = "db2.fxml";
                        break;
                }

                URL url = new File("resources/ui/" + fxml).toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                VBox parent = (VBox) loader.load();

//                SqlServerLogin sqlServerLogin = loader.getController();

                credentialsPane.getChildren().addAll(parent);
                parent.prefHeightProperty().bind(scrollPane.heightProperty());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        tile.getChildren().addAll(button);
        return tile;
    }
}