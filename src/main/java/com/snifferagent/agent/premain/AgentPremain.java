package com.snifferagent.agent.premain;

import java.lang.instrument.Instrumentation;

/**
 * Created by chengchao on 2017/11/28.
 */
public interface AgentPremain {


    public void premain(String argument, Instrumentation inst);
}
