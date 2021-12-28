package com.lhstack.stream.redis.test.stream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author lhstack
 * @date 2021/9/10
 * @since 1.8
 */
public class RtmpDecoder extends ByteToMessageDecoder {


    private byte messageType;
    private int payloadLength;
    private int timeStamp;

    private State state;

    private int version;

    private int chunkSize = 128;


    public RtmpDecoder() {
        state = State.C0;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.isReadable()) {
            //解析version
            if (state == State.C0) {
                version = in.readByte();
                state = State.C1;
                ctx.writeAndFlush(Unpooled.wrappedBuffer(new byte[]{(byte) this.version}));
            } else if (state == State.C1) {
                if (in.readableBytes() < 1536) {
                    return;
                } else {
                    ByteBuf c1Buf = in.readBytes(1536);
                    ByteBuf c2Buf = c1Buf.copy();
                    this.state = State.C2;
                    c1Buf.readerIndex(4);
                    c1Buf.writeInt((int) (System.currentTimeMillis() / 1000));
                    c1Buf.resetReaderIndex();
                    ctx.writeAndFlush(c1Buf);
                    c2Buf.readerIndex(4);
                    c2Buf.writeInt((int) (System.currentTimeMillis() / 1000));
                    c2Buf.resetReaderIndex();
                    ctx.writeAndFlush(c2Buf);
                }
            } else if (state == State.C2) {
                if (in.readableBytes() < 1536) {
                    return;
                } else {
                    in.readBytes(1536);
                    this.state = State.MESSAGE_TYPE;
                }
            } else if (state == State.MESSAGE_TYPE) {
                if (in.isReadable()) {
                    this.messageType = in.readByte();
                    this.state = State.PAYLOAD_LENGTH;
                } else {
                    return;
                }
            } else if (state == State.PAYLOAD_LENGTH) {
                if (in.readableBytes() >= 3) {
                    this.payloadLength = in.readMedium();
                    this.state = State.TIME_STAMP;
                } else {
                    return;
                }
            } else if (state == State.TIME_STAMP) {
                if (in.readableBytes() >= 4) {
                    this.timeStamp = in.readInt();
                    this.state = State.STREAM_ID;
                } else {
                    return;
                }
            } else if (state == State.STREAM_ID) {
                if (in.readableBytes() >= 3) {
                    this.payloadLength = in.readMedium();
                    this.state = State.PAYLOAD;
                } else {
                    return;
                }
            } else if (state == State.PAYLOAD) {
                System.out.println(in.toString(StandardCharsets.UTF_8));
            }
        }
    }

    enum State {
        /**
         * 读取状态
         */
        C0, C1, C2, MESSAGE_TYPE, PAYLOAD_LENGTH, TIME_STAMP, STREAM_ID, PAYLOAD;
    }

    public static void main(String[] args) {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            new Thread(() ->{
                try {
                    System.out.println(1);
                    Thread.sleep(10000);
                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            System.out.println(2);
            boolean await = countDownLatch.await(3, TimeUnit.SECONDS);
            System.out.println(await);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
