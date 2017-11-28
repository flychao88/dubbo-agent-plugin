package com.dubboagent.context;

import com.dubboagent.context.trace.AbstractSpan;
import com.dubboagent.context.trace.AbstractTrace;
import com.dubboagent.context.trace.DubboTrace;
import com.dubboagent.context.trace.DubboTracingSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date:2017/11/22
 *
 * @author:chao.cheng
 **/
public class ContextManager {
    private static Logger LOGGER = LoggerFactory.getLogger(ContextManager.class);

    private static ThreadLocal<AbstractTrace> CONTEXT = new ThreadLocal();


    public static AbstractTrace getOrCreateTrace(final String operationName) {
        AbstractTrace abstractTrace = CONTEXT.get();
        if(null == abstractTrace) {
            //TODO 未来可以写成SPI的方式,动态加载
            if("dubbo".equalsIgnoreCase(operationName)) {
                abstractTrace = new DubboTrace();
                CONTEXT.set(abstractTrace);
            }
        }
        return abstractTrace;
    }

    public static AbstractTrace createProviderTrace(String traceId) {
        AbstractTrace abstractTrace = CONTEXT.get();
        if(null == abstractTrace) {
            abstractTrace = new DubboTrace();
            abstractTrace.setTraceId(traceId);
            CONTEXT.set(abstractTrace);
        }
        return abstractTrace;
    }

    public static AbstractSpan createEntrySpan(int spanId) {
        AbstractSpan abstractSpan = new DubboTracingSpan();
        abstractSpan.setSpanId(spanId);
        //abstractSpan.setCurrentSpanId(1);
        return abstractSpan;
    }

    public static AbstractSpan activeSpan() {
        return CONTEXT.get().peekSpan();
    }



    public static void cleanTrace() {
        CONTEXT.remove();
    }







}
