package com.dubboagent.context.trace;

import com.dubboagent.utils.ThrowableTransformer;

import java.util.LinkedList;
import java.util.List;

/**
 * Date:2017/11/22
 *
 * @author:chao.cheng
 **/
public class DubboTracingSpan implements AbstractSpan {


    protected int spanId;
    protected int currentSpanId;
    /**
     * 方法名称
     **/
    protected String methodName;
    /**
     * 类名称
     **/
    protected String className;

    protected long startTime;
    protected long endTime;

    /**
     * 方法执行时间
     */
    protected long executeTime;

    /**
     * 异常信息集合
     **/
    protected List<LogDataEntity> logList;

    /**
     * 将异常信息封装到List集合中
     */
    @Override
    public DubboTracingSpan log(Throwable t) {
        if (logList == null) {
            logList = new LinkedList<LogDataEntity>();
        }
        logList.add(new LogDataEntity.Builder()
                .add(new KeyValuePair("event", "error"))
                .add(new KeyValuePair("error.kind", t.getClass().getName()))
                .add(new KeyValuePair("message", t.getMessage()))
                .add(new KeyValuePair("stack", ThrowableTransformer.INSTANCE.convert2String(t, 4000)))
                .build(System.currentTimeMillis()));
        return this;
    }


    @Override
    public int getSpanId() {
        return spanId;
    }

    @Override
    public List<LogDataEntity> getLogList() {
        return logList;
    }


    @Override
    public void setSpanId(int spanId) {
        this.spanId = spanId;
    }

    public int getCurrentSpanId() {
        return currentSpanId;
    }

    @Override
    public void setCurrentSpanId(int currentSpanId) {
        this.currentSpanId = currentSpanId;
    }


    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setLogList(List<LogDataEntity> logList) {
        this.logList = logList;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public void setExecuteTime(long exeTime) {
        this.executeTime = exeTime;
    }
}
