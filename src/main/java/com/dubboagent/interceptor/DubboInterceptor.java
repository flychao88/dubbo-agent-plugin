package com.dubboagent.interceptor;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.*;
import com.dubboagent.context.ContextManager;
import com.dubboagent.context.trace.AbstractSpan;
import com.dubboagent.context.trace.AbstractTrace;
import com.dubboagent.utils.extension.AgentExtensionLoader;
import com.dubboagent.utils.extension.MessageSender;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * Date:2017/11/21
 *
 * @author:chao.cheng
 **/
public class DubboInterceptor {
    private static Logger LOGGER = LoggerFactory.getLogger(DubboInterceptor.class);


    @RuntimeType
    public static Object intercept(@SuperCall Callable<?> call, @Origin Method method, @AllArguments Object[] arguments) {

        long startTime = System.currentTimeMillis();
        Object rtnObj = null;

        DubboInterceptParam paramObj = analyzeDubboParam(arguments);

        beforeMethod(paramObj, arguments);

        try {
            //方法拦截后,调用call方法,程序继续执行
            rtnObj = call.call();
            if (null != rtnObj) {
                LOGGER.info("[方法" + method.getName() + "] 返回值是:" + rtnObj.toString() + "]");
            }

            afterMethod(rtnObj);

        } catch (Throwable e) {
            LOGGER.error("[DubboInterceptor拦截异常] 方法名是:" + method.getName() + " 参数是:", e);
            //捕获异常执行
            dealException(e);

        } finally {
            long endTime = System.currentTimeMillis();

            LOGGER.info("[服务接口名:" + paramObj.getClassName() + " 方法名:" + paramObj.getMethodName() + " 执行时间是:"
                    + (endTime - startTime) + "毫秒]");

            finallyMethod(startTime, endTime, paramObj);

        }
        return rtnObj;
    }

    /**
     * 解析获取Dubbo请求的方法名和服务接口名
     *
     * @param arguments
     * @return
     */
    private static DubboInterceptParam analyzeDubboParam(@AllArguments Object[] arguments) {

        DubboInterceptParam paramObj = new DubboInterceptParam();

        if (arguments != null && arguments.length > 0) {
            Arrays.stream(arguments).forEach((args) -> {
                if (args instanceof Invoker) {
                    Invoker urlStr = (Invoker) args;
                    URL url = urlStr.getUrl();
                    String className = url.getParameter("interface");
                    paramObj.setClassName(className);
                }
                if (args instanceof RpcInvocation) {
                    RpcInvocation rpcInvocation = (RpcInvocation) args;
                    String methodName = rpcInvocation.getMethodName();
                    paramObj.setMethodName(methodName);
                }
            });
        }

        return paramObj;
    }


    /**
     * 初始化前置
     *
     * @param arguments
     * @return
     */
    public static void beforeMethod(DubboInterceptParam paramObj, Object[] arguments) {

        String methodName = paramObj.getMethodName();
        String className = paramObj.getClassName();

        StringBuffer paramBuf = new StringBuffer();
        try {
            if (arguments != null && arguments.length > 0) {

                Arrays.stream(arguments).forEach((args) -> {
                    if (args instanceof RpcInvocation) {
                        RpcInvocation rpcInvocation = (RpcInvocation) args;
                        paramBuf.append(rpcInvocation.getArguments());
                    }
                });
            }

        } catch (Exception e) {
            LOGGER.error("[获取方法" + methodName + " 入参失败]", e);
        }

        AbstractTrace trace = ContextManager.getOrCreateTrace("dubbo");
        AbstractSpan span = trace.peekSpan();


        RpcContext rpcContext = RpcContext.getContext();
        boolean isConsumer = rpcContext.isConsumerSide();
        boolean isProvider = rpcContext.isProviderSide();


        if (isConsumer) {
            LOGGER.info("[consumer]-[方法" + methodName + " 入参是:" + paramBuf.toString() + "]");

            if (null == span) {
                span = ContextManager.createEntrySpan("dubbo");
                span.setMethodName(methodName);
                span.setClassName(className);
                trace.pushSpan(span);
            } else {
                span.setSpanId(span.getSpanId() + 1);
            }


            rpcContext.getAttachments().put("agent-traceId", trace.getTraceId());
            rpcContext.getAttachments().put("agent-spanIdStr", trace.getSpanListStr());

        } else if (isProvider) {
            LOGGER.info("[provider]-[方法" + methodName + " 入参是:" + paramBuf.toString() + "]");

            String traceId = rpcContext.getAttachment("agent-traceId");
            String spanIdStr = rpcContext.getAttachment("agent-spanIdStr");

            if (null != traceId && !"".equals(traceId)) {
                AbstractTrace abstractTrace = ContextManager.createProviderTrace(traceId);
                String[] spanIdTmp = spanIdStr.split("-");
                Collections.reverse(Arrays.asList(spanIdTmp));
                //// TODO: 2017/11/24 创建Span stack 

            }
        }
    }


