package com.lhstack.stream.redis.test.io;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.messaging.MessageChannel;

/**
 * @author lhstack
 * @date 2021/9/9
 * @class SourceChannelIo
 * @since 1.8
 */
public interface SourceChannelIo {

    String SOURCE_MESSAGE_IN_PUT = "SOURCE_MESSAGE_IN_PUT";

    String SOURCE_MESSAGE_OUT_PUT = "SOURCE_MESSAGE_OUT_PUT";

    @Input(SOURCE_MESSAGE_IN_PUT)
    PollableMessageSource messageSource();

    @Output(SOURCE_MESSAGE_OUT_PUT)
    MessageChannel messageChannel();

}
