package com.lhstack.stream.redis.producer;

import com.lhstack.stream.redis.properties.RedisStreamConsumerProperties;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.core.env.Environment;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自定义消息发送器，监听redis 通道里面的消息，发送到应用的StreamListener上面
 *
 * @author lhstack
 * @date 2021/9/7
 * @class RedisMessageProducer
 * @since 1.8
 */
public class RedisStreamMessageProducer extends MessageProducerSupport {

    private final ExtendedConsumerProperties<RedisStreamConsumerProperties> properties;
    private final String group;
    private final String consumerName;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final RStream<Object, Object> stream;

    public RedisStreamMessageProducer(RedissonClient redissonClient,
                                      ConsumerDestination destination,
                                      String group,
                                      ExtendedConsumerProperties<RedisStreamConsumerProperties> properties,
                                      Environment environment) {
        this.group = Objects.isNull(group) ? "default" : group;
        this.properties = properties;
        this.consumerName = StringUtils.hasText(properties.getExtension().getConsumer()) ? properties.getExtension().getConsumer() : environment.getProperty("spring.application.name", "default");
        this.stream = redissonClient.getStream(destination.getName());
    }

    @Override
    protected void doStart() {
        try {
            this.stream
                    .createGroupAsync(this.group);
        } catch (Throwable ignore) {
        }
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<StreamMessageId, Map<Object, Object>> streamMessageIdMapMap = stream
                            .readGroup(group, consumerName, StreamReadGroupArgs.neverDelivered());
                    if (streamMessageIdMapMap.isEmpty()) {
                        scheduledExecutorService.schedule(this, 200, TimeUnit.MILLISECONDS);
                    } else {
                        StreamMessageId[] ids = streamMessageIdMapMap.entrySet()
                                .stream().map(item -> {
                                    item.getValue().entrySet().forEach(em -> {
                                        if (!"empty".equals(String.valueOf(em.getKey()))) {
                                            sendMessage((Message<?>) em.getValue());
                                        }
                                    });
                                    return item.getKey();
                                }).toArray(StreamMessageId[]::new);
                        stream.ack(group, ids);
                        scheduledExecutorService.schedule(this, 1, TimeUnit.MILLISECONDS);
                    }
                } catch (Throwable e) {
                    scheduledExecutorService.schedule(this, 1, TimeUnit.SECONDS);
                }
            }
        }, 1, TimeUnit.MILLISECONDS);

    }
}
