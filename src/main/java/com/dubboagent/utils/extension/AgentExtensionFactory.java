package com.dubboagent.utils.extension;

/**
 * @author chao.cheng
 */
public interface AgentExtensionFactory {

    public <T> T getExtension(Class<T> type, String name);
}
