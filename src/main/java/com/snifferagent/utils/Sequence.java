package com.snifferagent.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 时间戳 |机器码 |PID |计数器
 * MongoId生成规则
 * [0,1,2,3] [4,5,6] [7,8] [9,10,11]
 * 本代码生成规则
 * [0,1,2] [3,4,5] [6] [7,8]
 */
public class Sequence {

    /** 上次生成ID的时间截 */
    private String lastTimestamp = "";

    /** id自增 */
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /** mac缺省地址 */
    private String macAddress = "000000000000";

    private Sequence() {
        init();
    }

    public static final Sequence getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void init() {
        macAddress = getMacAddress();
    }

    public String getSequenceNumber() {
        String currentTimestamp = timeGen();
        if (!lastTimestamp.equals(currentTimestamp)) {
            atomicInteger.set(0);
            lastTimestamp = currentTimestamp;
        }
        String sequenceNoStr = currentTimestamp + macAddress + getThreadNo()+ generateId();
        return sequenceNoStr;
    }

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(200);
        int i = 0;
        while (i<20000) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Sequence sequence = Sequence.getInstance();
                    System.out.println(sequence.getSequenceNumber());
                }
            });
            i++;
        }
        executorService.shutdown();
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private synchronized String timeGen() {
        long millis = System.currentTimeMillis();
        String millisStr = Long.toHexString(millis);
        int length = 12;
        return prifexPaddingCode(millisStr, length);
    }

    /**
     * 获取MAC地址,如果有多张网卡则获取第一个, 否则返回000000000000
     *
     * @return 返回Mac地址
     *
     */
    private String getMacAddress() {
        String macAddress = "000000000000";
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                byte[] addres = networkInterface.getHardwareAddress();
                if (addres == null) {
                    continue;
                }
                StringBuffer sb = new StringBuffer("");
                for (int i = 0; i < addres.length; i++) {
                    // 补分割线
//                    if (i != 0) {
                        //                    sb.append("-");
//                    }
                    //字节转换为整数
                    int temp = addres[i] & 0xff;
                    String str = Integer.toHexString(temp);
                    //                System.out.println("每8位:"+str);
                    if (str.length() == 1) {
                        sb.append("0" + str);
                    } else {
                        sb.append(str);
                    }
                }
                macAddress = sb.toString();
                //            System.out.println("本机MAC地址:" + macAddress);
            }
        } catch (SocketException ex) {

        } finally {
            return macAddress;
        }
    }

    /**
     * 线程ID
     * @return 返回线程ID
     */
    private String getThreadNo() {
        long threadId = Thread.currentThread().getId();
        String threadIdStr = Long.toHexString(threadId);
        int length = 4;
        return prifexPaddingCode(threadIdStr, length);
    }

    /**
     * 自增ID
     * @return 返回自增ID
     */
    private String generateId() {
        int sequenceId = atomicInteger.incrementAndGet();
        String sequenceIdStr = Long.toHexString(sequenceId);
        int length = 4;
        return prifexPaddingCode(sequenceIdStr, length);
    }

    /**
     * 对齐并前置填充字符长度
     * @param src 待填充字符
     * @param length 目标字符长度
     * @return 填充后字符
     */
    private String prifexPaddingCode(String src, int length) {
        int prefixNum = 0;
        if ((prefixNum = length - src.length()) > 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < prefixNum; i++) {
                sb.append("0");// 左补0
            }
            sb.append(src);
            src = sb.toString();
        }
        return src;
    }

    private static class SingletonHolder {
        private static final Sequence INSTANCE = new Sequence();
    }
}

