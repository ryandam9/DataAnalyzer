package com.analyzer.ui;

import javafx.scene.control.Button;

public class TileButton extends Button {
    private String dbType;

    public TileButton(String dbType) {
        super("");
        this.dbType = dbType;
    }

    public String getDbType() {
        return dbType;
    }
}