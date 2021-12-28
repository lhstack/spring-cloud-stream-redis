package com.lhstack.stream.redis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * 自定义扩展的bindings配置，命名规则 spring.cloud.stream.redis.input spring.cloud.stream.redis.output
 * @author lhstack
 * @date 2021/9/7
 * @class RedisExtendBindingProperties
 * @since 1.8
 */
@ConfigurationProperties(value = "spring.cloud.stream.redis")
public class RedisExtendBindingProperties extends
        AbstractExtendedBindingProperties<RedisStreamConsumerProperties, RedisStreamProducerProperties, RedisStreamBindingProperties> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.redis.default";

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return RedisStreamBindingProperties.class;
    }
}