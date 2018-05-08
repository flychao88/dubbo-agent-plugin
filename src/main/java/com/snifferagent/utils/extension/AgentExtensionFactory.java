package com.snifferagent.utils.extension;

/**
 * @author chao.cheng
 */
public interface AgentExtensionFactory {

    /**
     * Agent扩展工厂类
     * @param type
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getExtension(Class<T> type, String name);
}
