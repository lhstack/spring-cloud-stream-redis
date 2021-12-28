package com.lhstack.stream.redis.test.controller;

import com.lhstack.stream.redis.test.io.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lhstack
 * @date 2021/9/7
 * @class MessageController
 * @since 1.8
 */
@RestController
@RequestMapping
public class MessageController {

    @Autowired
    @Output(TextChannelIo.TEXT_STREAM_OUT_PUT)
    private MessageChannel textMessageChannel;

    @Autowired
    @Output(JsonChannelIo.JSON_STREAM_OUT_PUT)
    private MessageChannel jsonMessageChannel;

    @Autowired
    @Output(ByteChannelIo.BYTE_STREAM_OUT_PUT)
    private MessageChannel streamMessageChannel;

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    @Output(SourceChannelIo.SOURCE_MESSAGE_OUT_PUT)
    private MessageChannel sourceMessageChannel;

    @Autowired
    @Output(PollableChannelIo.POLLABLE_CHANNEL_OUT_PUT)
    private MessageChannel pollMessageChannel;

    @GetMapping("poll")
    public ResponseEntity<Boolean> pollMessage(@RequestParam(name = "msg") String msg) {
        pollMessageChannel.send(MessageBuilder.withPayload(msg).build());
        return ResponseEntity.ok(true);
    }

    @GetMapping("source")
    public ResponseEntity<Boolean> sendSourceMessage(@RequestParam(name = "msg") String msg) {
        sourceMessageChannel.send(MessageBuilder.withPayload(msg).build());
        return ResponseEntity.ok(true);
    }

    @GetMapping("send")
    public ResponseEntity<Boolean> sendOutputMessage(@RequestParam(name = "output") String output, @RequestParam(name = "msg") String msg) {
        if (output.equals(JsonChannelIo.JSON_STREAM_OUT_PUT)) {
            this.streamBridge.send(output, msg, MediaType.APPLICATION_JSON);
        } else if (output.equals(ByteChannelIo.BYTE_STREAM_OUT_PUT)) {
            this.streamBridge.send(output, msg, MediaType.APPLICATION_OCTET_STREAM);
        } else {
            this.streamBridge.send(output, msg, MediaType.TEXT_PLAIN);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("text")
    public ResponseEntity<Boolean> sendTextMessage(@RequestParam(name = "msg") String msg) {
        textMessageChannel.send(MessageBuilder.withPayload(msg).build());
        return ResponseEntity.ok(true);
    }

    @GetMapping("json")
    public ResponseEntity<Boolean> sendJsonMessage(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        map.put("ip", request.getRemoteAddr());
        jsonMessageChannel.send(MessageBuilder.withPayload(map).build());
        return ResponseEntity.ok(true);
    }

    @GetMapping("stream")
    public ResponseEntity<Boolean> sendStreamMessage() throws Exception {
        FileChannel fileChannel = FileChannel.open(Paths.get("C:\\Users\\lhstack\\Desktop\\spring-cloud-stream-redis\\spring-cloud-stream-redis2-tests\\src\\main\\resources\\application.yml"), StandardOpenOption.READ);
        MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        int remaining = byteBuffer.remaining();
        int count = remaining / 10;
        for (int i = 0; i < count; i++) {
            if (byteBuffer.hasRemaining()) {
                if (count > byteBuffer.remaining()) {
                    count = byteBuffer.remaining();
                }
                byte[] bytes = new byte[count];
                byteBuffer.get(bytes);
                streamMessageChannel.send(MessageBuilder.withPayload(bytes).build());
            }
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("header")
    public ResponseEntity<Boolean> sendHeaderJsonMessage(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        map.put("ip", request.getRemoteAddr());
        map.put("msg", "携带了header信息");
        Map<String, Object> headers = new HashMap<>();
        headers.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        headers.put("ip", request.getRemoteAddr());
        jsonMessageChannel.send(MessageBuilder.createMessage(map, new MessageHeaders(headers)));
        return ResponseEntity.ok(true);
    }
}
