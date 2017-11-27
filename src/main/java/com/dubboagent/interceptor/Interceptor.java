package com.dubboagent.interceptor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author chao.cheng
 */
public interface Interceptor {

    public  Object intercept(@SuperCall Callable<?> call, @Origin Method method, @AllArguments Object[] arguments);
}
