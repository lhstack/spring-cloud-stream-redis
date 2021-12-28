package com.lhstack.stream.redis;

import com.lhstack.stream.redis.producer.RedisStreamMessageProducer;
import com.lhstack.stream.redis.properties.RedisExtendBindingProperties;
import com.lhstack.stream.redis.properties.RedisStreamClientProperties;
import com.lhstack.stream.redis.properties.RedisStreamConsumerProperties;
import com.lhstack.stream.redis.properties.RedisStreamProducerProperties;
import com.lhstack.stream.redis.utils.ProtostuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.*;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * redisBinder，output和input处理
 *
 * @author lhstack
 * @date 2021/9/7
 * @class RedisStreamBinder
 * @since 1.8
 */
public class RedisStreamMessageChannelBinder extends AbstractMessageChannelBinder<ExtendedConsumerProperties<RedisStreamConsumerProperties>, ExtendedProducerProperties<RedisStreamProducerProperties>, RedisProvisioningProvider> implements ExtendedPropertiesBinder<MessageChannel, RedisStreamConsumerProperties, RedisStreamProducerProperties> {

    private final RedissonClient redissonClient;
    private final RedisExtendBindingProperties redisExtendBindingProperties;

    private static final Set<String> IS_INIT_CREATE_STREAM = new ConcurrentSkipListSet<>();

    @Autowired
    private Environment environment;

    public RedisStreamMessageChannelBinder(RedisProvisioningProvider provisioningProvider, RedisStreamClientProperties redisStreamClientProperties, RedisExtendBindingProperties redisExtendBindingProperties) throws IOException {
        super(null, provisioningProvider);
        String redissonConfigYamlFile = redisStreamClientProperties.getRedissonConfigYamlFile();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = resourcePatternResolver.getResource(redissonConfigYamlFile);
        InputStream redissonConfigSource = resource.getInputStream();
        Config config = Config.fromYAML(redissonConfigSource);
        redissonConfigSource.close();
        Encoder keyEncoder = in -> Unpooled.copiedBuffer(String.valueOf(in).getBytes(StandardCharsets.UTF_8));
        Decoder<Object> keyDecoder = (buf, state) -> buf.toString(StandardCharsets.UTF_8);
        Decoder<Object> valueDecoder = (buf, state) -> {
            //value解码成Message<byte[]>对象，spring会自动处理
            if (!buf.isReadable()) {
                return MessageBuilder.withPayload(new byte[0]).build();
            }
            //这里header用2个字节表示长度即可,一般header长度不会太长,32768基本满足
            short headerLength = buf.readShort();
            byte[] headerBody = new byte[headerLength];
            buf.readBytes(headerBody);
            //body长度是int大小 Integer.MAX_VALUE
            int bodyLength = buf.readInt();
            byte[] bytes = new byte[bodyLength];
            buf.readBytes(bytes);
            //这里内容不做序列化处理，只对header用Protobuf做序列化操作
            Map<String, Object> header = ProtostuffUtils.deserialize(headerBody);
            return MessageBuilder.createMessage(bytes, new MessageHeaders(header));
        };

        Encoder valueEncoder = o -> {
            Message<byte[]> message = (Message<byte[]>) o;
            MessageHeaders headers = message.getHeaders();
            //序列化header
            byte[] headerBytes = ProtostuffUtils.serialize(new HashMap<>(headers));
            //申请内存，这里设置为header长度，body长度和header序列化之后的内容与body内容
            ByteBuf buf = Unpooled.buffer(2 + headerBytes.length + message.getPayload().length + 4);

            buf.writeShort(headerBytes.length).writeBytes(headerBytes).writeInt(message.getPayload().length).writeBytes(message.getPayload());
            return buf;
        };

        config.setCodec(new BaseCodec() {

            @Override
            public Encoder getMapKeyEncoder() {
                return keyEncoder;
            }

            @Override
            public Decoder<Object> getMapKeyDecoder() {
                return keyDecoder;
            }

            @Override
            public Decoder<Object> getValueDecoder() {
                return valueDecoder;
            }

            @Override
            public Encoder getValueEncoder() {
                return valueEncoder;
            }
        });
        this.redissonClient = Redisson.create(config);
        this.redisExtendBindingProperties = redisExtendBindingProperties;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination, ExtendedProducerProperties<RedisStreamProducerProperties> producerProperties, MessageChannel errorChannel) throws Exception {

        this.createStreamAndSendEmptyMessage(destination.getName());
        return new RedisStreamMessageHandler(redissonClient, destination, producerProperties, errorChannel);
    }

    private void createStreamAndSendEmptyMessage(String name) {
        if (!IS_INIT_CREATE_STREAM.contains(name)) {
            this.redissonClient.getStream(name).add(StreamAddArgs.entry("empty", MessageBuilder.withPayload(new byte[0]).build()));
            IS_INIT_CREATE_STREAM.add(name);
        }
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group, ExtendedConsumerProperties<RedisStreamConsumerProperties> properties) throws Exception {
        this.createStreamAndSendEmptyMessage(destination.getName());
        //这里对应input，向input发送消息即可
        return new RedisStreamMessageProducer(redissonClient, destination, group, properties, this.environment);
    }


    @Override
    protected PolledConsumerResources createPolledConsumerResources(String name, String group, ConsumerDestination destination, ExtendedConsumerProperties<RedisStreamConsumerProperties> consumerProperties) {

        this.createStreamAndSendEmptyMessage(destination.getName());
        return new PolledConsumerResources(new RedisStreamMessageSource(redissonClient, name, group, destination, consumerProperties, environment), registerErrorInfrastructure(destination, group, consumerProperties, true));
    }

    @Override
    protected MessageHandler getPolledConsumerErrorMessageHandler(ConsumerDestination destination, String group, ExtendedConsumerProperties<RedisStreamConsumerProperties> consumerProperties) {
        return message -> {
            if (message.getPayload() instanceof MessagingException) {
                AcknowledgmentCallback ack = StaticMessageHeaderAccessor
                        .getAcknowledgmentCallback(
                                Objects.requireNonNull(((MessagingException) message.getPayload())
                                        .getFailedMessage()));
                if (ack != null) {
                    /*if (properties.getExtension().shouldRequeue()) {
                        ack.acknowledge(AcknowledgmentCallback.Status.REQUEUE);
                    }
                    else {
                        ack.acknowledge(AcknowledgmentCallback.Status.REJECT);
                    }*/
                    ack.acknowledge(AcknowledgmentCallback.Status.REJECT);
                }
            }
        };
    }

    @Override
    public RedisStreamConsumerProperties getExtendedConsumerProperties(String channelName) {
        return redisExtendBindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public RedisStreamProducerProperties getExtendedProducerProperties(String channelName) {
        return redisExtendBindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return redisExtendBindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return redisExtendBindingProperties.getExtendedPropertiesEntryClass();
    }
}
