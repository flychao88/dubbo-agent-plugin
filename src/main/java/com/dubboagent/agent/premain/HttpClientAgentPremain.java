package com.dubboagent.agent.premain;

import com.dubboagent.interceptor.Interceptor;
import com.dubboagent.utils.extension.AgentExtensionLoader;
import com.dubboagent.utils.extension.Setting;
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
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * Date:2017/11/28
 *
 * @author:chao.cheng
 **/
@Setting
public class HttpClientAgentPremain implements AgentPremain {

    private static Logger LOGGER = LoggerFactory.getLogger(HttpClientAgentPremain.class);
    private final Interceptor interceptor = AgentExtensionLoader.getExtensionLoader(Interceptor.class).loadSettingClass();

    @Override
    public void premain(String argument, Instrumentation inst) {

        if (null == interceptor) {
            LOGGER.info("[interceptor error] 无法正确加载Interceptor.class拦截器,项目将继续启动不影响业务进行!");
            return;
        }

        ElementMatcher elementMatcher = ElementMatchers.nameStartsWith("org.spring.springboot.http")
                .or(nameMatches("org.apache.http.impl.client.InternalHttpClient"))
        .or(nameMatches("org.springframework.web.servlet.DispatcherServlet"));

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
                // .and(not(isAnnotation()))
                .transform((builder, typeDescription, classLoader) -> builder
                        //不匹配哪些方法
                        .method(not(isConstructor())
                                        .and(not(isStatic()))
                                        .and(not(named("main")))
                                        .and((named("doExecute")))
                                        .or(named("doService"))
                                //.method(named("printObj")
                        )
                        //拦截过滤器设置
                        .intercept(MethodDelegation.to(interceptor))
                ).with(new AgentBuilder.Listener() {

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
