package com.lhstack.stream.redis;

import com.lhstack.stream.redis.pollable.PollableChannelBindingTargetFactory;
import org.springframework.cloud.stream.binding.CompositeMessageChannelConfigurer;
import org.springframework.context.annotation.Bean;

/**
 * @author lhstack
 * @date 2021/9/9
 * @class RedisStreamAutoConfiguration
 * @since 1.8
 */
public class RedisStreamAutoConfiguration {
    @Bean
    public PollableChannelBindingTargetFactory pollableChannelBindingTargetFactory(CompositeMessageChannelConfigurer channelConfigurer) {
        return new PollableChannelBindingTargetFactory(channelConfigurer);
    }
}
