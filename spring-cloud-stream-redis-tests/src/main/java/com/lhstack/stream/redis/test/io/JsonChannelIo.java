package com.lhstack.stream.redis.test.io;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author lhstack
 * @date 2021/9/7
 * @class JsonChannelIo
 * @since 1.8
 */
public interface JsonChannelIo {
    String JSON_STREAM_OUT_PUT = "JSON_STREAM_OUT_PUT";

    String JSON_STREAM_IN_PUT = "JSON_STREAM_IN_PUT";

    /**
     * json数据结构
     *
     * @return
     */
    @Output(JSON_STREAM_OUT_PUT)
    MessageChannel messageChannel();


    /**
     * json数据结构
     *
     * @return
     */
    @Input(JSON_STREAM_IN_PUT)
    SubscribableChannel subscribableChannel();
}
