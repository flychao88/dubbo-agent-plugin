package com.snifferagent.interceptor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author chao.cheng
 */
public interface Interceptor {

    /**
     * 拦截主方法
     * @param call
     * @param method
     * @param arguments
     * @return
     * @throws Throwable
     */
    public  Object intercept(@SuperCall Callable<?> call, @Origin Method method, @AllArguments Object[] arguments)
            throws Throwable;
}
