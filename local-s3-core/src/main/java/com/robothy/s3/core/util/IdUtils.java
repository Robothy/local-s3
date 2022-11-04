package com.robothy.s3.core.util;

public class IdUtils {

  private final static long S4_EPOCH = 1645837713L;


  private final static long SEQUENCE_ID_BITS = 12;
  private final static long WORKER_ID_BITS = 5;
  private final static long DATACENTER_BITS = 5;


  private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_ID_BITS);
  private final static long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
  private final static long MAX_DATACENTER_ID = ~(-1L << DATACENTER_BITS);


  private final static long SEQUENCE_SHIFT = SEQUENCE_ID_BITS;
  private final static long DATACENTER_ID_SHIFT = SEQUENCE_ID_BITS + WORKER_ID_BITS;
  private final static long TIMESTMP_SHIFT = DATACENTER_ID_SHIFT + DATACENTER_BITS;

  private long datacenterId;
  private long machineId;
  private long sequence = 0L;
  private long lastStmp = -1L;

  private static final IdUtils GENERATOR = new IdUtils(0, 0);

  public static IdUtils defaultGenerator() {
    return GENERATOR;
  }

  public IdUtils(long datacenterId, long workerId) {
    if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
      throw new IllegalArgumentException(String.format("datacenterId can't be greater than %d or less than 0", MAX_DATACENTER_ID));
    }
    if (workerId > MAX_WORKER_ID || workerId < 0) {
      throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_WORKER_ID));
    }
    this.datacenterId = datacenterId;
    this.machineId = workerId;
  }

  public String nextStrId() {
    return String.valueOf(nextId());
  }

  public synchronized long nextId() {
    long currStmp = getNewTimestamp();
    if (currStmp < lastStmp) {
      throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
    }

    if (currStmp == lastStmp) {
      sequence = (sequence + 1) & MAX_SEQUENCE;
      if (sequence == 0L) {
        currStmp = getNextMill();
      }
    } else {
      sequence = 0L;
    }

    lastStmp = currStmp;

    return (currStmp - S4_EPOCH) << TIMESTMP_SHIFT
        | datacenterId << DATACENTER_ID_SHIFT
        | machineId << SEQUENCE_SHIFT
        | sequence;
  }

  private long getNextMill() {
    long mill = getNewTimestamp();
    while (mill <= lastStmp) {
      mill = getNewTimestamp();
    }
    return mill;
  }

  private long getNewTimestamp() {
    return System.currentTimeMillis();
  }

}
