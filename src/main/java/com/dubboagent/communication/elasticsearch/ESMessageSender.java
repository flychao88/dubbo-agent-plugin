package com.dubboagent.communication.elasticsearch;

import com.dubboagent.utils.PropertiesLoadUtils;
import com.dubboagent.utils.extension.MessageSender;
import com.dubboagent.utils.extension.Setting;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Date:2017/12/1
 *
 * @author:chao.cheng
 **/
@Setting
public class ESMessageSender implements MessageSender {
    private static Logger logger = LoggerFactory.getLogger(ESMessageSender.class);

    private static String CONFIG_PATH = "/config/es_inv.properties";

    private Client client = null;

    public ESMessageSender() {
        try {
            PropertiesLoadUtils.init(CONFIG_PATH, ESConfig.class);
            client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(
                    InetAddress.getByName(ESConfig.ip), ESConfig.port));

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendMsg(String key, String value) throws Exception {
        try {
            IndexRequest indexRequest = new IndexRequest(ESConfig.indexName, "tweet", key).source(value);

            UpdateRequest updateRequest = new UpdateRequest(ESConfig.indexName, "tweet", key).doc(
                    jsonBuilder().startObject().field("traceId", key)
                            .field("levelInfoData", value).endObject())
                    .upsert(indexRequest);
            client.update(updateRequest).get();


            /*client.prepareUpdate("dmtest7", "tweet" , key).setDoc(jsonBuilder().startObject()
                    .field("traceId", key)
                    .field("levelInfoData", value)
            ).get();*/
            //client.prepareIndex("dmtest5", "tweet", key).setSource(value).execute().actionGet();
        } catch (Throwable e) {
            throw e;
        }
    }

}
