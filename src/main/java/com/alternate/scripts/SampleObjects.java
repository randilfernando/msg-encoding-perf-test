package com.alternate.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * @author randilfernando
 */
public class SampleObjects {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static ExecutionReport executionReport;
    public static ExecutionReportModified executionReportModified;

    static {
        try {
            executionReport = objectMapper.readValue(new File("./exec_report.json"), ExecutionReport.class);
            executionReportModified = objectMapper.readValue(new File("./exec_report_modified.json"), ExecutionReportModified.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
