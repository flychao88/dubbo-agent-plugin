package com.snifferagent.context;

import com.snifferagent.context.trace.AbstractSpan;
import com.snifferagent.context.trace.AbstractTrace;
import com.snifferagent.context.trace.dubbo.Trace;
import com.snifferagent.context.trace.dubbo.DubboTracingSpan;
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
            abstractTrace = new Trace();
            CONTEXT.set(abstractTrace);
        }
        return abstractTrace;
    }

    public static AbstractTrace createProviderTrace(String traceId, int level) {
        AbstractTrace abstractTrace = CONTEXT.get();
        if(null == abstractTrace) {
            abstractTrace = new Trace(traceId, level);
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
        AbstractSpan span =  CONTEXT.get().peekSpan();
        return span == null ? null : span;
    }



    public static void cleanTrace() {
        CONTEXT.remove();
    }







}
