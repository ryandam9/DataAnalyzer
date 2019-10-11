package com.analyzer.ui;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class TreeViewEntry<T> extends TreeItem<T> {
    private String type;
    private String parentItem;

    public TreeViewEntry(String type, String parentItem, T text, Node image) {
        super(text, image);
        this.type = type;
        this.parentItem = parentItem;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParentItem() {
        return parentItem;
    }

    public void setParentItem(String parentItem) {
        this.parentItem = parentItem;
    }

    @Override
    public String toString() {
        return "TreeViewEntry{" +
                "type='" + type + '\'' +
                ", parentItem='" + parentItem + '\'' +
                '}';
    }
}