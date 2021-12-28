package com.lhstack.stream.redis;

import com.lhstack.stream.redis.properties.RedisStreamProducerProperties;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
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

    private final ExtendedProducerProperties<RedisStreamProducerProperties> producerProperties;
    private final MessageChannel errorChannel;
    private final RStream<Object, Object> stream;

    public RedisStreamMessageHandler(RedissonClient redissonClient, ProducerDestination destination, ExtendedProducerProperties<RedisStreamProducerProperties> producerProperties, MessageChannel errorChannel) {
        this.producerProperties = producerProperties;
        this.errorChannel = errorChannel;
        this.stream = redissonClient.getStream(destination.getName());
    }


    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            this.stream.add(StreamAddArgs.entry("default",
                    MessageBuilder.createMessage((byte[]) message.getPayload(), message.getHeaders())));
        } catch (Exception e) {
            errorChannel.send(MessageBuilder.withPayload(e.getMessage()).build());
        }
    }
}
