package com.lhstack.stream.redis.test.io;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

/**
 * @author lhstack
 * @date 2021/9/9
 * @class PollableChannelIo
 * @since 1.8
 */
public interface PollableChannelIo {

    String POLLABLE_CHANNEL_IN_PUT = "POLLABLE_CHANNEL_IN_PUT";

    String POLLABLE_CHANNEL_OUT_PUT = "POLLABLE_CHANNEL_OUT_PUT";

    @Output(POLLABLE_CHANNEL_OUT_PUT)
    MessageChannel pollableMessageChannel();

    @Input(POLLABLE_CHANNEL_IN_PUT)
    PollableChannel pollableChannel();
}
