package com.dubboagent.context.trace.dubbo;

import com.dubboagent.context.trace.AbstractSpan;
import com.dubboagent.context.trace.AbstractTrace;
import com.dubboagent.utils.GlobalIdGenerator;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Date:2017/11/23
 *
 * @author:chao.cheng
 **/
public class Trace implements AbstractTrace {
    public static int INIT_LEVEL = 1;

    /**
     * traceId的所有span
     **/
    private Stack<AbstractSpan> spanList;

    /**
     * 全局traceId
     **/
    private String traceId;

    protected int level;


    public Trace() {
        traceId = GlobalIdGenerator.generate();
        spanList = new Stack<AbstractSpan>();
        //设置level默认初始值是第一层
        level = INIT_LEVEL;
    }

    public Trace(String traceId, int level) {
        spanList = new Stack<AbstractSpan>();
        this.traceId = traceId;
        this.level = level;
    }

    @Override
    /**
     * 查看栈顶元素
     */
    public AbstractSpan peekSpan() {
        try {

            if(spanList == null && spanList.size() == 0) {
                return null;
            } else {
                return spanList.peek();
            }

        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    @Override
    /**
     * 将新的span信息压到栈顶
     */
    public void pushSpan(AbstractSpan span) {
        spanList.push(span);
    }

    /**
     * 获取traceId
     * @return
     */
    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }


    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String getSpanListStr() {

        StringBuffer stringBuffer = new StringBuffer();
        spanList.stream().forEach((span)->
            stringBuffer.append(span.getSpanId()+"-")
        );

        return stringBuffer.toString().substring(0,stringBuffer.length()-1);

    }


}
