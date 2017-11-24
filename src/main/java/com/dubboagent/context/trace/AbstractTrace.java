package com.dubboagent.context.trace;

/**
 * @author chao.cheng
 */
public interface AbstractTrace {


    public AbstractSpan peekSpan();

    public void pushSpan(AbstractSpan span);




}
