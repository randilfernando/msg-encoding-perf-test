package com.alternate.scripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.HdrHistogram.Histogram;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * @author randilfernando
 */
public class JacksonTest {
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static Histogram encodeHistogram = new Histogram(3600000000000L, 3);
    private static Histogram decodeHistogram = new Histogram(3600000000000L, 3);

    private static long messageSize;

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        long now;

        do {
            doIteration();
            now = System.currentTimeMillis();
        } while (now - startTime < 5000);

        encodeHistogram.reset();
        decodeHistogram.reset();

        for (int i = 0; i < 10000000; i++) {
            doIteration();
        }

        System.out.println("Message size: " + messageSize);

        encodeHistogram.outputPercentileDistribution(new PrintStream(new FileOutputStream(new File("./jackson-encode.txt"))), 1.0);
        decodeHistogram.outputPercentileDistribution(new PrintStream(new FileOutputStream(new File("./jackson-decode.txt"))), 1.0);
    }

    private static void doIteration() throws JsonProcessingException {
        ExecutionReportModified executionReport = SampleObjects.executionReportModified;

        long encodeStart = System.nanoTime();
        String encoded = encode(executionReport);
        long encodeStop = System.nanoTime();
        encodeHistogram.recordValue(encodeStop - encodeStart);

        messageSize = encoded.getBytes(Charset.defaultCharset()).length;

        long decodeStart = System.nanoTime();
        ExecutionReportModified decoded = decode(encoded, ExecutionReportModified.class);
        long decodeStop = System.nanoTime();
        decodeHistogram.recordValue(decodeStop - decodeStart);

        if (!executionReport.equals(decoded)) {
            throw new RuntimeException("Objects not match");
        }
    }

    private static String encode(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private static <T> T decode(String json, Class<T> tClass) throws JsonProcessingException {
        return objectMapper.readValue(json, tClass);
    }
}
