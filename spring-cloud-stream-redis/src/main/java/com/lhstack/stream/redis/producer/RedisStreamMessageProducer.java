package com.lhstack.stream.redis.producer;

import com.lhstack.stream.redis.properties.RedisStreamConsumerProperties;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;

import java.util.function.BiConsumer;

/**
 * 自定义消息发送器，监听redis 通道里面的消息，发送到应用的StreamListener上面
 * @author lhstack
 * @date 2021/9/7
 * @class RedisMessageProducer
 * @since 1.8
 */
public class RedisStreamMessageProducer extends MessageProducerSupport {

    private final StatefulRedisPubSubConnection<String, Message<byte[]>> connection;
    private final ConsumerDestination destination;
    private final ExtendedConsumerProperties<RedisStreamConsumerProperties> properties;
    private final String group;

    public RedisStreamMessageProducer(StatefulRedisPubSubConnection<String, Message<byte[]>> connection, ConsumerDestination destination, String group, ExtendedConsumerProperties<RedisStreamConsumerProperties> properties) {
        this.connection = connection;
        this.destination = destination;
        this.group = group;
        this.properties = properties;
    }

    @Override
    protected void doStart() {
        //订阅通道
        connection.async().subscribe(destination.getName()).whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void unused, Throwable throwable) {
                //监听消息变化，然后发送到input,这里订阅订阅的是所有通道的消息，需要判断一下
                connection.addListener(new RedisPubSubAdapter<String, Message<byte[]>>() {
                    @Override
                    public void message(String channel, Message<byte[]> message) {
                        if (channel.equals(destination.getName())) {
                            sendMessage(message);
                        }
                    }
                });
            }
        });
    }
}
