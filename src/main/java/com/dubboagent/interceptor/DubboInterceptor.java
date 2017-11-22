package com.dubboagent.interceptor;

import com.alibaba.dubbo.common.utils.CallChainContext;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date:2017/11/21
 * @author:chao.cheng
 **/
public class DubboInterceptor {
    private static Logger LOGGER = LoggerFactory.getLogger(DubboInterceptor.class);


    @RuntimeType
    public static String intercept(@SuperCall Callable<?> call, @Origin Method method, @AllArguments Object[] arguments) {

        String rtnCtx = "";
        StringBuffer paramBuf = new StringBuffer();
        beforeMethod(method, arguments, paramBuf);

        long startTime = System.currentTimeMillis();
        try {
            initCallChainDataMap();
            //方法拦截后,调用call方法,程序继续执行

            Object rtnObj = call.call();
            if (null != rtnObj) {
                LOGGER.info("[方法" + method.getName() + "] 返回值是:" + rtnObj.toString() + "]");
            }

        } catch (Exception e) {
            LOGGER.error("[DubboInterceptor拦截异常] 方法名是:" + method.getName() + " 参数是:", e);
        } finally {
            long endTime = System.currentTimeMillis();
            LOGGER.info("[类名:" + method.getDeclaringClass().getName() + " 方法名:" + method.getName() + " 执行时间是:" + (endTime - startTime) + "毫秒]");
        }
        return rtnCtx;
    }

    public static Object beforeMethod(Method method, Object[] arguments, StringBuffer paramBuf) {
        try {
            if (arguments != null && arguments.length > 0) {
                Arrays.stream(arguments).forEach((args) -> paramBuf.append(args));
                LOGGER.info("[方法" + method.getName() + " 入参是:" + paramBuf.toString() + "]");
            }

            initCallChainDataMap();
        } catch (Exception e) {
            LOGGER.error("[获取方法" + method.getName() + " 入参失败]", e);
        }
        return paramBuf;
    }

    /**
     * 初始化全局map
     * 如果没有值,则是第一次使用,初始化tradeId和spanId
     */
    public static void initCallChainDataMap() {

        Map<String, String> globalMap = CallChainContext.getContext().get();

        if (null == globalMap) {
            globalMap = new ConcurrentHashMap<String, String>();
            CallChainContext.getContext().add(globalMap);

            String traceIdValue = globalMap.get(CallChainContext.TRACEID);
            if (com.alibaba.dubbo.common.utils.StringUtils.isEmpty(traceIdValue)) {
                traceIdValue = Long.toHexString(System.currentTimeMillis());
                globalMap.put(CallChainContext.TRACEID, traceIdValue);
            }

            String spanIdValue = globalMap.get(CallChainContext.SPANID);
            if (com.alibaba.dubbo.common.utils.StringUtils.isEmpty(spanIdValue)) {
                spanIdValue = CallChainContext.DEFAULT_ID;
                globalMap.put(CallChainContext.SPANID, spanIdValue);
            }

            String currentIdValue = globalMap.get(CallChainContext.CURRENTID);
            if (com.alibaba.dubbo.common.utils.StringUtils.isEmpty(currentIdValue)) {
                //currentIdValue = CallChainContext.DEFAULT_ID;
                globalMap.put(CallChainContext.CURRENTID, spanIdValue);

            }
        }
    }
}
