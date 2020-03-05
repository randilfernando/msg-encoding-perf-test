package com.alternate.scripts;

import baseline.Capacity;
import baseline.Destination;
import baseline.ExecInst;
import baseline.ExecType;
import baseline.ExecutionReportDecoder;
import baseline.ExecutionReportEncoder;
import baseline.MessageHeaderDecoder;
import baseline.MessageHeaderEncoder;
import baseline.OrderSide;
import baseline.OrderStatus;
import baseline.OrderType;
import baseline.TimeInForce;
import org.HdrHistogram.Histogram;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

/**
 * @author randilfernando
 */
public class SBETest {
    private static final Histogram encodeHistogram = new Histogram(3600000000000L, 3);
    private static final Histogram decodeHistogram = new Histogram(3600000000000L, 3);

    private static final MessageHeaderDecoder MESSAGE_HEADER_DECODER = new MessageHeaderDecoder();
    private static final MessageHeaderEncoder MESSAGE_HEADER_ENCODER = new MessageHeaderEncoder();
    private static final ExecutionReportDecoder EXECUTION_REPORT_DECODER = new ExecutionReportDecoder();
    private static final ExecutionReportEncoder EXECUTION_REPORT_ENCODER = new ExecutionReportEncoder();

    private static final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private static final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

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

        encodeHistogram.outputPercentileDistribution(new PrintStream(new FileOutputStream(new File("./results/sbe-encode.txt"))), 1.0);
        decodeHistogram.outputPercentileDistribution(new PrintStream(new FileOutputStream(new File("./results/sbe-decode.txt"))), 1.0);
    }

    private static void doIteration() throws IOException {
        ExecutionReport executionReport = SampleObjects.executionReport;

        long encodeStart = System.nanoTime();
        ByteBuffer encoded = encode(executionReport);
        long encodeStop = System.nanoTime();
        encodeHistogram.recordValue(encodeStop - encodeStart);

        byteBuffer.clear();

        messageSize = encoded.limit();

        long decodeStart = System.nanoTime();
        ExecutionReport decoded = decode(encoded);
        long decodeStop = System.nanoTime();
        decodeHistogram.recordValue(decodeStop - decodeStart);

        if (!executionReport.equals(decoded)) {
            throw new RuntimeException("Objects not match");
        }
    }

    private static ByteBuffer encode(ExecutionReport executionReport) {
        EXECUTION_REPORT_ENCODER.wrapAndApplyHeader(directBuffer, 0, MESSAGE_HEADER_ENCODER)
                .buyPower(executionReport.getBuyPower())
                .capacity(Capacity.NONE)
                .clOrderID(executionReport.getClOrderID())
                .cumQty(executionReport.getCumQty())
                .destination(Destination.ATS)
                .execID(executionReport.getExecID())
                .execInst(ExecInst.NONE)
                .execLink(executionReport.getExecLink())
                .execType(ExecType.EXP)
                .instID(executionReport.getInstID())
                .lastPrice(executionReport.getLastPrice())
                .lastQty(executionReport.getLastQty())
                .leavesQty(executionReport.getLeavesQty())
                .orderLink(executionReport.getOrderLink())
                .orderQty(executionReport.getOrderQty())
                .orderStatus(OrderStatus.EXP)
                .orderType(OrderType.MARKET)
                .partyID(executionReport.getPartyID())
                .ruleText(executionReport.getRuleText())
                .side(OrderSide.BUY)
                .timeInForce(TimeInForce.DAY)
                .transactTime(executionReport.getTransactTime());

        byteBuffer.limit(MESSAGE_HEADER_ENCODER.encodedLength() + EXECUTION_REPORT_ENCODER.encodedLength());
        byte[] arr = new byte[byteBuffer.remaining()];
        byteBuffer.get(arr);
        byteBuffer.clear();
        return ByteBuffer.wrap(arr);
    }

    private static ExecutionReport decode(ByteBuffer buffer) throws IOException {
        int bufferOffset = 0;
        MESSAGE_HEADER_DECODER.wrap(new UnsafeBuffer(buffer), bufferOffset);
        int actingBlockLength = MESSAGE_HEADER_DECODER.blockLength();
        int actingVersion = MESSAGE_HEADER_DECODER.version();

        bufferOffset += MESSAGE_HEADER_DECODER.encodedLength();

        EXECUTION_REPORT_DECODER.wrap(directBuffer, bufferOffset, actingBlockLength, actingVersion);

        return ExecutionReport.builder()
                .buyPower(EXECUTION_REPORT_DECODER.buyPower())
                .capacity(EXECUTION_REPORT_DECODER.capacity().toString())
                .clOrderID(EXECUTION_REPORT_DECODER.clOrderID())
                .cumQty(EXECUTION_REPORT_DECODER.cumQty())
                .destination(EXECUTION_REPORT_DECODER.destination().toString())
                .execID(EXECUTION_REPORT_DECODER.execID())
                .execInst(EXECUTION_REPORT_DECODER.execInst().toString())
                .execLink(EXECUTION_REPORT_DECODER.execLink())
                .execType(EXECUTION_REPORT_DECODER.execType().toString())
                .instID(EXECUTION_REPORT_DECODER.instID())
                .lastPrice(EXECUTION_REPORT_DECODER.lastPrice())
                .lastQty(EXECUTION_REPORT_DECODER.lastQty())
                .leavesQty(EXECUTION_REPORT_DECODER.leavesQty())
                .orderLink(EXECUTION_REPORT_DECODER.orderLink())
                .orderQty(EXECUTION_REPORT_DECODER.orderQty())
                .orderStatus(EXECUTION_REPORT_DECODER.orderStatus().toString())
                .orderType(EXECUTION_REPORT_DECODER.orderType().toString())
                .partyID(EXECUTION_REPORT_DECODER.partyID())
                .ruleText(EXECUTION_REPORT_DECODER.ruleText())
                .side(EXECUTION_REPORT_DECODER.side().toString())
                .timeInForce(EXECUTION_REPORT_DECODER.timeInForce().toString())
                .transactTime(EXECUTION_REPORT_DECODER.transactTime())
                .build();
    }
}
