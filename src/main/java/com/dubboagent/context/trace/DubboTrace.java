package com.dubboagent.context.trace;

import com.dubboagent.context.utils.GlobalIdGenerator;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

/**
 * Date:2017/11/23
 *
 * @author:chao.cheng
 **/
public class DubboTrace implements AbstractTrace {

    /**
     * traceId的所有span
     **/
    private Stack<AbstractSpan> spanList;

    /**
     * 全局traceId
     **/
    private String traceId;


    public DubboTrace() {
        traceId = GlobalIdGenerator.generate();
        spanList = new Stack<AbstractSpan>();
    }

    @Override
    public AbstractSpan peekSpan() {
        try {
            return spanList.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    @Override
    public void pushSpan(AbstractSpan span) {
        spanList.push(span);
    }




}
