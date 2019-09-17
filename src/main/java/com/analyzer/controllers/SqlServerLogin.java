package com.analyzer.controllers;

import com.analyzer.DBConnections;
import com.analyzer.Utils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class SqlServerLogin implements Initializable {
    @FXML
    private TextField userId;

    @FXML
    private TextField dbServer;

    @FXML
    private TextField port;

    @FXML
    private Button connectBtn;

    @FXML
    private VBox statusBox;

    @FXML
    private Label status;

    private ProgressIndicator progressIndicator;

    private Task<Connection> task;
    private static Connection connection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);
        progressIndicator.setVisible(false);
        statusBox.getChildren().addAll(progressIndicator);
    }

    @FXML
    private void connectToDb(ActionEvent event) {
        final String userId = this.userId.getText();
        final String dbServer = this.dbServer.getText();
        final String port = this.port.getText();

        task = new DBConnectionTask(userId, dbServer, port);
        new Thread(task).start();
    }

    class DBConnectionTask extends Task<Connection> {
        private final String userName;
        private final String serverInstance;
        private final String portNo;

        public DBConnectionTask(String userName, String serverInstance, String portNo) {
            this.userName = userName;
            this.serverInstance = serverInstance;
            this.portNo = portNo;
        }

        @Override
        protected Connection call() {
            Platform.runLater(() -> status.setText(""));
            Platform.runLater(() -> progressIndicator.setVisible(true));
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            Connection connection = null;

            try {
                connection = DBConnections.getSqlServerConnection(userName, serverInstance, portNo);
            } catch (Exception e) {
                final String errorMessage = "Unable to connect to the DB using " + userName + ";" + e.getMessage();
                Platform.runLater(() -> status.setText(errorMessage));
                throw new RuntimeException("Unable to connect to the DB using " + userName);
            }

            Platform.runLater(() -> status.setText("Connection acquired !"));
            return connection;
        }

        @Override
        public void succeeded() {
            super.succeeded();
            connection = task.getValue();

            // Verify connection
            ResultSet resultSet;

            try {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT @@VERSION");
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String resultSetString = resultSet.getString(1);
                    Platform.runLater(() -> status.setText(status.getText() + "\nDatabase Version: " + resultSetString));

                    Platform.runLater(() -> {
                        // Since the connection succeeded, Disable the Text boxes, and the button.
                        userId.setDisable(true);
                        dbServer.setDisable(true);
                        port.setDisable(true);
                        connectBtn.setVisible(false);

                        HBox hbox = new HBox();
                        hbox.setAlignment(Pos.CENTER_RIGHT);

                        Button nextBtn = new Button("NEXT");
                        nextBtn.getStyleClass().add("click-button");

                        hbox.getChildren().add(nextBtn);
                        statusBox.getChildren().add(hbox);

                        nextBtn.setOnAction(event -> {
                            System.out.println("SQL Server Next steps!");
                            Utils.createStage("sql_server.fxml", "theme-1.css");
                        });
                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        @Override
        protected void done() {
            super.done();
            Platform.runLater(() -> progressIndicator.setVisible(false));
        }
    }
}