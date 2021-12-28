package com.lhstack.stream.redis;

import com.lhstack.stream.redis.producer.RedisStreamMessageProducer;
import com.lhstack.stream.redis.properties.RedisExtendBindingProperties;
import com.lhstack.stream.redis.properties.RedisStreamClientProperties;
import com.lhstack.stream.redis.properties.RedisStreamConsumerProperties;
import com.lhstack.stream.redis.properties.RedisStreamProducerProperties;
import com.lhstack.stream.redis.utils.ProtostuffUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * redisBinder，output和input处理
 *
 * @author lhstack
 * @date 2021/9/7
 * @class RedisStreamBinder
 * @since 1.8
 */
public class RedisStreamBinder extends AbstractMessageChannelBinder<ExtendedConsumerProperties<RedisStreamConsumerProperties>, ExtendedProducerProperties<RedisStreamProducerProperties>, RedisProvisioningProvider> implements ExtendedPropertiesBinder<MessageChannel, RedisStreamConsumerProperties, RedisStreamProducerProperties> {

    private final StatefulRedisPubSubConnection<String, Message<byte[]>> connection;
    private final StatefulRedisConnection<String, Message<byte[]>> statefulConnection;
    private final RedisExtendBindingProperties redisExtendBindingProperties;

    public RedisStreamBinder(RedisProvisioningProvider provisioningProvider, RedisStreamClientProperties redisStreamClientProperties, RedisExtendBindingProperties redisExtendBindingProperties) {
        super(null, provisioningProvider);
        //创建redis客户端
        RedisClient redisClient = RedisClient.create(RedisURI.builder().withHost(redisStreamClientProperties.getHost())
                .withPort(redisStreamClientProperties.getPort())
                .withDatabase(redisStreamClientProperties.getDatabase())
                .withPassword(redisStreamClientProperties.getPassword().toCharArray()).build());
        //自定义redis解编码器
        RedisCodec<String, Message<byte[]>> redisCodec = new RedisCodec<String, Message<byte[]>>() {
            @Override
            public String decodeKey(ByteBuffer buf) {
                //key就直接用String
                int remaining = buf.remaining();
                byte[] bytes = new byte[remaining];
                buf.get(bytes);
                return new String(bytes, StandardCharsets.UTF_8);
            }

            @Override
            public Message<byte[]> decodeValue(ByteBuffer buf) {
                //value解码成Message<byte[]>对象，spring会自动处理
                if (!buf.hasRemaining()) {
                    return MessageBuilder.withPayload(new byte[0]).build();
                }
                //这里header用2个字节表示长度即可,一般header长度不会太长,32768基本满足
                short headerLength = buf.getShort();
                byte[] headerBody = new byte[headerLength];
                buf.get(headerBody);
                //body长度是int大小 Integer.MAX_VALUE
                int bodyLength = buf.getInt();
                byte[] bytes = new byte[bodyLength];
                buf.get(bytes);
                //这里内容不做序列化处理，只对header用Protobuf做序列化操作
                Map<String, Object> header = ProtostuffUtils.deserialize(headerBody);
                return MessageBuilder.createMessage(bytes, new MessageHeaders(header));
            }

            @Override
            public ByteBuffer encodeKey(String s) {
                return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public ByteBuffer encodeValue(Message<byte[]> s) {
                MessageHeaders headers = s.getHeaders();
                //序列化header
                byte[] headerBytes = ProtostuffUtils.serialize(new HashMap<>(headers));
                //申请内存，这里设置为header长度，body长度和header序列化之后的内容与body内容
                ByteBuffer buf = ByteBuffer.allocate(2 + headerBytes.length + s.getPayload().length + 4);
                buf.putShort((short) headerBytes.length).put(headerBytes).putInt(s.getPayload().length).put(s.getPayload());
                //翻转
                buf.flip();
                return buf;
            }
        };
        this.connection = redisClient.connectPubSub(redisCodec);
        this.statefulConnection = redisClient.connect(redisCodec);
        this.redisExtendBindingProperties = redisExtendBindingProperties;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination, ExtendedProducerProperties<RedisStreamProducerProperties> producerProperties, MessageChannel errorChannel) throws Exception {
        return new RedisStreamMessageHandler(this.statefulConnection, destination, producerProperties, errorChannel);
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group, ExtendedConsumerProperties<RedisStreamConsumerProperties> properties) throws Exception {
        //这里对应input，向input发送消息即可
        return new RedisStreamMessageProducer(this.connection, destination, group, properties);
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
