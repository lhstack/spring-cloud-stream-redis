package com.lhstack.stream.redis;

import com.lhstack.stream.redis.properties.RedisStreamConsumerProperties;
import com.lhstack.stream.redis.properties.RedisStreamProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 *
 * @author lhstack
 * @date 2021/9/7
 * @class RedisProvisioningProvider
 * @since 1.8
 */
public class RedisProvisioningProvider implements ProvisioningProvider<ExtendedConsumerProperties<RedisStreamConsumerProperties>, ExtendedProducerProperties<RedisStreamProducerProperties>> {

    @Override
    public ProducerDestination provisionProducerDestination(String name, ExtendedProducerProperties<RedisStreamProducerProperties> properties) throws ProvisioningException {
        return new ProducerDestination() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getNameForPartition(int partition) {
                return "default";
            }
        };
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group, ExtendedConsumerProperties<RedisStreamConsumerProperties> properties) throws ProvisioningException {
        return () -> name;
    }
}
