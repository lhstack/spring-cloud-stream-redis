package com.lhstack.stream.redis;

import com.lhstack.stream.redis.properties.RedisExtendBindingProperties;
import com.lhstack.stream.redis.properties.RedisStreamClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author lhstack
 * @date 2021/9/7
 * @class RedisStreamBinderAutoConfiguration
 * @since 1.8
 */
@EnableConfigurationProperties({RedisStreamClientProperties.class, RedisExtendBindingProperties.class})
public class RedisStreamBinderAutoConfiguration {

    @Autowired
    private RedisStreamClientProperties redisStreamClientProperties;

    @Autowired
    private RedisExtendBindingProperties redisExtendBindingProperties;

    @Bean
    public RedisStreamBinder redisStreamBinder(RedisProvisioningProvider redisProvisioningProvider) {
        return new RedisStreamBinder(redisProvisioningProvider, redisStreamClientProperties,redisExtendBindingProperties);
    }

    @Bean
    public RedisProvisioningProvider redisProvisioningProvider() {
        return new RedisProvisioningProvider();
    }
}
