package com.alternate.scripts;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.HdrHistogram.Histogram;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

/**
 * @author randilfernando
 */
public class ProtoStuffTest {
    private static LinkedBuffer linkedBuffer = LinkedBuffer.allocate(1024 * 1024);

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

        encodeHistogram.outputPercentileDistribution(new PrintStream(new FileOutputStream(new File("./results/protostuff-encode.txt"))), 1.0);
        decodeHistogram.outputPercentileDistribution(new PrintStream(new FileOutputStream(new File("./results/protostuff-decode.txt"))), 1.0);
    }

    private static void doIteration() throws IOException {
        ExecutionReport executionReport = SampleObjects.executionReport;

        long encodeStart = System.nanoTime();
        ByteBuffer encoded = encode(executionReport);
        long encodeStop = System.nanoTime();
        encodeHistogram.recordValue(encodeStop - encodeStart);

        messageSize = encoded.limit();

        Schema<ExecutionReport> schema = RuntimeSchema.getSchema(ExecutionReport.class);

        long decodeStart = System.nanoTime();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(encoded);
        ExecutionReport decoded = decode(schema, byteBuf);
        long decodeStop = System.nanoTime();
        decodeHistogram.recordValue(decodeStop - decodeStart);

        if (!executionReport.equals(decoded)) {
            throw new RuntimeException("Objects not match");
        }
    }

    private static ByteBuffer encode(Object obj) {
        linkedBuffer.clear();
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        byte[] buffer = ProtostuffIOUtil.toByteArray(obj, schema, linkedBuffer);
        return ByteBuffer.wrap(buffer);
    }

    private static <T> T decode(Schema<T> schema, ByteBuf buffer) throws IOException {
        T msg = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(new ByteBufInputStream(buffer), msg, schema);
        return msg;
    }
}
