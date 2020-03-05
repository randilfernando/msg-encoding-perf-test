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

    static {
        try {
            executionReport = objectMapper.readValue(new File("./data/exec_report.json"), ExecutionReport.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
