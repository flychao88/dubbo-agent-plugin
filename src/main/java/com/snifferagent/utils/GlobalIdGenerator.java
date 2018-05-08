package com.snifferagent.utils;

/**
 * Date:2017/11/23
 *
 * @author:chao.cheng
 **/
public class GlobalIdGenerator {

    private static final ThreadLocal<IDContext> THREAD_ID_SEQUENCE = new ThreadLocal<IDContext>() {
        @Override
        protected IDContext initialValue() {
            return new IDContext(System.currentTimeMillis(), (short)0);
        }
    };

    /**
     * 以字符串的方式生成traceId
     * @return
     */
    public static String generate() {

        IDContext context = THREAD_ID_SEQUENCE.get();
        return Long.toHexString(context.nextSeq());
    }

    public static void main(String[] args) {
        System.out.println(generate());
    }





}
