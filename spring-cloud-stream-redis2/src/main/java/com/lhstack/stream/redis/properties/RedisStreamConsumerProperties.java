package com.lhstack.stream.redis.properties;

/**
 * 自定义consumer配置
 *
 * @author lhstack
 * @date 2021/9/7
 * @class RedisConsumerProperties
 * @since 1.8
 */
public class RedisStreamConsumerProperties {

    /**
     * 消费组的应用名称 同一个消费组之间是竞争消费,默认是应用名称
     */
    private String consumer;

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getConsumer() {
        return consumer;
    }

    @Override
    public String toString() {
        return "RedisStreamConsumerProperties{" +
                "consumer='" + consumer + '\'' +
                '}';
    }
}
