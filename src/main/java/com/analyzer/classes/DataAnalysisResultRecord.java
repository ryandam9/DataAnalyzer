package com.analyzer.classes;

public class DataAnalysisResultRecord {
    private String sno;
    private String db;
    private String schema;
    private String table;
    private String column;
    private String patternMatched;
    private String skipped;
    private String comments;

    public DataAnalysisResultRecord(String sno, String db, String schema, String table, String column, String patternMatched, String skipped, String comments) {
        this.sno = sno;
        this.db = db;
        this.schema = schema;
        this.table = table;
        this.column = column;
        this.patternMatched = patternMatched;
        this.skipped = skipped;
        this.comments = comments;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getPatternMatched() {
        return patternMatched;
    }

    public void setPatternMatched(String patternMatched) {
        this.patternMatched = patternMatched;
    }

    public String getSkipped() {
        return skipped;
    }

    public void setSkipped(String skipped) {
        this.skipped = skipped;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
