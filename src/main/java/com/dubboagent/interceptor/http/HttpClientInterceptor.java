package com.dubboagent.interceptor.http;

import com.dubboagent.context.ContextManager;
import com.dubboagent.context.trace.AbstractSpan;
import com.dubboagent.interceptor.Interceptor;
import com.dubboagent.interceptor.dubbo.DubboInterceptor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Date:2017/11/27
 *
 * @author:chao.cheng
 **/

public class HttpClientInterceptor implements Interceptor {
    private static Logger logger = LoggerFactory.getLogger(HttpClientInterceptor.class);

    @RuntimeType
    @Override
    public Object intercept(@SuperCall Callable<?> call, @Origin Method method, @AllArguments Object[] arguments)
            throws Throwable {
        if (arguments[0] == null || arguments[1] == null) {
            return null;
        }
        final HttpHost httpHost = (HttpHost) arguments[0];
        HttpRequest httpRequest = (HttpRequest) arguments[1];
        AbstractSpan span = null;
        String remotePeer = httpHost.getHostName() + ":" + httpHost.getPort();
        try {
            URL url = new URL(httpRequest.getRequestLine().getUri());
            logger.info("url====" + url);




        } catch (Throwable e) {
            throw e;
        }

        return null;
    }


    public static void main(String[] args) throws Throwable{
        try {

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("http://www.baidu.com/s?wd=java");

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("username","admin"));

            params.add(new BasicNameValuePair("password","123456"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params,"UTF-8");

            httpclient.execute(httpGet);


        } catch (Throwable e) {
            throw e;
        }

    }

}
