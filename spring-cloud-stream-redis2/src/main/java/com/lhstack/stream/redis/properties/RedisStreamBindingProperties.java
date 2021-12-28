package com.lhstack.stream.redis.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * 绑定consumer和producer
 * @author lhstack
 * @date 2021/9/7
 * @class RedisBindingProperties
 * @since 1.8
 */
public class RedisStreamBindingProperties implements BinderSpecificPropertiesProvider {

    public RedisStreamProducerProperties redisStreamProducerProperties = new RedisStreamProducerProperties();

    public RedisStreamConsumerProperties redisStreamConsumerProperties = new RedisStreamConsumerProperties();

    @Override
    public Object getConsumer() {
        return redisStreamConsumerProperties;
    }

    @Override
    public Object getProducer() {
        return redisStreamProducerProperties;
    }

    public RedisStreamProducerProperties getRedisStreamProducerProperties() {
        return redisStreamProducerProperties;
    }

    public void setRedisStreamProducerProperties(RedisStreamProducerProperties redisStreamProducerProperties) {
        this.redisStreamProducerProperties = redisStreamProducerProperties;
    }

    public RedisStreamConsumerProperties getRedisStreamConsumerProperties() {
        return redisStreamConsumerProperties;
    }

    public void setRedisStreamConsumerProperties(RedisStreamConsumerProperties redisStreamConsumerProperties) {
        this.redisStreamConsumerProperties = redisStreamConsumerProperties;
    }
}
