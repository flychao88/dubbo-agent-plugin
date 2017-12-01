package com.dubboagent.communication.elasticsearch;

import com.dubboagent.utils.extension.MessageSender;
import com.dubboagent.utils.extension.Setting;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Date:2017/12/1
 *
 * @author:chao.cheng
 **/
@Setting
public class ESMessageSender implements MessageSender {
    private static Logger logger = LoggerFactory.getLogger(ESMessageSender.class);

    private Client client;

    public ESMessageSender() {
        try {
            client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(
                    InetAddress.getByName("103.240.17.228"), 9300));

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendMsg(String key, String value) {
        try {
            client.prepareIndex("dm", "tweet", key).setSource(value).execute().actionGet();
        } catch (Throwable e) {
            throw e;
        }
    }

}
