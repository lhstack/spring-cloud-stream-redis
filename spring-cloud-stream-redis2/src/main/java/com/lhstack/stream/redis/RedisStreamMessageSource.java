package com.lhstack.stream.redis;

import com.lhstack.stream.redis.properties.RedisStreamConsumerProperties;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.core.env.Environment;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * poll使用
 *
 * @author lhstack
 * @date 2021/9/9
 * @since 1.8
 */
public class RedisStreamMessageSource extends AbstractMessageSource<byte[]> {
    private final String name;
    private final String group;
    private final ExtendedConsumerProperties<RedisStreamConsumerProperties> consumerProperties;
    private final String consumerName;

    private final Deque<Message<byte[]>> messageDeque = new LinkedBlockingDeque<>();
    private final RStream<Object, Object> stream;

    public RedisStreamMessageSource(RedissonClient redissonClient,
                                    String name,
                                    String group,
                                    ConsumerDestination destination,
                                    ExtendedConsumerProperties<RedisStreamConsumerProperties> consumerProperties, Environment environment) {
        this.name = name;
        this.group = Objects.isNull(group) ? "default" : group;
        this.consumerProperties = consumerProperties;
        this.consumerName = StringUtils.hasText(consumerProperties.getExtension().getConsumer()) ? consumerProperties.getExtension().getConsumer() : environment.getProperty("spring.application.name", "default");
        this.stream = redissonClient.getStream(destination.getName());
        try {
            this.stream.createGroupAsync(this.group);
        } catch (Exception ignore) {
        }
    }


    @Override
    protected Object doReceive() {
        Map<StreamMessageId, Map<Object, Object>> streamMessageIdMapMap = this.stream.readGroup(group, consumerName, StreamReadGroupArgs.neverDelivered());

        try {
            StreamMessageId[] ids = streamMessageIdMapMap.entrySet().stream().map(item -> {
                item.getValue().forEach((key, value) -> {
                    if (!"empty".equals(String.valueOf(key))) {
                        messageDeque.add((Message<byte[]>) value);
                    }
                });
                return item.getKey();
            }).toArray(StreamMessageId[]::new);
            if (ids.length > 0) {
                this.stream.ack(group, ids);
            }
            Message<byte[]> poll = messageDeque.poll();
            if (Objects.nonNull(poll)) {
                poll = MessageBuilder.fromMessage(poll).setHeader("acknowledgmentCallback", (AcknowledgmentCallback) status -> {

                }).build();
            }
            return poll;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getComponentType() {
        return "redis-mq-source";
    }
}
