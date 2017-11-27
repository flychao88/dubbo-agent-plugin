package com.dubboagent.communication.kafka;

import com.dubboagent.utils.extension.MessageSender;
import com.dubboagent.utils.extension.Setting;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;

/**
 * Date:2017/11/25
 *
 * @author:chao.cheng
 **/
@Setting
public class KafkaMessageSender implements MessageSender {
    private  Properties props = null;
    private String topic = "test1";

    public KafkaMessageSender() {
        props = new Properties();
        props.put("bootstrap.servers", "169.254.82.175:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.ByteArraySerializer");
        // props.put("serializer.class", "kafka.serializer.DefaultEncoder");
        props.put("metadata.broker.list", "169.254.82.175:9092");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "1000");
    }


    @Override
    public  void sendMsg(String key, String value) {
        KafkaProducer<byte[], byte[]> kafkaProducer = null;
        try {
            // 实例化produce
            kafkaProducer = new KafkaProducer<byte[], byte[]>(props);

            // 消息封装
            ProducerRecord<byte[], byte[]> pr = new ProducerRecord<byte[], byte[]>(
                    topic, key.getBytes(), value.getBytes());

            // 发送数据
            kafkaProducer.send(pr, new Callback() {
                // 回调函数
                @Override
                public void onCompletion(RecordMetadata metadata,
                                         Exception exception) {
                    if (null != exception) {
                      //  System.out.println("记录的offset在:" + metadata.offset());
                        System.out.println(exception);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();

        } finally {
            if (kafkaProducer != null) {
                kafkaProducer.close();
            }

        }
    }

    public static void main(String[] args) {
        KafkaMessageSender kafkaMessageSender = new KafkaMessageSender();
        kafkaMessageSender.sendMsg("aaaaaa","cccc3333cc");
    }



}