    /**
     * 拦截方法后置处理
     * 主要判断是否有异常
     *
     * @param rtnObj
     * @throws Throwable
     */
    public static void afterMethod(Object rtnObj) throws Throwable {
        Result result = (Result) rtnObj;
        if (result != null && result.getException() != null) {
            dealException(result.getException());
        }
    }

    /**
     * span消息封装
     * 消息发送
     *
     * @param startTime
     * @param endTime
     */
    public static void finallyMethod(long startTime, long endTime, DubboInterceptParam paramObj) {
        RpcContext rpcContext = RpcContext.getContext();
        boolean isConsumer = rpcContext.isConsumerSide();

        if (isConsumer) {
            AbstractSpan span = ContextManager.activeSpan();
            try {
                span.setStartTime(startTime);
                span.setEndTime(endTime);
                span.setExecuteTime(endTime - startTime);
                span.setMethodName(paramObj.getMethodName());
                span.setClassName(paramObj.getClassName());
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }

            try {
                //发送消息
                MessageSender messageSender = AgentExtensionLoader.getExtensionLoader(MessageSender.class)
                        .loadSettingClass();

                if (messageSender != null) {
                    messageSender.sendMsg("aaaa", span.toString());
                } else {
                    LOGGER.info("null-----------------------");
                }

            } catch (Throwable te) {
                // LOGGER.error("[agent消息发送失败] method:" + span.getMethodName() + " className:" + span.getClassName(), te);
                LOGGER.error("", te);
            } finally {
                ContextManager.cleanTrace();
            }

        }

    }


    /**
     * 如果异常是抛出的方式
     * 采用此方法记录异常
     *
     * @param throwable
     */
    private static void dealException(Throwable throwable) {
        AbstractSpan span = ContextManager.activeSpan();
        span.log(throwable);
    }


    private static String generateOperationName(URL requestURL, Invocation invocation) {
        StringBuilder operationName = new StringBuilder();
        operationName.append(requestURL.getPath());
        operationName.append("." + invocation.getMethodName() + "(");
        for (Class<?> classes : invocation.getParameterTypes()) {
            operationName.append(classes.getSimpleName() + ",");
        }

        if (invocation.getParameterTypes().length > 0) {
            operationName.delete(operationName.length() - 1, operationName.length());
        }

        operationName.append(")");

        return operationName.toString();
    }

    /**
     * 获取请求URL
     *
     * @param url
     * @param invocation
     * @return
     */
    private static String generateRequestURL(URL url, Invocation invocation) {
        StringBuilder requestURL = new StringBuilder();
        requestURL.append(url.getProtocol() + "://");
        requestURL.append(url.getHost());
        requestURL.append(":" + url.getPort() + "/");
        requestURL.append(generateOperationName(url, invocation));
        return requestURL.toString();
    }


    /**
     * @author chao.cheng
     *         封装类
     *         用于获取Dubbo服务的方法名,接口名和参数
     */
    static class DubboInterceptParam {
        private String methodName;
        private String className;

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }
}
