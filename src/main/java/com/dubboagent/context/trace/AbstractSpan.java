package com.dubboagent.context.trace;

import java.util.List;

/**
 * Date:2017/11/22
 * @author :chao.cheng
 **/
public interface AbstractSpan {


    public AbstractSpan log(Throwable t);

    public AbstractSpan setOperationName(String operationName);

    public AbstractSpan tag(String key, String value);

    public List<LogDataEntity> getLogList();

    public int getSpanId();

    public void setSpanId(int spanId);

    public void setCurrentSpanId(int currentSpanId);

    public String getMethodName() ;

    public void setMethodName(String methodName) ;

    public String getClassName() ;

    public void setClassName(String className) ;


}
