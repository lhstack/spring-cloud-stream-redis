package com.lhstack.stream.redis.test.listener;

import com.lhstack.stream.redis.test.io.ByteChannelIo;
import com.lhstack.stream.redis.test.io.JsonChannelIo;
import com.lhstack.stream.redis.test.io.TextChannelIo;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author lhstack
 * @date 2021/9/7
 * @class StreamListenerHandler
 * @since 1.8
 */
@Component
public class StreamListenerHandler {

    //@StreamListener(TextChannelIo.TEXT_STREAM_IN_PUT)
    public void textListener(String message, Message<String> msg) {
        System.out.println("收到text消息: " + message + "," + msg);
    }

    @StreamListener(JsonChannelIo.JSON_STREAM_IN_PUT)
    public void jsonListener(Map<String, Object> message, Message<Map<String, Object>> msg) {
        System.out.println("收到json消息: " + message + "," + msg);
    }

    @StreamListener(ByteChannelIo.BYTE_STREAM_IN_PUT)
    public void byteListener(byte[] message, Message<byte[]> msg) {
        System.out.println("收到byte消息: " + new String(message, StandardCharsets.UTF_8) + "," + msg);
    }
}
