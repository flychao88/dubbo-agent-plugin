package com.dubboagent.interceptor.http;

import com.dubboagent.context.ContextManager;
import com.dubboagent.context.trace.AbstractSpan;
import com.dubboagent.context.trace.AbstractTrace;
import com.dubboagent.interceptor.Interceptor;
import com.dubboagent.utils.extension.Setting;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Date:2017/11/27
 *
 * @author:chao.cheng
 **/
public class HttpClientInterceptor implements Interceptor {
    private static Logger logger = LoggerFactory.getLogger(HttpClientInterceptor.class);

    public static String TRACE_ID = "agent-traceId";
    public static String SPAN_ID = "agent-spanIdStr";
    public static String LEVEL = "agent-level";

    @RuntimeType
    @Override
    public Object intercept(@SuperCall Callable<?> call, @Origin Method method, @AllArguments Object[] arguments)
            throws Throwable {

        if (arguments[0] == null || arguments[1] == null) {
            return null;
        }

        boolean isConsumer = checkIsConsumer(arguments);

        Object rtnObj = null;
        try {
            beforeMethod(arguments);
            rtnObj = call.call();
        } catch (Throwable e) {
            logger.error(method.getName()+" http请求失败!",e);
            dealException(e, isConsumer);

        } finally {
            ContextManager.cleanTrace();
        }

        return rtnObj;
    }

    private void beforeMethod(@AllArguments Object[] arguments) throws Throwable {
        if (arguments[0] instanceof HttpHost && arguments[1] instanceof HttpRequest) {
            logger.info("==============client========");
            HttpHost httpHost = (HttpHost) arguments[0];
            HttpRequest httpRequest = (HttpRequest) arguments[1];

            try {
                AbstractTrace trace = ContextManager.getOrCreateTrace("http");
                AbstractSpan span = trace.peekSpan();

                if (null == span) {
                    span = ContextManager.createEntrySpan(1);
                    span.setMethodName("");
                    span.setClassName("");
                    trace.pushSpan(span);
                } else {
                    span.setSpanId(span.getSpanId() + 1);
                }
                Header[] headers = httpRequest.getAllHeaders();

                httpRequest.addHeader(TRACE_ID, trace.getTraceId());
                httpRequest.addHeader(SPAN_ID, trace.getSpanListStr());
                httpRequest.addHeader(LEVEL, String.valueOf(trace.getLevel()));
            } catch (Throwable e) {
               logger.error(e.getMessage(), e);
            }
        }

        if (arguments[0] instanceof HttpServletRequest && arguments[1] instanceof HttpServletResponse) {

            try {
                HttpServletRequest request = (HttpServletRequest) arguments[0];
                String traceId = (String) request.getHeader(TRACE_ID);
                String spanId = (String) request.getHeader(SPAN_ID);
                int level = Integer.valueOf(request.getHeader(LEVEL));

                logger.info("传过来的traceId:" + traceId + "=========传过来的spanId:" + spanId+"=========level:"+level);

                AbstractTrace trace = ContextManager.createProviderTrace(traceId, level);
                String[] spanIdTmp = spanId.split("-");
                List<String> list = Arrays.asList(spanIdTmp);
                list.forEach((spanStr) -> {
                    logger.info("provider foreach span:" + spanStr);
                    AbstractSpan newSpan = ContextManager.createEntrySpan(Integer.valueOf(spanStr));
                    trace.pushSpan(newSpan);
                });
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 如果异常是抛出的方式
     * 采用此方法记录异常
     *
     * @param throwable
     */
    private static void dealException(Throwable throwable, boolean isConsumer) {
        if (isConsumer) {
            AbstractSpan span = ContextManager.activeSpan();
            if (null != span) {
                span.log(throwable);
            }
        }
    }


    /**
     * 判断当前是生产者还是消息者
     *
     * @param arguments
     * @return
     */
    private boolean checkIsConsumer(@AllArguments Object[] arguments) {
        if (arguments[0] instanceof HttpHost && arguments[1] instanceof HttpRequest) {
            return true;
        } else if (arguments[0] instanceof HttpServletRequest && arguments[1] instanceof HttpServletResponse) {
            return false;
        }

        return false;


    }


}
