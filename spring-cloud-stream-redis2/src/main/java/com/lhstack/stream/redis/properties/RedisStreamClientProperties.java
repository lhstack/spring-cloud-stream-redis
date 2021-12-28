package com.lhstack.stream.redis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置客户端信息
 *
 * @author lhstack
 * @date 2021/9/7
 * @class RedisStreamBindingProperties
 * @since 1.8
 */
@ConfigurationProperties("spring.cloud.stream.redis.binder")
public class RedisStreamClientProperties {
    private String redissonConfigYamlFile;

    public String getRedissonConfigYamlFile() {
        return redissonConfigYamlFile;
    }

    public void setRedissonConfigYamlFile(String redissonConfigYamlFile) {
        this.redissonConfigYamlFile = redissonConfigYamlFile;
    }
}
