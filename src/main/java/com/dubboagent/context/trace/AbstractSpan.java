package com.dubboagent.context.trace;

import java.util.List;

/**
 * Date:2017/11/22
 *
 * @author :chao.cheng
 **/
public interface AbstractSpan {


    /**
     * 异常记录
     *
     * @param t
     * @return
     */
    public AbstractSpan log(Throwable t);


    /**
     * 获取异常记录列表
     *
     * @return
     */
    public List<LogDataEntity> getLogList();

    /**
     * 获取spanId
     *
     * @return
     */
    public int getSpanId();

    /**
     * 设置spanId
     *
     * @param spanId
     */
    public void setSpanId(int spanId);

    /**
     * 设置当前spanId
     *
     * @param currentSpanId
     */
    public void setCurrentSpanId(int currentSpanId);

    /**
     * 获取方法名
     *
     * @return
     */
    public String getMethodName();

    /**
     * 设置方法名
     *
     * @param methodName
     */
    public void setMethodName(String methodName);

    /**
     * 获取类名
     *
     * @return
     */
    public String getClassName();

    /**
     * 设置类名
     *
     * @param className
     */
    public void setClassName(String className);

    /**
     * 获取开始时间
     *
     * @return
     */
    public long getStartTime();

    /**
     * 设置开始时间
     *
     * @param startTime
     */
    public void setStartTime(long startTime);

    /**
     * 获取结束时间
     *
     * @return
     */
    public long getEndTime();

    /**
     * 设置结束时间
     *
     * @param endTime
     */
    public void setEndTime(long endTime);

    /**
     * 方法执行时间
     * @param exeTime
     */
    public void setExecuteTime(long exeTime);


    /**
     * 设置故障错误日志列表
     * @param logList
     */
    public void setLogList(List<LogDataEntity> logList);




}
