package com.alternate.scripts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author randilfernando
 */
@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionReport {
    private double buyPower;
    private String capacity;
    private String clOrderID;
    private double cumQty;
    private String destination;
    private String execID;
    private String execInst;
    private String execLink;
    private String execType;
    private int instID;
    private double lastPrice;
    private double lastQty;
    private double leavesQty;
    private String orderLink;
    private double orderQty;
    private String orderStatus;
    private String orderType;
    private String partyID;
    private String ruleText;
    private String side;
    private String timeInForce;
    private long transactTime;
}
