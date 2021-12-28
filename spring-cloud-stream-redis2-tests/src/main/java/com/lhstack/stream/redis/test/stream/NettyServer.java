package com.lhstack.stream.redis.test.stream;

import com.lhstack.stream.redis.test.io.ByteChannelIo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author lhstack
 * @date 2021/9/10
 * @since 1.8
 */
@Component
public class NettyServer implements InitializingBean {

    @Autowired
    @Input(ByteChannelIo.BYTE_STREAM_IN_PUT)
    private MessageChannel messageChannel;

    @Override
    public void afterPropertiesSet() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RtmpDecoder())
                                .addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof ByteBuf) {
                                    byte[] bytes = ByteBufUtil.getBytes((ByteBuf) msg);
                                    messageChannel.send(MessageBuilder.withPayload(bytes).build());
                                }
                            }
                        });
                    }
                }).bind(8800)
                .channel().closeFuture();
    }
}
