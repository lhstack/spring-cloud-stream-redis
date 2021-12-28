package com.lhstack.stream.redis.pollable;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binding.BindingTargetFactory;
import org.springframework.cloud.stream.binding.CompositeMessageChannelConfigurer;
import org.springframework.cloud.stream.binding.MessageChannelConfigurer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.PollableChannel;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author lhstack
 * @date 2021/9/9
 * @class PollableChannelBindingTargetFactory
 * @since 1.8
 */

public class PollableChannelBindingTargetFactory implements BindingTargetFactory {

    private final MessageChannelConfigurer messageChannelConfigurer;

    @Autowired
    private GenericApplicationContext context;

    public PollableChannelBindingTargetFactory(CompositeMessageChannelConfigurer messageChannelConfigurer) {
        this.messageChannelConfigurer = messageChannelConfigurer;
    }


    @Override
    public boolean canCreate(Class<?> clazz) {
        return clazz == PollableChannel.class;
    }

    @Override
    public PollableChannel createInput(String name) {
        return getOrCreatePollableChannel(name);
    }

    @Override
    public PollableChannel createOutput(String name) {
        return getOrCreatePollableChannel(name);
    }

    public PollableChannel getOrCreatePollableChannel(String name) {
        PollableChannel pollableChannel = null;
        if (context != null && context.containsBean(name)) {
            try {
                pollableChannel = context.getBean(name, PollableChannel.class);
            } catch (BeanCreationException e) {
                // ignore
            }
        }
        if (pollableChannel == null) {
            QueueChannel channel = new QueueChannel(new LinkedBlockingDeque<>());
            channel.setComponentName(name);
            if (context != null && !context.containsBean(name)) {
                context.registerBean(name, QueueChannel.class, () -> channel);
            }
            pollableChannel = channel;
        }
        if (pollableChannel instanceof QueueChannel) {
            this.messageChannelConfigurer.configureInputChannel(pollableChannel, name);
        }
        return pollableChannel;
    }
}
