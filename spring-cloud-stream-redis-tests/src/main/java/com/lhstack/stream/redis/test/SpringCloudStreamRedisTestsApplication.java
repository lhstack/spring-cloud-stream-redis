package com.lhstack.stream.redis.test;

import com.lhstack.stream.redis.test.io.ByteChannelIo;
import com.lhstack.stream.redis.test.io.JsonChannelIo;
import com.lhstack.stream.redis.test.io.TextChannelIo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author lhstack
 */
@SpringBootApplication
@EnableBinding({ByteChannelIo.class, JsonChannelIo.class, TextChannelIo.class})
public class SpringCloudStreamRedisTestsApplication implements ApplicationRunner {


    @Autowired
    @Output(TextChannelIo.TEXT_STREAM_IN_PUT)
    private SubscribableChannel subscribableChannel;

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudStreamRedisTestsApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //手动监听，StreamListener和手动subscribe只能存在一个
        subscribableChannel.subscribe(new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                System.out.println(message);
            }
        });
        subscribableChannel.send(MessageBuilder.withPayload("test消息").build());
    }
}
