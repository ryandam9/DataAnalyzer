package com.analyzer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.analyzer.AppLogger.logger;

public class Utils {
    public static void createStage(String fxmlFileName, String cssFileName) {
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

    /**
     * Loads Database specific Queries stored in a XML file into a Map.
     *
     * @param dbType Type of Database. E.g. oracle, sqlserver, etc. This param is part of the XML file name.
     * @return A map, where key is a query identifier, and value is the SQL Query.
     */
    public static Map<String, String> fetchQueries(String dbType) throws Exception {
        @XmlRootElement
        class SqlMap {
            Map<String, String> sqls = new HashMap<>();

            public Map<String, String> getSqls() {
                return sqls;
            }

            public void setSqls(Map<String, String> sqls) {
                this.sqls = sqls;
            }

            public String getSql(String name) {
                return sqls.get(name);
            }

            public Map<String, String> load(File xmlFile) throws Exception {
                JAXBContext jaxbContext = JAXBContext.newInstance(SqlMap.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                SqlMap sqlMap = (SqlMap) unmarshaller.unmarshal(xmlFile);

                return sqlMap.sqls;
            }
        }

        File xmlFile = null;
        Map<String, String> sqls = null;

        try {
            String xmlFilePath = "resources/queries/" + dbType + ".xml";
            xmlFile = new File(xmlFilePath);
            logger.debug("Queries going to be loaded from: " + xmlFile.getAbsolutePath());
            sqls = new SqlMap().load(xmlFile);
        } catch (Exception e) {
            logger.error("Error Reading SQL Queries from " + xmlFile.getAbsolutePath());
            logger.error(e.getMessage());
            throw e;
        }

        logger.debug("Queries in the XML file:");
        for (String query : sqls.values()) {
            logger.debug(query);
        }

        return sqls;
    }
}