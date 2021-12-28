package com.lhstack.stream.redis;

import com.lhstack.stream.redis.properties.RedisStreamProducerProperties;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author lhstack
 * @date 2021/9/7
 * @class RedisStreamMessageHandler
 * @since 1.8
 */
public class RedisStreamMessageHandler implements MessageHandler {

    private final ProducerDestination destination;
    private final ExtendedProducerProperties<RedisStreamProducerProperties> producerProperties;
    private final MessageChannel errorChannel;
    private final StatefulRedisConnection<String, Message<byte[]>> statefulConnection;

    public RedisStreamMessageHandler(StatefulRedisConnection<String, Message<byte[]>> statefulConnection, ProducerDestination destination, ExtendedProducerProperties<RedisStreamProducerProperties> producerProperties, MessageChannel errorChannel) {
        this.destination = destination;
        this.producerProperties = producerProperties;
        this.errorChannel = errorChannel;
        this.statefulConnection = statefulConnection;
    }


    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            //这里对应output，所以这里向redis发送消息即可
            this.statefulConnection.async().publish(destination.getName(), MessageBuilder.createMessage((byte[]) message.getPayload(), message.getHeaders()));
        } catch (Exception e) {
            errorChannel.send(MessageBuilder.withPayload(e.getMessage()).build());
        }
    }
}
