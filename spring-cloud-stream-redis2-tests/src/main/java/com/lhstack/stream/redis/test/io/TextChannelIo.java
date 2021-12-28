package com.lhstack.stream.redis.test.io;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author lhstack
 * @date 2021/9/7
 * @class TextChannelIo
 * @since 1.8
 */
public interface TextChannelIo {
    String TEXT_STREAM_OUT_PUT = "TEXT_STREAM_OUT_PUT";

    String TEXT_STREAM_IN_PUT = "TEXT_STREAM_IN_PUT";

    /**
     * json数据结构
     *
     * @return
     */
    @Output(TEXT_STREAM_OUT_PUT)
    MessageChannel messageChannel();


    /**
     * json数据结构
     *
     * @return
     */
    @Input(TEXT_STREAM_IN_PUT)
    SubscribableChannel subscribableChannel();
}
