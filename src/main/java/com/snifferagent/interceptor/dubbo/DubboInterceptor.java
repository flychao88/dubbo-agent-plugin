package com.snifferagent.interceptor.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSON;
import com.snifferagent.context.ContextManager;
import com.snifferagent.context.trace.AbstractSpan;
import com.snifferagent.context.trace.AbstractTrace;
import com.snifferagent.interceptor.Interceptor;
import com.snifferagent.utils.Sequence;
import com.snifferagent.utils.extension.AgentExtensionLoader;
import com.snifferagent.utils.extension.MessageSender;
import com.snifferagent.utils.extension.Setting;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Date:2017/11/21
 *
 * @author:chao.cheng
 **/
@Setting
public class DubboInterceptor implements Interceptor {
    private static Logger logger = LoggerFactory.getLogger(DubboInterceptor.class);
    private static Sequence seq = Sequence.getInstance();
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @RuntimeType
    @Override
    public Object intercept(@SuperCall Callable<?> call, @Origin Method method, @AllArguments Object[] arguments) {
        Object rtnObj = null;

        DubboInterceptParam paramObj = analyzeDubboParam(arguments);

        beforeMethod(paramObj, arguments);

        long startTime = System.currentTimeMillis();
        try {
            //方法拦截后,调用call方法,程序继续执行
            rtnObj = call.call();
            if (null != rtnObj) {
                logger.info("[方法" + paramObj.getMethodName() + "] 返回值是:" + rtnObj.toString() + "]");
            }

            afterMethod(rtnObj);

        } catch (Throwable e) {
            logger.error("[DubboInterceptor拦截异常] 方法名是:" + method.getName() + " 参数是:", e);
            //捕获异常执行
            dealException(e);

        } finally {
            long endTime = System.currentTimeMillis();

            logger.info("[服务接口名:" + paramObj.getClassName() + " 方法名:" + paramObj.getMethodName() + " 执行时间是:"
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
            Arrays.stream(arguments).forEach((methodParam) -> {
                if (methodParam instanceof Invoker) {
                    Invoker urlStr = (Invoker) methodParam;
                    URL url = urlStr.getUrl();
                    String className = url.getParameter("interface");
                    paramObj.setClassName(className);
                }
                if (methodParam instanceof RpcInvocation) {
                    RpcInvocation rpcInvocation = (RpcInvocation) methodParam;
                    String methodName = rpcInvocation.getMethodName();
                    paramObj.setMethodName(methodName);
                }
            });
        }
        return paramObj;
    }

    /**
     * 初始化前置
     * 判断是消费者还是生产者
     * 根据消费者产生traceId和spanId
     *
     * @param paramObj  dubbo参数对象
     * @param arguments 拦截后获取的参数对象
     */
    public static void beforeMethod(DubboInterceptParam paramObj, Object[] arguments) {

        String methodName = paramObj.getMethodName();
        String className = paramObj.getClassName();

        StringBuffer paramBuf = new StringBuffer();
        try {
            if (arguments != null && arguments.length > 0) {

                Arrays.stream(arguments).forEach((methodParam) -> {
                    if (methodParam instanceof RpcInvocation) {
                        RpcInvocation rpcInvocation = (RpcInvocation) methodParam;
                        Arrays.stream(rpcInvocation.getArguments()).forEach((param) -> {
                            paramBuf.append(param + " ");
                        });
                    }
                });
            }

        } catch (Exception e) {
            logger.error("[获取方法" + methodName + " 入参失败]", e);
        }

        AbstractTrace trace = ContextManager.getOrCreateTrace("dubbo");

        RpcContext rpcContext = RpcContext.getContext();
        boolean isConsumer = rpcContext.isConsumerSide();
        boolean isProvider = rpcContext.isProviderSide();
        AbstractSpan span = trace.peekSpan();

        if (isConsumer) {
            logger.info("[consumer]-[方法" + methodName + " 入参是:" + paramBuf.toString() + "]");

            if (null == span) {
                span = ContextManager.createEntrySpan(1);
                span.setMethodName(methodName);
                span.setClassName(className);
                trace.pushSpan(span);
            } else {
                span.setSpanId(span.getSpanId() + 1);
            }

            rpcContext.getAttachments().put("agent-traceId", trace.getTraceId());
            rpcContext.getAttachments().put("agent-spanIdStr", trace.getSpanListStr());
            rpcContext.getAttachments().put("agent-level", String.valueOf(trace.getLevel() + 1));

        } else if (isProvider) {
            logger.info("[provider]-[方法" + methodName + " 入参是:" + paramBuf.toString() + "]");

            String traceId = rpcContext.getAttachment("agent-traceId");
            String spanIdStr = rpcContext.getAttachment("agent-spanIdStr");
            int level = Integer.valueOf(rpcContext.getAttachment("agent-level"));

            if (null != traceId && !"".equals(traceId)) {
                ContextManager.createProviderTrace(traceId, level);
                String[] spanIdTmp = spanIdStr.split("-");
                List<String> list = Arrays.asList(spanIdTmp);
                list.forEach((spanStr) -> {
                    logger.info("provider foreach span:" + spanStr);
                    AbstractSpan newSpan = ContextManager.createEntrySpan(Integer.valueOf(spanStr));
                    trace.pushSpan(newSpan);
                });
            }
        }
    }


    /**
     * 拦截方法后置处理
     * 主要判断是否有异常
     *
     * @param rtnObj 判断类型是否是Dubbo的返回对象
     *               如果是则获取异常信息
     * @throws Throwable
     */
    public static void afterMethod(Object rtnObj) throws Throwable {
        Result result = (Result) rtnObj;
        if (result != null && result.getException() != null) {
            dealException(result.getException());
        }
    }


    /**
     * finally方法
     * 根据traceId和span信息封装消息对象
     * 将消息对象发送到采集端,如:kafka和elasticSearch等
     *
     * @param startTime
     * @param endTime
     * @param paramObj
     */
    public static void finallyMethod(long startTime, long endTime, DubboInterceptParam paramObj) {
        RpcContext rpcContext = RpcContext.getContext();
        boolean isConsumer = rpcContext.isConsumerSide();

        if (isConsumer) {

            AbstractSpan span = ContextManager.activeSpan();
            AbstractTrace trace = ContextManager.getOrCreateTrace("dubbo");
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> infoDataMap = new TreeMap<>();
            Map<String, Object> levelMap = new TreeMap<>();
            Map<String, String> spanMap = new HashMap<>();

            try {
                span.setStartTime(startTime);
                span.setEndTime(endTime);
                span.setExecuteTime(endTime - startTime);

                String spanId = String.valueOf(span.getSpanId());
                String level = String.valueOf(trace.getLevel());
                String traceId = trace.getTraceId();
                String logJson = JSON.toJSONString(span.getLogList());
                String serverIp = InetAddress.getLocalHost().getHostAddress();

                spanMap.put("spanId", spanId);
                spanMap.put("startTime", dateFormat.format(new Date(startTime)));
                spanMap.put("endTime", dateFormat.format(new Date(endTime)));
                spanMap.put("execTime", String.valueOf(span.getExecuteTime()));
                spanMap.put("methodName", span.getMethodName());
                spanMap.put("className", span.getClassName());
                spanMap.put("log", logJson);
                spanMap.put("serverIp", serverIp);

                levelMap.put("span" + spanId, spanMap);
                infoDataMap.put(level, levelMap);

                resultMap.put("traceId", traceId);
                resultMap.put("levelInfoData", infoDataMap);
                resultMap.put("collectTime", System.currentTimeMillis());

                System.out.println(resultMap);

            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                return;
            }

            String message = "";
            try {
                //发送消息
                message = JSON.toJSONString(resultMap);
                MessageSender messageSender = AgentExtensionLoader.getExtensionLoader(MessageSender.class)
                        .loadSettingClass();

                logger.info("[messageSender]发送的消息体是:" + message);
                messageSender.sendMsg(seq.getSequenceNumber(), message);

            } catch (Throwable te) {
                logger.error("[采集消息发送失败] message:" + message, te);
            }
        }

        ContextManager.cleanTrace();
    }

    /**
     * 如果异常是抛出的方式
     * 采用此方法记录异常
     *
     * @param throwable
     */
    private static void dealException(Throwable throwable) {
        RpcContext rpcContext = RpcContext.getContext();
        boolean isConsumer = rpcContext.isConsumerSide();
        if (isConsumer) {
            AbstractSpan span = ContextManager.activeSpan();
            if (null != span) {
                span.log(throwable);
            }
        }
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
