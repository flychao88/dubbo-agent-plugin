package com.dubboagent.interceptor.http;

import com.dubboagent.interceptor.Interceptor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Date:2017/11/27
 *
 * @author:chao.cheng
 **/

public class HttpClientInterceptor implements Interceptor {

    @RuntimeType
    @Override
    public  Object intercept(@SuperCall Callable<?> call, @Origin Method method, @AllArguments Object[] arguments) {

        return null;
    }

}
