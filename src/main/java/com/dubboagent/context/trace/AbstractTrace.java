package com.dubboagent.context.trace;

/**
 * @author chao.cheng
 */
public interface AbstractTrace {


    /**
     * 查看栈顶span
     *
     * @return
     */
    public AbstractSpan peekSpan();

    /**
     * 将最新span压入栈顶
     *
     * @param span
     */
    public void pushSpan(AbstractSpan span);

    /**
     * 获取traceId
     *
     * @return
     */
    public String getTraceId();

    /**
     * 设置traceId
     *
     * @param traceId
     */
    public void setTraceId(String traceId);

    /**
     * 以字符串的方式获取spanid列表
     * 用"-"线分隔
     *
     * @return
     */
    public String getSpanListStr();

    public int getLevel() ;


    public void setLevel(int level) ;


}
