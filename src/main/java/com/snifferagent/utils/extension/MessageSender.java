package com.snifferagent.utils.extension;

/**
 * Created by nina on 2017/11/27.
 */
public interface MessageSender {

    /**
     * 消息发送
     *
     * @param key
     * @param value
     * @throws Exception
     */
    public void sendMsg(String key, String value) throws Exception;
}
