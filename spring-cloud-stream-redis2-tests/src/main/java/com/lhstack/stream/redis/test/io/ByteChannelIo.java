package com.lhstack.stream.redis.test.io;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * 定义用于接收字节流的数据
 * @author lhstack
 * @date 2021/9/7
 * @class ByteChannelIo
 * @since 1.8
 */
public interface ByteChannelIo {

    String BYTE_STREAM_OUT_PUT = "BYTE_STREAM_OUT_PUT";

    String BYTE_STREAM_IN_PUT = "BYTE_STREAM_IN_PUT";

    /**
     * 消息发送通道
     * @return
     */
    @Output(BYTE_STREAM_OUT_PUT)
    MessageChannel messageChannel();

    /**
     * 消息接收通道
     * @return
     */
    @Input(BYTE_STREAM_IN_PUT)
    SubscribableChannel subscribableChannel();
}
