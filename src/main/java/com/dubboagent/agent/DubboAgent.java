package com.dubboagent.agent;

import com.dubboagent.interceptor.DubboInterceptor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Date:2017/11/21
 * @author :chao.cheng1
 **/
public class DubboAgent {
    private static Logger LOGGER = LoggerFactory.getLogger(DubboAgent.class);

    public static void premain(String argument, Instrumentation inst) {

        ElementMatcher elementMatcher = null;
        if(argument != null && !"".equals(argument)) {
            elementMatcher = ElementMatchers.nameStartsWith(argument);
        } else {
            LOGGER.error("[error] 需要指定需要扫描的包路径,如:com.xxx.test");
            elementMatcher = ElementMatchers.nameStartsWith("com.chinagpay");
        }

        new AgentBuilder.Default()
                //根据包名匹配
                .type(elementMatcher)
                //根据方法名上使用的注解匹配
                //.type(isAnnotatedWith(ToString.class))
                //根据类名包括关键字匹配
               // .type(nameContains("Facade"))
                .and(not(isStatic()))
                .and(not(isAbstract()))
                .and(not(isPrivate()))
                .and(not(isEnum()))
                .and(not(isAnnotation()))
                .transform((builder,typeDescription,classLoader) -> builder
                        //匹配哪些方法
                        .method(not(isConstructor())
                                        .or(not(isStatic()))
                                        .or(not(named("main")))
                        //.method(named("printObj")
                        )
                        //拦截过滤器设置
                        .intercept(MethodDelegation.to(DubboInterceptor.class))
                ).with(new AgentBuilder.Listener(){

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
                                         DynamicType dynamicType) {
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {

            }

            @Override
            public void onError(String s, ClassLoader classLoader, JavaModule javaModule, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule) {

            }
        }).installOn(inst);
    }
}
