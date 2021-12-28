package com.lhstack.stream.redis.test;

import com.lhstack.stream.redis.test.io.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.messaging.*;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author lhstack
 */
@SpringBootApplication
@EnableBinding({ByteChannelIo.class, JsonChannelIo.class, TextChannelIo.class, SourceChannelIo.class, PollableChannelIo.class})
public class SpringCloudStreamRedisTestsApplication implements ApplicationRunner {

    @Autowired
    @Input(ByteChannelIo.BYTE_STREAM_IN_PUT)
    private MessageChannel messageChannel;

    @Autowired
    @Output(TextChannelIo.TEXT_STREAM_IN_PUT)
    private SubscribableChannel subscribableChannel;

    @Autowired
    @Input(SourceChannelIo.SOURCE_MESSAGE_IN_PUT)
    private PollableMessageSource pollableMessageSource;

    @Autowired
    @Input(PollableChannelIo.POLLABLE_CHANNEL_IN_PUT)
    private PollableChannel pollableChannel;

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudStreamRedisTestsApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pollableMessageSource.poll(message -> System.out.println("pollableMessageSource:" + message));
            }
        }).start();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message<?> receive = pollableChannel.receive();
                if (receive != null) {
                    System.out.println("poll message: " + receive);
                }
            }
        }).start();

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
