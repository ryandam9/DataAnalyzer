package com.analyzer.scanning;

import com.dbutils.masking.ThreadController;

public class DatascanController extends ThreadController {
    public DatascanController() {
        super(10);
    }
}