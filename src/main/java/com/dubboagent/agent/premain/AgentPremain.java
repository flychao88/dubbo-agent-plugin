package com.dubboagent.agent.premain;

import java.lang.instrument.Instrumentation;

/**
 * Created by nina on 2017/11/28.
 */
public interface AgentPremain {


    public void premain(String argument, Instrumentation inst);
}
